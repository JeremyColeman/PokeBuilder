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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    private double getCost(Pokemon pokemon, String type) {
        return getBaseCost(type) * getMultiplier(pokemon);
    }

    private double getBaseCost(String type) {
        switch (type) {
            case "hp":
                return Config.hpEvsCost;
            case "attack":
                return Config.attEvsCost;
            case "defence":
                return Config.defEvsCost;
            case "specialAttack":
                return Config.spAttEvsCost;
            case "specialDefence":
                return Config.spDefEvsCost;
            case "speed":
                return Config.speedEvsCost;
            default:
                return 0;
        }
    }

    private ItemType getGuiItem(String type) {
        switch (type) {
            case "hp":
                return (ItemType) PixelmonItemsHeld.powerBracer;
            case "attack":
                return (ItemType) PixelmonItemsHeld.powerBand;
            case "defence":
                return (ItemType) PixelmonItemsHeld.powerLens;
            case "specialAttack":
                return (ItemType) PixelmonItemsHeld.powerAnklet;
            case "specialDefence":
                return (ItemType) PixelmonItemsHeld.powerBelt;
            case "speed":
                return (ItemType) PixelmonItemsHeld.powerWeight;
            default:
                return ItemTypes.BARRIER;
        }
    }

    @Override
    public ItemType getItem() {
        return (ItemType) PixelmonItemsHeld.everStone;
    }

    @Override
    public boolean run(ModifierData data) {
        Pokemon pokemon = data.getPokemon();
        Player player = data.getPlayer();
        StateContainer container = data.getGui();
        if (container == null) {
            pokemon.getStats().evs.randomizeMaxEVs();
            return true;
        }
        if (container.hasState("evs")) {
            container.removeState("evs");
        }
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
        List<Field> fields = Arrays.stream(EVStore.class.getDeclaredFields()).filter(f -> f.getType() == int.class && !f.getName().contains("_")).collect(Collectors.toList());
        int a = 0;
        try {
            for (Field field : fields) {
                String type = field.getName();
                int i = a;
                builder.putElement(i, new Element(ItemStack.builder()
                        .itemType(getGuiItem(type))
                        .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, field.get(pokemon.getEVs()))))
                        .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, Utils.fromCamelToDisplay(type).replace("Hp", "HP") + " EVs"))
                        .build())
                );
                if (getCost(pokemon, type) > 0) {
                    builder.putElement(i + 2, new ActionableElement(
                                    new RunnableAction(container, ActionType.NONE, "", c -> {
                                        try {
                                            if (Utils.withdrawBalance(player, getCost(pokemon, type)) && (Integer) field.get(pokemon.getEVs()) + 1 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - Arrays.stream(pokemon.getEVs().getArray()).sum() > 0) {
                                                field.set(pokemon.getEVs(), ((Integer) field.get(pokemon.getEVs())) + 1);
                                                player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(i)).ifPresent(inv -> {
                                                            try {
                                                                inv.set(ItemStack.builder()
                                                                        .itemType(getGuiItem(type))
                                                                        .quantity((Integer) field.get(pokemon.getEVs()))
                                                                        .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, field.get(pokemon.getEVs()))))
                                                                        .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, Utils.fromCamelToDisplay(type).replace("Hp", "HP") + " EVs"))
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
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+1 " + Utils.fromCamelToDisplay(type).replace("Hp", "HP") + " EV"))
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
                                            if (Utils.withdrawBalance(player, getCost(pokemon, type) * 10) && (Integer) field.get(pokemon.getEVs()) + 10 <= EVStore.MAX_EVS && MAX_TOTAL_EVS - Arrays.stream(pokemon.getEVs().getArray()).sum() > 0) {
                                                field.set(pokemon.getEVs(), ((Integer) field.get(pokemon.getEVs())) + 10);
                                                player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(i)).ifPresent(inv -> {
                                                            try {
                                                                inv.set(ItemStack.builder()
                                                                        .itemType(getGuiItem(type))
                                                                        .quantity((Integer) field.get(pokemon.getEVs()))
                                                                        .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, field.get(pokemon.getEVs()))))
                                                                        .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, Utils.fromCamelToDisplay(type).replace("Hp", "HP") + " EVs"))
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
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "+10 " + Utils.fromCamelToDisplay(type).replace("Hp", "HP") + " EV"))
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                                    getCost(pokemon, type) * 10 > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                                    getCost(pokemon, type) * 10 + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                                            )))
                                            .build()
                            )
                    );
                }
                a += 9;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (Config.resetEvsCost > 0) {
            builder.putElement(24, new ActionableElement(
                    new RunnableAction(container, ActionType.NONE, "", c -> {
                        double cost = Config.resetEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pokemon.getSpecies() == p) || pokemon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1);
                        if (Utils.withdrawBalance(player, cost)) {
                            pokemon.getStats().evs = new EVStore();
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(45)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pokemon.getEVs().speed)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(36)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pokemon.getEVs().specialDefence)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(27)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pokemon.getEVs().specialAttack)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(18)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerLens)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pokemon.getEVs().defence)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(9)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBand)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pokemon.getEVs().attack)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pokemon.getEVs().hp)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP EVs"))
                                            .build()
                                    )
                            );
                        } else Utils.sendPlayerError(player, "You can't afford this!");
                    }),
                    ItemStack.builder()
                            .itemType((ItemType) PixelmonItems.potion)
                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Reset EVs"))
                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                    Config.resetEvsCost * getMultiplier(pokemon) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                    Config.resetEvsCost * getMultiplier(pokemon) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                            )))
                            .build()
            ));
        }
        if (Config.randomMaxEvsCost > 0) {
            builder.putElement(33, new ActionableElement(
                    new RunnableAction(container, ActionType.NONE, "", c -> {
                        double cost = Config.randomMaxEvsCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pokemon.getSpecies() == p) || pokemon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1);
                        if (Utils.withdrawBalance(player, cost)) {
                            pokemon.getEVs().randomizeMaxEVs();
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(45)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerWeight)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pokemon.getEVs().speed)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Speed EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(36)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBelt)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pokemon.getEVs().specialDefence)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Defence EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(27)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerAnklet)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pokemon.getEVs().specialAttack)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Special Attack EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(18)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerLens)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pokemon.getEVs().defence)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Defence EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(9)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBand)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pokemon.getEVs().attack)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Attack EVs"))
                                            .build()
                                    )
                            );
                            player.getOpenInventory().map(inv1 -> Lists.<Inventory>newArrayList(inv1.slots()).get(0)).ifPresent(inv ->
                                    inv.set(ItemStack.builder()
                                            .itemType((ItemType) PixelmonItemsHeld.powerBracer)
                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.WHITE, pokemon.getEVs().hp)))
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "HP EVs"))
                                            .build()
                                    )
                            );
                        } else Utils.sendPlayerError(player, "You can't afford this!");
                    }),
                    ItemStack.builder()
                            .itemType((ItemType) PixelmonItems.maxPotion)
                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Randomly Max EVs"))
                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of(
                                    Config.randomMaxEvsCost * getMultiplier(pokemon) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                                    Config.randomMaxEvsCost * getMultiplier(pokemon) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                            )))
                            .build()
            ));
        }
        container.addState(builder.build("evs"));
        container.openState(player, "evs");
        return false;
    }
}