package com.esgi.vMail.control.plugin;

import java.lang.reflect.Type;

/**
 * Created by Linneya on 07/08/2016.
 */
public class Launchable<T> {
    private ILaunchable<T> launcher;
    public Launchable(ILaunchable<T> launcher){
        this.launcher = launcher;
    }

    public T launch(T param){
        return this.launcher.launch(param);
    }

    @FunctionalInterface
    public interface ILaunchable<T> {
        T launch(T param);
    }
}
