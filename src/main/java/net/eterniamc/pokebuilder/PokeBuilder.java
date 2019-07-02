package net.eterniamc.pokebuilder;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.inject.Inject;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import net.eterniamc.pokebuilder.modifiers.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(
        id = "pokebuilder",
        name = "A Completely Legal All-Inclusive Genetic Modifier For Pokemon AKA PokeBuilder",
        description = "A Completely Legal All-Inclusive Genetic Modifier For Pokemon AKA PokeBuilder",
        url = "http://eterniamc.net",
        dependencies = {
                @Dependency(id = "huskyui", optional = false),
                @Dependency(id = "pixelmon", optional = false)
        },
        authors = {
                "Justin"
        }
)
public class PokeBuilder {
    public static PokeBuilder instance;
    public static List<Modifier> modifiers;
    public static Config config;
    public static Currency currency;
    private final File file = new File("./config/pokebuilder.conf");
    @Inject
    public Logger logger;

    @Listener
    public void onInit(GamePostInitializationEvent event) {
        instance = this;
        modifiers = Lists.newArrayList(
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
        if (!Sponge.getServiceManager().isRegistered(EconomyService.class)) {
            logger.info("No economy service detected, PokeBuilder will not start");
            return;
        }
        EconomyService es = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
        try {
            Sponge.getEventManager().registerListeners(this, new ChatGuiHelper());
            if (!file.exists()) {
                config = new Config();
                file.createNewFile();
                PrintWriter pw = new PrintWriter(file);
                String json = new GsonBuilder().setPrettyPrinting().create().toJson(config);
                pw.print(json);
                pw.flush();
                pw.close();
            } else
                config = new GsonBuilder().setPrettyPrinting().create().fromJson(new JsonReader(new FileReader(file)), Config.class);
            currency = es.getCurrencies().stream().filter(c -> c.getDisplayName().toPlain().equalsIgnoreCase(config.currencyName)).findFirst().orElse(null);
            if (currency == null) {
                logger.info("Invalid currency, PokeBuilder will not start");
                return;
            }
            Sponge.getCommandManager().register(this,
                    CommandSpec.builder()
                            .permission("pokebuilder")
                            .arguments(GenericArguments.playerOrSource(Text.of("player")))
                            .executor(((src, args) -> {
                                Player player = src.hasPermission("pokebuilder.other") ? args.<Player>getOne("player").get() : (Player) src;
                                PokeBuilderGui.openGui(player);
                                return CommandResult.empty();
                            }))
                            .build(),
                    "pokebuilder", "builder"
            );
            Sponge.getCommandManager().register(this,
                    CommandSpec.builder()
                            .permission("pokebuilder.modifier.give")
                            .arguments(
                                    GenericArguments.playerOrSource(Text.of("player")),
                                    GenericArguments.choices(
                                            Text.of("modifier"),
                                            () -> modifiers.stream().map(m -> m.getClass().getName()).collect(Collectors.toList()),
                                            s -> modifiers.stream().filter(m -> m.getClass().getName().equals(s)).findFirst().get()
                                    )
                            )
                            .executor(((src, args) -> {
                                Player player = args.<Player>getOne("player").get();
                                Modifier modifier = args.<Modifier>getOne("modifier").get();
                                player.getInventory().offer(modifier.getItemStack(player, null));
                                return CommandResult.empty();
                            }))
                            .build(),
                    "givemodifier"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("A Completely Legal All-Inclusive Genetic Modifier For Pokemon AKA PokeBuilder has loaded successfully");
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        try {
            if (!file.exists()) {
                config = new Config();
                file.createNewFile();
                PrintWriter pw = new PrintWriter(file);
                String json = new GsonBuilder().setPrettyPrinting().create().toJson(config);
                pw.print(json);
                pw.flush();
                pw.close();
            } else
                config = new GsonBuilder().setPrettyPrinting().create().fromJson(new JsonReader(new FileReader(file)), Config.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onEntityInteract(InteractEntityEvent event, @Root Player p) {
        if (event.getTargetEntity() instanceof EntityPixelmon) {
            EntityPlayerMP player = (EntityPlayerMP) p;
            NBTTagCompound nbt = player.getHeldItemMainhand().getTagCompound();
            if (nbt != null && nbt.hasKey("Modifier")) {
                try {
                    Modifier modifier = (Modifier) Class.forName(nbt.getString("Modifier")).newInstance();
                    if (modifier.run(new ModifierData(((EntityPixelmon) event.getTargetEntity()).getPokemonData(), p, null))) {
                        player.getHeldItemMainhand().shrink(1);
                        if (player.getHeldItemMainhand().isEmpty())
                            player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
