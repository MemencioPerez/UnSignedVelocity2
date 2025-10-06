package io.github._4drian3d.unsignedvelocity.configuration;

import com.google.inject.AbstractModule;

public final class ConfigurationModule extends AbstractModule {
    private final ConfigurationProvider configurationProvider;

    public ConfigurationModule(Configuration initialConfiguration) {
        this.configurationProvider = new ConfigurationProvider(initialConfiguration);
    }

    @Override
    protected void configure() {
        bind(Configuration.class).toProvider(configurationProvider);
    }

    public ConfigurationProvider getConfigurationProvider() {
        return configurationProvider;
    }
}
