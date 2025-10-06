package io.github._4drian3d.unsignedvelocity.configuration;

import com.google.inject.Provider;

public final class ConfigurationProvider implements Provider<Configuration> {
    private Configuration configuration;

    public ConfigurationProvider(Configuration initialConfiguration) {
        this.configuration = initialConfiguration;
    }

    public void updateConfiguration(Configuration newConfiguration) {
        this.configuration = newConfiguration;
    }

    @Override
    public Configuration get() {
        return configuration;
    }
}
