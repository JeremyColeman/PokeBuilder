package net.eterniamc.pokebuilder.Configuration;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;

public class ConfigManager {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static ConfigurationOptions options;
    private static ConfigurationNode node;

    public ConfigManager(ConfigurationLoader<CommentedConfigurationNode> loader) {
        ConfigManager.loader = loader;
        options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
        load();
    }

    public static void load() {
        try {
            node = loader.load(options);
            node.getValue(TypeToken.of(Config.class), new Config());
            save();
        } catch (IOException | ObjectMappingException exception) {
            exception.printStackTrace();
        }
    }

    public static void save() {
        try {
            node.setValue(TypeToken.of(Config.class), new Config());
            loader.save(node);
        } catch (IOException | ObjectMappingException exception) {
            exception.printStackTrace();
        }
    }

}
