package io.github._4drian3d.unsignedvelocity.listener;

public interface ConfigurableListener {
    void register();

    void unregister();

    boolean canBeLoaded();
}
