package com.esgi.vMail.control.plugin;

import java.util.LinkedList;

public class PluginNode<T> {
        private String registry;
        private Launchable<T> value;
        private LinkedList<PluginNode> childs = new LinkedList<>();

        public PluginNode(Launchable<T> value ) {
            this.value = value;
        }

        public PluginNode(Launchable<T> value , String registry) {
            this.registry = registry;
            this.value = value;
        }
        
        public Launchable<T> getValue() {
            return value;
        }

        public void setValue(Launchable<T> value) {
            this.value = value;
        }

        public String getRegistry() {
            return registry;
        }

        public void setRegistry(String registry) {
            this.registry = registry;
        }

    public LinkedList<PluginNode> getChilds() {
        return childs;
    }
}
