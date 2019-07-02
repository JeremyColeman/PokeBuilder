package net.eterniamc.pokebuilder.modifiers;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.eterniamc.pokebuilder.Configuration.Config;
import net.eterniamc.pokebuilder.ModifierData;
import net.eterniamc.pokebuilder.Utils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;

/**
 * Created by Justin
 */
public class ShinyModifier implements Modifier {

    @Override
    public boolean run(ModifierData data) {
        Pokemon pixelmon = data.getPokemon();
        Player player = data.getPlayer();
        if (pixelmon.isShiny()) {
            Utils.sendPlayerError(player, "Pokemon is already shiny");
            return false;
        } else {
            pixelmon.setShiny(true);
        }
        return true;
    }

    @Override
    public double getCost(Pokemon pokemon) {
        return Config.shinyModifierCost * getMultiplier(pokemon);
    }

    @Override
    public ItemType getItem() {
        return ItemTypes.NETHER_STAR;
    }
}
