package net.eterniamc.pokebuilder;

import com.google.inject.Inject;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import net.eterniamc.pokebuilder.Commands.Base;
import net.eterniamc.pokebuilder.Commands.GiveModifier;
import net.eterniamc.pokebuilder.Configuration.Config;
import net.eterniamc.pokebuilder.Configuration.ConfigManager;
import net.eterniamc.pokebuilder.modifiers.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;

import java.util.Arrays;
import java.util.List;

@Plugin(
        id = "pokebuilder",
        name = "PokeBuilder",
        description = "PokeBuilder",
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

    private static PokeBuilder instance;
    private static List<Modifier> modifiers;
    private static Currency currency;
    @Inject
    private Logger logger;
    @Inject
    private PluginContainer container;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public static PokeBuilder getInstance() {
        return instance;
    }

    public static PluginContainer getContainer() {
        return instance.container;
    }

    public static List<Modifier> getModifiers() {
        return modifiers;
    }

    public static Currency getCurrency() {
        return currency;
    }

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
                new ShinyModifier(),
                new GrowthModifier()
        );

        Sponge.getEventManager().registerListeners(this, new ChatGuiHelper());

        currency = Utils.getEconomy().getCurrencies().stream().filter(c -> c.getDisplayName().toPlain().equalsIgnoreCase(Config.currencyName)).findFirst().orElse(null);

        if (currency == null) {
            logger.info("Invalid currency, PokeBuilder will not start");
            return;
        }

        Sponge.getCommandManager().register(this, Base.getSpec(), "pokebuilder", "builder");
        Sponge.getCommandManager().register(this, GiveModifier.getSpec(), "givemodifier");
        logger.info("PokeBuilder has loaded successfully");
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        ConfigManager.save();
        ConfigManager.load();
    }

    @Listener(beforeModifications = true, order = Order.EARLY)
    public void onEntityInteract(InteractEntityEvent event, @Root Player p) {
        if (event.getTargetEntity() instanceof EntityPixelmon) {
            EntityPlayerMP player = (EntityPlayerMP) p;
            NBTTagCompound nbt = player.getHeldItemMainhand().getTagCompound();
            if (nbt != null && nbt.hasKey("Modifier")) {
                event.setCancelled(true);
                try {
                    Modifier modifier = (Modifier) Class.forName(nbt.getString("Modifier")).newInstance();
                    modifier.run(new ModifierData(((EntityPixelmon) event.getTargetEntity()).getPokemonData(), p, null));
                    player.getHeldItemMainhand().shrink(1);
                    if (player.getHeldItemMainhand().isEmpty())
                        player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
