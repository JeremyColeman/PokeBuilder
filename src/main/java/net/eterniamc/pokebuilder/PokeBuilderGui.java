package net.eterniamc.pokebuilder;

import com.codehusky.huskyui.StateContainer;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.action.ActionType;
import com.codehusky.huskyui.states.action.runnable.RunnableAction;
import com.codehusky.huskyui.states.element.ActionableElement;
import com.codehusky.huskyui.states.element.Element;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonItemsPokeballs;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.items.ItemPixelmonSprite;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.eterniamc.pokebuilder.Configuration.Config;
import net.eterniamc.pokebuilder.modifiers.Modifier;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Justin
 */
public class PokeBuilderGui {
    public static void openGui(Player player) {
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
                if (Config.blacklistedPokemon.stream().noneMatch(s -> s.equalsIgnoreCase(pixelmon.getSpecies().getPokemonName()))) {
                    main.addElement(new ActionableElement(
                                    new RunnableAction(container, ActionType.NONE, "", c -> createEditorPage(container, pixelmon, player).openState(player, "editor")),
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
            } else if (Config.pokemonCost >= 0 && Config.legendaryCost >= 0) {
                main.addElement(new ActionableElement(
                        new RunnableAction(container, ActionType.CLOSE, "", c -> {
                            ChatGuiHelper.addGUI("Enter the name of the pokemon you want, regular pokemon cost $" + Config.pokemonCost + " and legendaries cost $" + Config.legendaryCost, player, text -> {
                                    EnumSpecies pokemon = EnumSpecies.getFromNameAnyCase(text.toPlain());
                                    if (pokemon == null) {
                                        Utils.sendPlayerError(player, "Invalid Pokemon");
                                        return;
                                    } else if (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> p == pokemon)) {
                                        if (Config.legendaryCost == -1) {
                                            Utils.sendPlayerError(player, "Creation of legendary Pokémon is disabled!");
                                            return;
                                        }
                                        if (!Utils.withdrawBalance(player, Config.legendaryCost)) {
                                            Utils.sendPlayerError(player, "You can't afford this!");
                                            return;
                                        }
                                    } else {
                                        if (Config.pokemonCost == -1) {
                                            Utils.sendPlayerError(player, "Creation of normal Pokémon is disabled!");
                                            return;
                                        }
                                        if (!Utils.withdrawBalance(player, Config.pokemonCost)) {
                                            Utils.sendPlayerError(player, "You can't afford this!");
                                            return;
                                        }
                                    }
                                    Pokemon pixelmon1 = Pixelmon.pokemonFactory.create(pokemon);
                                    pixelmon1.setOriginalTrainer((EntityPlayerMP) player);
                                    store.add(pixelmon1);
                                });
                        }),
                        ItemStack.builder()
                                .itemType((ItemType) PixelmonItemsPokeballs.pokeBall)
                                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Create a new Pokemon"))
                                .add(Keys.ITEM_LORE, Arrays.asList(
                                        Text.of(Config.pokemonCost > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                                "Regular Pokemon: " + Config.pokemonCost + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                        ),
                                        Text.of(
                                                Config.legendaryCost > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                                "Legendaries: " + Config.legendaryCost + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                        )
                                ))
                                .build()
                ));
            } else {
                main.addElement(
                        new Element(
                                ItemStack.builder()
                                        .itemType((ItemType) PixelmonItemsPokeballs.pokeBall)
                                        .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "There is nothing in this slot"))
                                        .build()
                        )
                );
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
    }

    private static StateContainer createEditorPage(StateContainer container, Pokemon pokemon, Player player) {
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
        for (int i = 0; i < PokeBuilder.getModifiers().size(); i++) {
            Modifier modifier = PokeBuilder.getModifiers().get(i);
            if (modifier.getCost(pokemon) < 0) {
                builder.putElement(i / 2 * 9 + i % 2 * 8, new Element(
                        ItemStack.builder()
                                .itemType(ItemTypes.BARRIER)
                                .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize("&c" + modifier.toString() + " Modifier &7{&c&lDISABLED&7}"))
                                .build()
                ));
            } else {
                builder.putElement(i / 2 * 9 + i % 2 * 8, new ActionableElement(
                        new RunnableAction(container, ActionType.NONE, "", c -> {
                            if (!Utils.withdrawBalance(player, modifier.getCost(pokemon))) {
                                if (modifier.run(new ModifierData(pokemon, player, container))) {
                                    Utils.sendPlayerMessage(player, modifier.getCost(pokemon) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain() + " have been withdrawn from your account");
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
