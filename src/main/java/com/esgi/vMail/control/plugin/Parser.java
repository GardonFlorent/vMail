package com.esgi.vMail.control.plugin;

import com.esgi.vMail.model.Chat;
import com.esgi.vMail.plugins.PluginType;
import com.esgi.vMail.plugins.annotations.Plugin;
import com.esgi.vMail.plugins.annotations.PluginOption;
import com.esgi.vMail.plugins.annotations.PluginReturn;
import custo.java.nio.Directory;
import custo.javax.module.model.Module;
import custo.javax.module.model.ModuleFactory;
import custo.javax.module.model.error.ModuleFormatException;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by Linneya on 03/08/2016.
 */
public class Parser {
    private final static Path path = Paths.get("./plugins");
    private static Directory moduleDir;
    private final static ModuleFactory factory = new ModuleFactory();
    private final static ObservableMap<Module, Launchable<?>> loadedModules = FXCollections.observableHashMap();
    private final static HashMap<PluginType, PluginList<?>> nodeMap = new HashMap<>();
    private final static HashMap<PluginType, ObservableList<Launchable<?>>> addonMap = new HashMap<>();
    private final static HashMap<Launchable<?>, Launchable<?>> unloadMap = new HashMap<>();
    private static Directory createDir() {
        try {
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    return new Directory(path);
                } else {
                    throw new IOException(String.format("%s already exist and is not a directory", path));
                }
            }
            return Directory.mkdirs(path.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ModuleFactory getFactory() {
        return factory;
    }

    static {
        Parser.loadedModules.addListener((MapChangeListener<? super Module, ? super Launchable<?>>) change -> {
            Parser.freeNodeMap();
            Parser.determineNodeMap();
        });

        Parser.addonMap.put(PluginType.MESSAGE, FXCollections.observableArrayList());
        Parser.addonMap.put(PluginType.CHAT, FXCollections.observableArrayList());

        determineLinkedRules();
        Parser.moduleDir = createDir();
        try {

            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean addPlugin(InputStream stream, String fileName) {
        Path temporary = Paths.get("./tmp");
        Path tmpPluginPath = Paths.get(temporary.toString() + "/" + fileName);
        Path pluginPath = Paths.get(path.toString() + "/" + fileName);
        try {
            Files.deleteIfExists(temporary);
            Files.createDirectory(temporary);

            FileOutputStream oStream = new FileOutputStream(tmpPluginPath.toString());
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = stream.read(bytes)) != -1) {
                oStream.write(bytes, 0, read);
            }
            oStream.close();
            Files.move(tmpPluginPath, pluginPath);

            return Files.exists(pluginPath);
        } catch (FileAlreadyExistsException error) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Plugin already exist at " + pluginPath);
            alert.showAndWait();
            try {
                Files.deleteIfExists(tmpPluginPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Files.deleteIfExists(temporary);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean delPlugin(String fileName) {
        try {
            Path pluginPath = Paths.get(path.toString() + "/" + fileName);
            Module module = factory.getModuleByPath(pluginPath);
            factory.unregister(module);
            module = null;
            Files.delete(pluginPath);
            return !Files.exists(pluginPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void determineNodeMap() {
        Parser.nodeMap.put(PluginType.CHAT, new PluginList<>());
        Parser.nodeMap.put(PluginType.MESSAGE, new PluginList<String>());
        System.err.println("je suis passer par là");
        Parser.nodeMap.keySet().forEach(key -> Parser.setNodeLevel(key, null ,""));
    }

    private static void setNodeLevel(PluginType type, PluginNode node, String dependency) {
        System.err.println("#######################################");
        System.err.println("Type: " + type);
        System.err.println((node != null)? "NodeName: " + node.getRegistry() : "ROOT");
        System.err.println((dependency == "" || dependency == null)? "No Dependency" : "Dependency: " + dependency);
        System.err.println("#######################################");
        ObservableList<Module> modules = FXCollections.observableArrayList();
        Parser.loadedModules.keySet().forEach(module -> modules.add(module));
        modules.filtered(module -> module.getProperties().get("type").equals(type) && module.getProperties().get("dependOn").equals(dependency)).forEach(module -> {
            PluginNode nextNode = null;
            switch((PluginType)module.getProperties().get("type")) {
                case MESSAGE:
                    nextNode = new PluginNode<>((Launchable<String>) Parser.loadedModules.get(module), (String) module.getProperties().get("registryName"));
                    break;
                default:
                    break;
            }

            if (node == null) {
                Parser.nodeMap.get(type).add(nextNode);
            } else {
                node.getChilds().add(nextNode);
            }
            Parser.setNodeLevel(type, nextNode, nextNode.getRegistry());
        });
    }

    private static void freeNodeMap() {
        Parser.nodeMap.clear();
    }

    private static void init() throws IOException {
        moduleDir.getSubPaths().forEach(path -> {
            try {
                System.out.println(path);
                factory.register(new Module(path));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });
        moduleDir.addDirectoryListener((key, events) -> {
            events.forEach(watchEvent -> {
                if (watchEvent.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                    try {
                        System.out.println(watchEvent.context());
                        factory.register(new Module(path));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
                if (watchEvent.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                        factory.unregister(factory.getModuleByPath((Path) watchEvent.context()));
                }
            });
        });
    }

    private static void determineLinkedRules() {
        factory.setOnRegister(event -> {
            Module module = event.getSource();
            module.newLoader();
            Enumeration<JarEntry> entries = module.getJarFile().entries();
            ArrayList<Class<?>> classs = new ArrayList<>();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.toString().endsWith(".class")) {
                    try {
                        Class<?> clazz =  Class.forName(entry.toString().replace('/', '.').substring(0, entry.toString().length() - ".class".length()), false, module.getLoader());
                        classs.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            classs.forEach(clazz -> {
                if (clazz.isAnnotationPresent(Plugin.class)) {
                    Plugin annotation = clazz.getAnnotation(Plugin.class);
                    module.getProperties().put("author", annotation.author());
                    module.getProperties().put("registryName", annotation.registryName());
                    module.getProperties().put("creationDate", annotation.creationDate());
                    module.getProperties().put("version", annotation.version());
                    module.getProperties().put("dependOn", annotation.dependOn());
                    module.getProperties().put("name", annotation.name());
                    module.getProperties().put("type", annotation.type());
                    System.out.println(module.getProperties());
                }
            });
            if (module.getProperties().isEmpty()) {System.err.println(String.format("%s is not a valid module for vMail", module.getJarFile().getName()));return;}
            module.getLoader().close();
            factory.load(module);
        });

        factory.setOnLoad(event -> {
            final boolean[] isDuplicate = {false};
            Module module = event.getSource();
            Parser.loadedModules.keySet().forEach(module1 ->  {
                if (module1.getProperties().get("registryName").equals(module.getProperties().get("registryName")))
                    isDuplicate[0] = true;
            });
            if (isDuplicate[0]) {
                System.err.println("Duplicate module. ignoring.");
                return;
            }
            module.newLoader();
            Enumeration<JarEntry> entries = module.getJarFile().entries();
            Class<? extends Runnable> classLauncher = null;

            Class<?> toLaunch = null;
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.toString().endsWith(".class")) {
                    Class<?> clazz = null;
                    try {
                        clazz = Class.forName(entry.toString().replace('/', '.').substring(0, entry.toString().length() - ".class".length()), false, module.getLoader());
                        if (clazz.isAnnotationPresent(Plugin.class)) {
                            toLaunch = clazz;
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (toLaunch == null) {
                System.err.println(String.format("%s is not a valid module for vMail", module.getJarFile().getName()));
                return;
            }
            Launchable<?> plugin = null;
            Launchable<?> addon = null;
            Launchable<?> unloader = null;
            for (Method method: toLaunch.getMethods()) {
                switch((PluginType)module.getProperties().get("type")) {
                    case MESSAGE:
                        Class<?> finalToLaunch = toLaunch;
                        if (method.isAnnotationPresent(PluginReturn.class)) {
                            plugin = new Launchable<String>(param -> {
                                try {
                                    return (String) method.invoke(finalToLaunch.newInstance(), param);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            });
                        }
                        if (method.isAnnotationPresent(PluginOption.class)) {
                            switch(method.getAnnotation(PluginOption.class).aimTo()) {
                                case LOAD:
                                    addon = new Launchable<>(param -> {
                                        try {
                                            return (WebView) method.invoke(finalToLaunch.newInstance(), param);
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        } catch (InvocationTargetException e) {
                                            e.printStackTrace();
                                        } catch (InstantiationException e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    });
                                    break;
                                case UNLOAD:
                                    unloader = new Launchable<>(param -> {
                                        try {
                                            return (WebView) method.invoke(finalToLaunch.newInstance(), param);
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        } catch (InvocationTargetException e) {
                                            e.printStackTrace();
                                        } catch (InstantiationException e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    });
                                    break;
                                default:
                                    break;
                            }
                        }

                        break;
                    case CHAT:
                    default:
                        break;
                }
            }
            if (addon != null && unloader == null) {
                System.err.println("For plugin " + module.getJarFile().getName() + ": No unloader found for the addon. Aborting.");
                module.getLoader().close();
                return;
            }
            if (plugin != null) {
                Parser.loadedModules.put(module, plugin);
            }

            if (addon != null && unloader != null) {
                Parser.addonMap.get(module.getProperties().get("type")).add(addon);
                Parser.unloadMap.put(addon, unloader);
            }
        });

        factory.setOnUnregister(event -> factory.unload(event.getSource()));

        factory.setOnUnload(event -> {
            Module module = event.getSource();
            module.getLoader().close();
        });
    }

    public static void init1() {

    }

    public static ObservableMap<Module, Launchable<?>> getLoadedModules() {
        return loadedModules;
    }

    public static HashMap<PluginType, PluginList<?>> getNodeMap() {
        return nodeMap;
    }

    public static HashMap<PluginType, ObservableList<Launchable<?>>> getAddonMap() {
        return addonMap;
    }

    public static HashMap<Launchable<?>, Launchable<?>> getUnloadMap() {
        return unloadMap;
    }
}
