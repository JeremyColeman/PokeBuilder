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
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
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

import static com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore.MAX_TOTAL_EVS;

/**
 * Created by Justin
 */
public class EVModifier implements Modifier {

    @Override
    public String getName() {
        return "EV Modifier";
    }

    @Override
    public double getCost(Pokemon pokemon) {
        return 0;
    }

    @Override
    public ItemType getItem() {
        return (ItemType) PixelmonItemsHeld.everStone;
    }

    @Override
    public boolean run(ModifierData data) {
        Pokemon pixelmon = data.getPokemon();
        Player player = data.getPlayer();
        StateContainer container = data.getGui();
        if (container.hasState("evs"))
            container.removeState("evs");
        Page.PageBuilder builder = Page.builder()
                .setTitle(Text.of("EV Modifier"))
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
                .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().hp)))
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP EVs"))
                .build())
        );
        if (Config.hpEvsCost > 0) {
            builder.putElement(2, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.hpEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getEVs().hp + 1 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - pixelmon.getEVs().hp - pixelmon.getEVs().attack - pixelmon.getEVs().defence - pixelmon.getEVs().specialAttack - pixelmon.getEVs().specialDefence - pixelmon.getEVs().speed > 0) {
                                    pixelmon.getEVs().hp++;
                                    Utils.withdraw(player, Config.hpEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().hp)))
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP EVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 HP EV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.hpEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.hpEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
            builder.putElement(3, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.hpEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getEVs().hp + 10 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - pixelmon.getEVs().hp - pixelmon.getEVs().attack - pixelmon.getEVs().defence - pixelmon.getEVs().specialAttack - pixelmon.getEVs().specialDefence - pixelmon.getEVs().speed > 0) {
                                    pixelmon.getEVs().hp += 10;
                                    Utils.withdraw(player, Config.hpEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().hp)))
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP EVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .quantity(10)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 HP EV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.hpEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.hpEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
        }
        builder.putElement(9, new Element(ItemStack.builder()
                .itemType((ItemType) PixelmonItemsHeld.powerBand)
                .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().attack)))
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack EVs"))
                .build())
        );
        if (Config.attEvsCost > 0) {
            builder.putElement(11, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.attEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getEVs().attack + 1 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - pixelmon.getEVs().hp - pixelmon.getEVs().attack - pixelmon.getEVs().defence - pixelmon.getEVs().specialAttack - pixelmon.getEVs().specialDefence - pixelmon.getEVs().speed > 0) {
                                    pixelmon.getEVs().attack++;
                                    Utils.withdraw(player, Config.attEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(9)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerBand)
                                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().attack)))
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack EVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 Attack EV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.attEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.attEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
            builder.putElement(12, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.attEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getEVs().attack + 10 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - pixelmon.getEVs().hp - pixelmon.getEVs().attack - pixelmon.getEVs().defence - pixelmon.getEVs().specialAttack - pixelmon.getEVs().specialDefence - pixelmon.getEVs().speed > 0) {
                                    pixelmon.getEVs().attack += 10;
                                    Utils.withdraw(player, Config.attEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(9)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerBand)
                                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().attack)))
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack EVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .quantity(10)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 Attack EVs"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.attEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.attEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
        }
        builder.putElement(18, new Element(ItemStack.builder()
                .itemType((ItemType) PixelmonItemsHeld.powerLens)
                .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().defence)))
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence EVs"))
                .build())
        );
        if (Config.defEvsCost > 0) {
            builder.putElement(20, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.defEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getEVs().defence + 1 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - pixelmon.getEVs().hp - pixelmon.getEVs().attack - pixelmon.getEVs().defence - pixelmon.getEVs().specialAttack - pixelmon.getEVs().specialDefence - pixelmon.getEVs().speed > 0) {
                                    pixelmon.getEVs().defence++;
                                    Utils.withdraw(player, Config.defEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(18)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerLens)
                                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().defence)))
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence EVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 Defence EV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.defEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.defEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
            builder.putElement(21, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.defEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getEVs().defence + 10 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - pixelmon.getEVs().hp - pixelmon.getEVs().attack - pixelmon.getEVs().defence - pixelmon.getEVs().specialAttack - pixelmon.getEVs().specialDefence - pixelmon.getEVs().speed > 0) {
                                    pixelmon.getEVs().defence += 10;
                                    Utils.withdraw(player, Config.defEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(18)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerLens)
                                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().defence)))
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence EVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .quantity(10)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 Defence EVs"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.defEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.defEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
        }
        builder.putElement(27, new Element(ItemStack.builder()
                .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().specialAttack)))
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack EVs"))
                .build())
        );
        if (Config.spAttEvsCost > 0) {
            builder.putElement(29, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.spAttEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getEVs().specialAttack + 1 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - pixelmon.getEVs().hp - pixelmon.getEVs().attack - pixelmon.getEVs().defence - pixelmon.getEVs().specialAttack - pixelmon.getEVs().specialDefence - pixelmon.getEVs().speed > 0) {
                                    pixelmon.getEVs().specialAttack++;
                                    Utils.withdraw(player, Config.spAttEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(27)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().specialAttack)))
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack EVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 Special Attack EV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.spAttEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.spAttEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
            builder.putElement(30, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.spAttEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getEVs().specialAttack + 10 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - pixelmon.getEVs().hp - pixelmon.getEVs().attack - pixelmon.getEVs().defence - pixelmon.getEVs().specialAttack - pixelmon.getEVs().specialDefence - pixelmon.getEVs().speed > 0) {
                                    pixelmon.getEVs().specialAttack += 10;
                                    Utils.withdraw(player, Config.spAttEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(27)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().specialAttack)))
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack EVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .quantity(10)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 Special Attack EVs"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.spAttEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.spAttEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
        }
        builder.putElement(36, new Element(ItemStack.builder()
                .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().specialDefence)))
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence EVs"))
                .build())
        );
        if (Config.spDefEvsCost > 0) {
            builder.putElement(38, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.spDefEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getEVs().specialDefence + 1 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - pixelmon.getEVs().hp - pixelmon.getEVs().attack - pixelmon.getEVs().defence - pixelmon.getEVs().specialAttack - pixelmon.getEVs().specialDefence - pixelmon.getEVs().speed > 0) {
                                    pixelmon.getEVs().specialDefence++;
                                    Utils.withdraw(player, Config.spDefEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(36)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().specialDefence)))
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence EVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 Special Defence EV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.spDefEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.spDefEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
            builder.putElement(39, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.spDefEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getEVs().specialDefence + 10 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - pixelmon.getEVs().hp - pixelmon.getEVs().attack - pixelmon.getEVs().defence - pixelmon.getEVs().specialAttack - pixelmon.getEVs().specialDefence - pixelmon.getEVs().speed > 0) {
                                    pixelmon.getEVs().specialDefence += 10;
                                    Utils.withdraw(player, Config.spDefEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(36)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().specialDefence)))
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence EVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .quantity(10)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 Special Defence EVs"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.spDefEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.spDefEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
        }
        builder.putElement(45, new Element(ItemStack.builder()
                .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().speed)))
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed EVs"))
                .build())
        );
        if (Config.speedEvsCost > 0) {
            builder.putElement(47, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.speedEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getEVs().speed + 1 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - pixelmon.getEVs().hp - pixelmon.getEVs().attack - pixelmon.getEVs().defence - pixelmon.getEVs().specialAttack - pixelmon.getEVs().specialDefence - pixelmon.getEVs().speed > 0) {
                                    pixelmon.getEVs().speed++;
                                    Utils.withdraw(player, Config.speedEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(45)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().speed)))
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed EVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 Speed EV"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.speedEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.speedEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
            builder.putElement(48, new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", c -> {
                                if (Utils.getBal(player) >= Config.speedEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) && pixelmon.getEVs().speed + 10 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - pixelmon.getEVs().hp - pixelmon.getEVs().attack - pixelmon.getEVs().defence - pixelmon.getEVs().specialAttack - pixelmon.getEVs().specialDefence - pixelmon.getEVs().speed > 0) {
                                    pixelmon.getEVs().speed += 10;
                                    Utils.withdraw(player, Config.speedEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                                    player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(45)).ifPresent(inv ->
                                            inv.set(ItemStack.builder()
                                                    .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().speed)))
                                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed EVs"))
                                                    .build()
                                            )
                                    );
                                }
                            }),
                            ItemStack.builder()
                                    .itemType(ItemTypes.CONCRETE)
                                    .quantity(10)
                                    .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 Speed EVs"))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                            Config.speedEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                            Config.speedEvsCost * 10 * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                    )))
                                    .build()
                    )
            );
        }
        if (Config.resetEvsCost > 0)
            builder.putElement(24, new ActionableElement(
                    new RunnableAction(container, ActionType.NONE, "", c -> {
                        if (Utils.getBal(player) >= Config.resetEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1)) {
                            Utils.withdraw(player, Config.resetEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                            pixelmon.getStats().evs = new EVStore();
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(45)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().speed)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(36)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().specialDefence)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(27)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().specialAttack)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(18)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerLens)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().defence)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(9)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBand)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().attack)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().hp)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP EVs"))
                                            .build()
                                    )
                            );
                        }
                    }),
                    ItemStack.builder()
                            .itemType((ItemType) PixelmonItems.potion)
                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Reset EVs"))
                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                    Config.resetEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                    Config.resetEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                            )))
                            .build()
            ));
        if (Config.randomMaxEvsCost > 0)
            builder.putElement(33, new ActionableElement(
                    new RunnableAction(container, ActionType.NONE, "", c -> {
                        if (Utils.getBal(player) >= Config.randomMaxEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1)) {
                            Utils.withdraw(player, Config.randomMaxEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));
                            pixelmon.getEVs().randomizeMaxEVs();
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(45)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().speed)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(36)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().specialDefence)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(27)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().specialAttack)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(18)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerLens)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().defence)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(9)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBand)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().attack)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pixelmon.getEVs().hp)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP EVs"))
                                            .build()
                                    )
                            );
                        }
                    }),
                    ItemStack.builder()
                            .itemType((ItemType) PixelmonItems.maxPotion)
                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Randomly Max EVs"))
                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                    Config.randomMaxEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                    Config.randomMaxEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                            )))
                            .build()
            ));
        container.addState(builder.build("evs"));
        container.openState(player, "evs");
        return false;
    }
}
