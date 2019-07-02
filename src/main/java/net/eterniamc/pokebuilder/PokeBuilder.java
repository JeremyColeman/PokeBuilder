package net.eterniamc.pokebuilder;

import com.google.inject.Inject;
import net.eterniamc.pokebuilder.Commands.Base;
import net.eterniamc.pokebuilder.Configuration.Config;
import net.eterniamc.pokebuilder.Configuration.ConfigManager;
import net.eterniamc.pokebuilder.modifiers.*;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;

import java.util.Arrays;
import java.util.List;

@Plugin(
        id = "pokebuilder",
        name = "A Completely Legal All-Inclusive Genetic Modifier For Pokemon AKA PokeBuilder",
        description = "A Completely Legal All-Inclusive Genetic Modifier For Pokemon AKA PokeBuilder",
        url = "http://eterniamc.net",
        dependencies = {
                @Dependency(id = "huskyui"),
                @Dependency(id = "pixelmon")
        },
        authors = {
                "Justin"
        }
)
public class PokeBuilder {

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    @Inject
    public Logger logger;

    private static PokeBuilder instance;
    private static List<Modifier> modifiers;
    private static Currency currency;

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        new ConfigManager(loader);
    }

    @Listener
    public void onInit(GamePostInitializationEvent event) {
        instance = this;
        if (!Sponge.getServiceManager().isRegistered(EconomyService.class)) {
            logger.error("There is no economy plugin that implements EconomyService! PokeBuilder will not start");
            return;
        }
        modifiers = Arrays.asList(
                new EVModifier(),
                new GenderModifier(),
                new HiddenAbilityModifier(),
                new IVModifier(),
                new MaxHappinessModifier(),
                new MoveModifier(),
                new NatureModifier(),
                new PokeballModifier(),
                new ShinyModifier()
        );

        Sponge.getEventManager().registerListeners(this, new ChatGuiHelper());

        currency = Utils.getEconomy().getCurrencies().stream().filter(c -> c.getDisplayName().toPlain().equalsIgnoreCase(Config.currencyName)).findFirst().orElse(null);

        if (currency == null) {
            logger.info("Invalid currency, PokeBuilder will not start");
            return;
        }

        Sponge.getCommandManager().register(this, Base.getSpec(), "pokebuilder", "builder");
        logger.info("A Completely Legal All-Inclusive Genetic Modifier For Pokemon AKA PokeBuilder has loaded successfully");
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        ConfigManager.save();
        ConfigManager.load();
    }

    public static PokeBuilder getInstance() {
        return instance;
    }

    public static List<Modifier> getModifiers() {
        return modifiers;
    }

    public static Currency getCurrency() {
        return currency;
    }
}
