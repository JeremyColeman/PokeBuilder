package net.eterniamc.pokebuilder.modifiers;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.eterniamc.pokebuilder.ModifierData;
import net.eterniamc.pokebuilder.PokeBuilder;
import net.eterniamc.pokebuilder.Utils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;

/**
 * Created by Justin
 */
public interface Modifier {
    boolean run(ModifierData data);

    ItemType getItem();

    double getCost(Pokemon pokemon);

    default String getName() {
        return Utils.fromCamelToDisplay(getClass().getSimpleName());
    }

    default ItemStack getItemStack(Player player, Pokemon pokemon) {
        return ItemStack.builder()
                .itemType(getItem())
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, getName()))
                .add(Keys.ITEM_LORE, Collections.singletonList(getCost(pokemon) == 0 ? Text.EMPTY : Text.of(
                        getCost(pokemon) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                        getCost(pokemon) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
                )))
                .build();
    }

    default double getMultiplier(Pokemon pokemon) {
        return getMultiplier(pokemon);
    }
}
