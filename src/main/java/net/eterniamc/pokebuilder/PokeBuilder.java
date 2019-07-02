package net.eterniamc.pokebuilder;

import com.codehusky.huskyui.StateContainer;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.action.ActionType;
import com.codehusky.huskyui.states.action.runnable.RunnableAction;
import com.codehusky.huskyui.states.element.ActionableElement;
import com.codehusky.huskyui.states.element.Element;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.inject.Inject;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonItemsPokeballs;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.items.ItemPixelmonSprite;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.eterniamc.pokebuilder.modifiers.*;
import net.minecraft.entity.player.EntityPlayerMP;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;
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
    private List<Modifier> modifiers;
    private final File file = new File("./config/pokebuilder.conf");
    public Config config;
    public Currency currency;
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
                                PlayerPartyStorage store = Pixelmon.storageManager.getParty((EntityPlayerMP) player);
                                List<Pokemon> party = Arrays.stream(store.getAll()).collect(Collectors.toList());
                                StateContainer container = new StateContainer();
                                Page.PageBuilder main = Page.builder()
                                        .setAutoPaging(false)
                                        .setInventoryDimension(InventoryDimension.of(9, 3))
                                        .setTitle(Text.of(TextColors.RED, "PokeBuilder"));
                                for (int i = 0; i < 10; i++) {
                                    main.addElement(new Element(
                                            ItemStack.builder()
                                                    .itemType(ItemTypes.STAINED_GLASS_PANE)
                                                    .add(Keys.DISPLAY_NAME, Text.EMPTY)
                                                    .build()
                                    ));
                                }
                                for (Pokemon pixelmon : party) {
                                    if (pixelmon != null) {
                                        if (!config.blacklistedPokemon.stream().anyMatch(s -> s.equalsIgnoreCase(pixelmon.getDisplayName()))) {
                                            main.addElement(new ActionableElement(
                                                            new RunnableAction(container, ActionType.NONE, "", c -> createEditorPage(container, pixelmon, player, es).openState(player, "editor")),
                                                            ItemStack.builder()
                                                                    .from((ItemStack) (Object) ItemPixelmonSprite.getPhoto(pixelmon))
                                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, pixelmon.getDisplayName()))
                                                                    .build()
                                                    )
                                            );
                                        } else {
                                            main.addElement(new Element(ItemStack.builder()
                                                            .from((ItemStack) (Object) ItemPixelmonSprite.getPhoto(pixelmon))
                                                            .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize("&c" + pixelmon.getDisplayName() + " &7{&c&lBLACKLISTED&7}"))
                                                            .build()
                                                    )
                                            );
                                        }
                                    } else {
                                        main.addElement(new ActionableElement(
                                                new RunnableAction(container, ActionType.CLOSE, "", c -> {
                                                    double cash = Utils.getBal(player);
                                                    if (cash < config.pokemonCost && cash < config.legendaryCost)
                                                        Utils.sendPlayerError(player, "You can't afford this!");
                                                    else {
                                                        ChatGuiHelper.addGUI("Enter the name of the pokemon you want, regular pokemon getCost(pokemon) $" + config.pokemonCost + " and legendaries getCost(pokemon) $" + config.legendaryCost, player, text -> {
                                                            EnumSpecies pokemon = EnumSpecies.getFromNameAnyCase(text.toPlain());
                                                            Pokemon pixelmon1 = Pixelmon.pokemonFactory.create(pokemon);
                                                            if (pokemon == null) {
                                                                Utils.sendPlayerError(player, "Invalid Pokemon");
                                                                return;
                                                            } else if (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> p == pokemon)) {
                                                                es.getOrCreateAccount(player.getUniqueId()).ifPresent(a -> a.withdraw(currency, new BigDecimal(config.legendaryCost), Cause.of(EventContext.empty(), this)));
                                                            } else {
                                                                es.getOrCreateAccount(player.getUniqueId()).ifPresent(a -> a.withdraw(currency, new BigDecimal(config.pokemonCost), Cause.of(EventContext.empty(), this)));
                                                            }
                                                            pixelmon1.setOriginalTrainer((EntityPlayerMP) player);
                                                            store.add(pixelmon1);
                                                        });
                                                    }
                                                }),
                                                ItemStack.builder()
                                                        .itemType((ItemType) PixelmonItemsPokeballs.pokeBall)
                                                        .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Create a new Pokemon"))
                                                        .add(Keys.ITEM_LORE, Arrays.asList(
                                                                Text.of(config.pokemonCost > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                                                        "Regular Pokemon: " + config.pokemonCost + " " + currency.getPluralDisplayName().toPlain()
                                                                ),
                                                                Text.of(
                                                                        config.legendaryCost > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                                                        "Legendaries: " + config.legendaryCost + " " + currency.getPluralDisplayName().toPlain()
                                                                )
                                                        ))
                                                        .build()
                                        ));
                                    }
                                }
                                for (int i = 0; i < 11; i++) {
                                    main.addElement(new Element(
                                            ItemStack.builder()
                                                    .itemType(ItemTypes.STAINED_GLASS_PANE)
                                                    .add(Keys.DISPLAY_NAME, Text.EMPTY)
                                                    .build()
                                    ));
                                }
                                for (int i = 0; i < 3; i++) {
                                    main.putElement(i * 9 + 8, new Element(
                                            ItemStack.builder()
                                                    .itemType(ItemTypes.STAINED_GLASS_PANE)
                                                    .add(Keys.DYE_COLOR, DyeColors.RED)
                                                    .add(Keys.DISPLAY_NAME, Text.EMPTY)
                                                    .build()
                                    ));
                                }
                                container.setInitialState(main.build("main"));
                                container.launchFor(player);
                                return CommandResult.empty();
                            }))
                            .build(),
                    "pokebuilder", "builder"
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

    private StateContainer createEditorPage(StateContainer container, Pokemon pokemon, Player player, EconomyService es) {
        if (container.hasState("editor"))
            container.removeState("editor");
        Page.PageBuilder builder = Page.builder()
                .setTitle(TextSerializers.FORMATTING_CODE.deserialize("&cPokeBuilder &7{&aModifiers&7}"))
                .setAutoPaging(true)
                .setEmptyStack(ItemStack.builder()
                        .itemType(ItemTypes.STAINED_GLASS_PANE)
                        .add(Keys.DISPLAY_NAME, Text.EMPTY)
                        .build()
                )
                .setParent("main");
        for (int i = 0; i < modifiers.size(); i++) {
            Modifier modifier = modifiers.get(i);
            if (modifier.getCost(pokemon) < 0)
                builder.putElement(i / 2 * 9 + i % 2 * 8, new Element(
                        ItemStack.builder()
                                .itemType(ItemTypes.BARRIER)
                                .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize("&c" + modifier.toString() + " Modifier &7{&c&lDISABLED&7}"))
                                .build()
                ));
            else
                builder.putElement(i / 2 * 9 + i % 2 * 8, new ActionableElement(
                        new RunnableAction(container, ActionType.NONE, "", c -> {
                            if (modifier.getCost(pokemon) <= Utils.getBal(player)) {
                                if (modifier.run(new ModifierData(pokemon, player, container))) {
                                    Utils.withdraw(player, modifier.getCost(pokemon));
                                    if (!player.getOpenInventory().isPresent())
                                        container.openState(player, "editor");
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(22)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .from((ItemStack) (Object) ItemPixelmonSprite.getPhoto(pokemon))
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, pokemon.getDisplayName()))
                                                    .build()
                                            )
                                    );
                                }
                            } else
                                Utils.sendPlayerError(player, "You can't afford this!");
                        }),
                        modifier.getItemStack(player, pokemon)
                ));
        }
        for (int i = 0; i < 45; i++)
            if (i % 9 != 0 && i % 9 != 8)
                builder.putElement(i, new Element(
                        ItemStack.builder()
                                .itemType(ItemTypes.STAINED_GLASS_PANE)
                                .add(Keys.DYE_COLOR, i % 9 == 1 || i % 9 == 7 || i / 9 == 2 ? DyeColors.WHITE : DyeColors.RED)
                                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                                .build()
                ));
        builder.putElement(22, new Element(
                ItemStack.builder()
                        .from((ItemStack) (Object) ItemPixelmonSprite.getPhoto(pokemon))
                        .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, pokemon.getDisplayName()))
                        .build()
        ));
        builder.putElement(31, new Element(
                ItemStack.builder()
                        .itemType(ItemTypes.STAINED_GLASS_PANE)
                        .add(Keys.DISPLAY_NAME, Text.EMPTY)
                        .build()
        ));
        builder.putElement(13, new Element(
                ItemStack.builder()
                        .itemType(ItemTypes.STAINED_GLASS_PANE)
                        .add(Keys.DISPLAY_NAME, Text.EMPTY)
                        .build()
        ));
        container.addState(builder.build("editor"));
        return container;
    }
}
