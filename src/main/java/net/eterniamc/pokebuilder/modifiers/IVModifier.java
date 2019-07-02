package net.eterniamc.pokebuilder.modifiers;

import com.codehusky.huskyui.StateContainer;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.action.ActionType;
import com.codehusky.huskyui.states.action.runnable.RunnableAction;
import com.codehusky.huskyui.states.element.ActionableElement;
import com.codehusky.huskyui.states.element.Element;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.config.PixelmonItemsHeld;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.eterniamc.pokebuilder.Configuration.Config;
import net.eterniamc.pokebuilder.ModifierData;
import net.eterniamc.pokebuilder.PokeBuilder;
import net.eterniamc.pokebuilder.Utils;
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

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Justin
 * P.S. This code is shit and so is HuskyUI
 */
public class IVModifier implements Modifier {

    @Override
    public String getName() {
        return "IV Modifier";
    }

    @Override
    public ItemType getItem() {
        return (ItemType) PixelmonItemsHeld.powerLens;
    }

    @Override
    public double getCost(Pokemon pokemon) {
        return 0; // this doesn't matter
    }

    @Override
    public boolean run(ModifierData data) {
        Pokemon pixelmon = data.getPokemon();
        Player player = data.getPlayer();
        StateContainer container = data.getGui();
        if (container.hasState("ivs"))
            container.removeState("ivs");
        Page.PageBuilder builder = Page.builder()
                .setTitle(Text.of("IV Modifier"))
                .setInventoryDimension(InventoryDimension.of(9, 6))
                .setAutoPaging(false);
        for (int i = 0; i < 54; i++) {
            builder.addElement(new Element(ItemStack.builder()
                    .itemType(ItemTypes.STAINED_GLASS_PANE)
                    .add(Keys.DYE_COLOR, (i % 9 >= 5 && i % 9 <= 7) && (i / 9 >= 1 && i / 9 <= 4) ? DyeColors.RED : DyeColors.WHITE)
                    .add(Keys.DISPLAY_NAME, Text.EMPTY)
                    .build())
            );
        }
        builder.putElement(0, new Element(ItemStack.builder()
                .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                .quantity(pixelmon.getIVs().hp)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP IVs"))
                .build())
        );
        if (Config.hpIvsCost > 0) {
            builder.putElement(2, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.hpIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getIVs().hp + 1 <= 31) {
                                    pixelmon.getIVs().hp++;
                                    Utils.withdraw(player, Config.hpIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                                                    .quantity(pixelmon.getIVs().hp)
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP IVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 HP IV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.hpIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.hpIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
            builder.putElement(3, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.hpIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getIVs().hp + 10 <= 31) {
                                    pixelmon.getIVs().hp += 10;
                                    Utils.withdraw(player, Config.hpIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                                                    .quantity(pixelmon.getIVs().hp)
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP IVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .quantity(10)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 HP IV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.hpIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.hpIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
        }
        builder.putElement(9, new Element(ItemStack.builder()
                .itemType((ItemType) PixelmonItemsHeld.powerBand)
                .quantity(pixelmon.getIVs().attack)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack IVs"))
                .build())
        );
        if (Config.attIvsCost > 0) {
            builder.putElement(11, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.attIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getIVs().attack + 1 <= 31) {
                                    pixelmon.getIVs().attack++;
                                    Utils.withdraw(player, Config.attIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(9)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerBand)
                                                    .quantity(pixelmon.getIVs().attack)
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack IVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 Attack IV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.attIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.attIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
            builder.putElement(12, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.attIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getIVs().attack + 10 <= 31) {
                                    pixelmon.getIVs().attack += 10;
                                    Utils.withdraw(player, Config.attIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(9)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerBand)
                                                    .quantity(pixelmon.getIVs().attack)
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack IVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .quantity(10)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 Attack IVs"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.attIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.attIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
        }
        builder.putElement(18, new Element(ItemStack.builder()
                .itemType((ItemType) PixelmonItemsHeld.powerLens)
                .quantity(pixelmon.getIVs().defence)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence IVs"))
                .build())
        );
        if (Config.defIvsCost > 0) {
            builder.putElement(20, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.defIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getIVs().defence + 1 <= 31) {
                                    pixelmon.getIVs().defence++;
                                    Utils.withdraw(player, Config.defIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(18)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerLens)
                                                    .quantity(pixelmon.getIVs().defence)
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence IVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 Defence IV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.defIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.defIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
            builder.putElement(21, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.defIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getIVs().defence + 10 <= 31) {
                                    pixelmon.getIVs().defence += 10;
                                    Utils.withdraw(player, Config.defIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(18)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerLens)
                                                    .quantity(pixelmon.getIVs().defence)
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence IVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .quantity(10)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 Defence IVs"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.defIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.defIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
        }
        builder.putElement(27, new Element(ItemStack.builder()
                .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                .quantity(pixelmon.getIVs().specialAttack)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack IVs"))
                .build())
        );
        if (Config.spAttIvsCost > 0) {
            builder.putElement(29, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.spAttIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getIVs().specialAttack + 1 <= 31) {
                                    pixelmon.getIVs().specialAttack++;
                                    Utils.withdraw(player, Config.spAttIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(27)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                                                    .quantity(pixelmon.getIVs().specialAttack)
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack IVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 Special Attack IV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.spAttIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.spAttIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
            builder.putElement(30, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.spAttIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getIVs().specialAttack + 10 <= 31) {
                                    pixelmon.getIVs().specialAttack += 10;
                                    Utils.withdraw(player, Config.spAttIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(27)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                                                    .quantity(pixelmon.getIVs().specialAttack)
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack IVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .quantity(10)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 Special Attack IVs"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.spAttIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.spAttIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
        }
        builder.putElement(36, new Element(ItemStack.builder()
                .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                .quantity(pixelmon.getIVs().specialDefence)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence IVs"))
                .build())
        );
        if (Config.spDefIvsCost > 0) {
            builder.putElement(38, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.spDefIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getIVs().specialDefence + 1 <= 31) {
                                    pixelmon.getIVs().specialDefence++;
                                    Utils.withdraw(player, Config.spDefIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(36)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                                                    .quantity(pixelmon.getIVs().specialDefence)
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence IVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 Special Defence IV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.spDefIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.spDefIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
            builder.putElement(39, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.spDefIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getIVs().specialDefence + 10 <= 31) {
                                    pixelmon.getIVs().specialDefence += 10;
                                    Utils.withdraw(player, Config.spDefIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(36)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                                                    .quantity(pixelmon.getIVs().specialAttack)
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence IVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .quantity(10)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 Special Defence IVs"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.spDefIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.spDefIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
        }
        builder.putElement(45, new Element(ItemStack.builder()
                .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                .quantity(pixelmon.getIVs().speed)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed IVs"))
                .build())
        );
        if (Config.speedIvsCost > 0) {
            builder.putElement(47, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.speedIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getIVs().speed + 1 <= 31) {
                                    pixelmon.getIVs().speed++;
                                    Utils.withdraw(player, Config.speedIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(45)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                                                    .quantity(pixelmon.getIVs().speed)
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed IVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 Speed IV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.speedIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.speedIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
            builder.putElement(48, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.speedIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getIVs().speed + 10 <= 31) {
                                    pixelmon.getIVs().speed += 10;
                                    Utils.withdraw(player, Config.speedIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(45)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                                                    .quantity(pixelmon.getIVs().specialAttack)
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed IVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .quantity(10)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 Speed IVs"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.speedIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.speedIvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
        }
        if (Config.rerollIvsCost > 0)
            builder.putElement(24, new ActionableElement(
                    new RunnableAction(container, ActionType.NONE, "", c -> {
                        if (Utils.getBal(player) >= Config.rerollIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1)) {
                            Utils.withdraw(player, Config.rerollIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                            pixelmon.getStats().ivs = IVStore.CreateNewIVs();
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(45)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                                            .quantity(pixelmon.getIVs().specialAttack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(36)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                                            .quantity(pixelmon.getIVs().specialAttack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(27)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                                            .quantity(pixelmon.getIVs().specialAttack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(18)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerLens)
                                            .quantity(pixelmon.getIVs().defence)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(9)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBand)
                                            .quantity(pixelmon.getIVs().attack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                                            .quantity(pixelmon.getIVs().hp)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP IVs"))
                                            .build()
                                    )
                            );
                        }
                    }),
                    ItemStack.builder()
                            .itemType((ItemType) PixelmonItems.potion)
                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Reroll IVs"))
                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                    Config.rerollIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                    Config.rerollIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                            )))
                            .build()
            ));
        if (Config.maxIvsCost > 0)
            builder.putElement(33, new ActionableElement(
                    new RunnableAction(container, ActionType.NONE, "", c -> {
                        if (Utils.getBal(player) >= Config.maxIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1)) {
                            Utils.withdraw(player, Config.maxIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                            pixelmon.getIVs().maximizeIVs();
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(45)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                                            .quantity(pixelmon.getIVs().specialAttack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(36)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                                            .quantity(pixelmon.getIVs().specialAttack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(27)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                                            .quantity(pixelmon.getIVs().specialAttack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(18)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerLens)
                                            .quantity(pixelmon.getIVs().defence)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(9)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBand)
                                            .quantity(pixelmon.getIVs().attack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                                            .quantity(pixelmon.getIVs().hp)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP IVs"))
                                            .build()
                                    )
                            );
                        }
                    }),
                    ItemStack.builder()
                            .itemType((ItemType) PixelmonItems.maxPotion)
                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Max IVs"))
                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                    Config.maxIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                    Config.maxIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                            )))
                            .build()
            ));
        container.addState(builder.build("ivs"));
        container.openState(player, "ivs");
        return false;
    }
}
