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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Justin
 */
public class IVModifier implements Modifier {

    @Override
    public String getName() {
        return "IV Modifier";
    }

    @Override
    public double getCost(Pokemon pokemon) {
        return 0;
    }

    private double getCost(Pokemon pokemon, String type) {
        return getBaseCost(type) * getMultiplier(pokemon);
    }

    private double getBaseCost(String type) {
        switch (type) {
            case "hp":
                return Config.hpIvsCost;
            case "attack":
                return Config.attIvsCost;
            case "defence":
                return Config.defIvsCost;
            case "specialAttack":
                return Config.spAttIvsCost;
            case "specialDefence":
                return Config.spDefIvsCost;
            case "speed":
                return Config.speedIvsCost;
            default:
                return 0;
        }
    }

    private ItemType getGuiItem(String type) {
        switch (type) {
            case "hp":
                return (ItemType) PixelmonItemsHeld.powerBracer;
            case "attack":
                return (ItemType) PixelmonItemsHeld.powerLens;
            case "defence":
                return (ItemType) PixelmonItemsHeld.powerBelt;
            case "specialAttack":
                return (ItemType) PixelmonItemsHeld.powerBand;
            case "specialDefence":
                return (ItemType) PixelmonItemsHeld.powerWeight;
            case "speed":
                return (ItemType) PixelmonItemsHeld.powerAnklet;
            default:
                return ItemTypes.BARRIER;
        }
    }

    @Override
    public ItemType getItem() {
        return (ItemType) PixelmonItemsHeld.powerLens;
    }

    @Override
    public boolean run(ModifierData data) {
        Pokemon pokemon = data.getPokemon();
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
        List<Field> fields = Arrays.stream(IVStore.class.getDeclaredFields()).filter(f -> f.getType() == int.class && !f.getName().contains("_")).collect(Collectors.toList());
        int i = 0;
        try {
            for (Field field : fields) {
                String type = field.getName();
                i++;
                builder.putElement(i, new Element(ItemStack.builder()
                        .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                        .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, field.get(pokemon.getEVs()))))
                        .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, Utils.fromCamelToDisplay(type).replace("Hp", "HP") + " IVs"))
                        .build())
                );
                if (getCost(pokemon, type) > 0) {
                    builder.putElement(i + 2, new ActionableElement(
                                    new RunnableAction(container, ActionType.NONE, "", c -> {
                                        try {
                                            if (!Utils.withdrawBalance(player, getCost(pokemon, type)) && (Integer) field.get(pokemon.getEVs()) + 1 <= IVStore.MAX_IVS) {
                                                field.set(pokemon.getEVs(), ((Integer) field.get(pokemon.getEVs())) + 1);
                                                player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv -> {
                                                            try {
                                                                inv.set(ItemStack.builder()
                                                                        .itemType(getGuiItem(type))
                                                                        .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, field.get(pokemon.getEVs()))))
                                                                        .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, Utils.fromCamelToDisplay(type).replace("Hp", "HP") + " IVs"))
                                                                        .build()
                                                                );
                                                            } catch (IllegalAccessException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                );
                                            } else Utils.sendPlayerError(player, "You can't afford this!");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }),
                                    ItemStack.builder()
                                            .itemType(ItemTypes.CONCRETE)
                                            .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 " + Utils.fromCamelToDisplay(type).replace("Hp", "HP") + " IV"))
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                                    getCost(pokemon, type) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                                    getCost(pokemon, type) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                            )))
                                            .build()
                            )
                    );
                    builder.putElement(i + 3, new ActionableElement(
                                    new RunnableAction(container, ActionType.NONE, "", c -> {
                                        try {
                                            if (Utils.withdrawBalance(player, getCost(pokemon, type)) && (Integer) field.get(pokemon.getEVs()) + 10 <= IVStore.MAX_IVS) {
                                                field.set(pokemon.getEVs(), ((Integer) field.get(pokemon.getEVs())) + 1);
                                                player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv -> {
                                                            try {
                                                                inv.set(ItemStack.builder()
                                                                        .itemType(getGuiItem(type))
                                                                        .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, field.get(pokemon.getEVs()))))
                                                                        .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, Utils.fromCamelToDisplay(type).replace("Hp", "HP") + " IVs"))
                                                                        .build()
                                                                );
                                                            } catch (IllegalAccessException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                );
                                            } else Utils.sendPlayerError(player, "You can't afford this!");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }),
                                    ItemStack.builder()
                                            .itemType(ItemTypes.CONCRETE)
                                            .quantity(10)
                                            .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 " + Utils.fromCamelToDisplay(type).replace("Hp", "HP") + " IV"))
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                                    getCost(pokemon, type) * 10 > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                                    getCost(pokemon, type) * 10 + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                            )))
                                            .build()
                            )
                    );
                }
                i += 9;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (Config.rerollIvsCost > 0) {
            builder.putElement(24, new ActionableElement(
                    new RunnableAction(container, ActionType.NONE, "", c -> {
                        double cost = Config.rerollIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pokemon.getSpecies() == p) || pokemon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1);
                        if (Utils.withdrawBalance(player, cost)) {
                            pokemon.getStats().ivs = IVStore.CreateNewIVs();
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(45)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                                            .quantity(pokemon.getIVs().specialAttack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(36)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                                            .quantity(pokemon.getIVs().specialAttack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(27)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                                            .quantity(pokemon.getIVs().specialAttack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(18)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerLens)
                                            .quantity(pokemon.getIVs().defence)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(9)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBand)
                                            .quantity(pokemon.getIVs().attack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                                            .quantity(pokemon.getIVs().hp)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP IVs"))
                                            .build()
                                    )
                            );
                        } else Utils.sendPlayerError(player, "You can't afford this!");
                    }),
                    ItemStack.builder()
                            .itemType((ItemType) PixelmonItems.potion)
                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Reroll IVs"))
                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                    Config.rerollIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pokemon.getSpecies() == p) || pokemon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                    Config.rerollIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pokemon.getSpecies() == p) || pokemon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                            )))
                            .build()
            ));
        }
        if (Config.maxIvsCost > 0) {
            builder.putElement(33, new ActionableElement(
                    new RunnableAction(container, ActionType.NONE, "", c -> {
                        double cost = Config.maxIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pokemon.getSpecies() == p) || pokemon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1);
                        if (Utils.withdrawBalance(player, cost)) {
                            pokemon.getIVs().maximizeIVs();
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(45)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                                            .quantity(pokemon.getIVs().specialAttack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(36)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                                            .quantity(pokemon.getIVs().specialAttack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(27)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                                            .quantity(pokemon.getIVs().specialAttack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(18)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerLens)
                                            .quantity(pokemon.getIVs().defence)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(9)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBand)
                                            .quantity(pokemon.getIVs().attack)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack IVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                                            .quantity(pokemon.getIVs().hp)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP IVs"))
                                            .build()
                                    )
                            );
                        } else Utils.sendPlayerError(player, "You can't afford this!");
                    }),
                    ItemStack.builder()
                            .itemType((ItemType) PixelmonItems.maxPotion)
                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Max IVs"))
                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                    Config.maxIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pokemon.getSpecies() == p) || pokemon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                    Config.maxIvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pokemon.getSpecies() == p) || pokemon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                            )))
                            .build()
            ));
        }
        container.addState(builder.build("ivs"));
        container.openState(player, "ivs");
        return false;
    }
}