package com.esgi.vMail.control.plugin;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by Linneya on 07/08/2016.
 */
public class PluginList<T> extends LinkedList<PluginNode<T>> {
    public PluginList() {
        super();
    }

    public PluginList(Collection<? extends PluginNode<T>> collection) {
        super(collection);
    }
    public T executeTree(T param,PluginNode<T> node) {
        System.err.println(node.getRegistry());
        T tmp = node.getValue().launch(param);
        for (PluginNode<T> nodeChild: node.getChilds()) {
            System.err.println("-----" + nodeChild.getRegistry());
            tmp = executeTree(tmp, nodeChild);
        }
        return tmp;
    }
    public T executeAll(T param) {
        final ObjectProperty<T> containerT = new SimpleObjectProperty<>();
        containerT.set(param);
        this.forEach(tPluginNode -> containerT.set(executeTree(containerT.get(), tPluginNode)));
        return containerT.get();
    }
}
