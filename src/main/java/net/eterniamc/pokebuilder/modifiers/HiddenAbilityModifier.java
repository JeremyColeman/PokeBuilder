package net.eterniamc.pokebuilder.modifiers;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import net.eterniamc.pokebuilder.Configuration.Config;
import net.eterniamc.pokebuilder.ModifierData;
import net.eterniamc.pokebuilder.Utils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;

/**
 * Created by Justin
 */
public class HiddenAbilityModifier implements Modifier {

    @Override
    public boolean run(ModifierData data) {
        Pokemon pixelmon = data.getPokemon();
        Player player = data.getPlayer();
        if (pixelmon.getBaseStats().abilities.length == 3 && pixelmon.getBaseStats().abilities[2] != null) {
            pixelmon.setAbility(pixelmon.getBaseStats().abilities[2]);
        } else {
            Utils.sendPlayerError(player, "This Pokemon doesn't have a hidden ability!");
            return false;
        }
        return true;
    }

    @Override
    public ItemType getItem() {
        return (ItemType) PixelmonItems.abilityCapsule;
    }

    @Override
    public double getCost(Pokemon pokemon) {
        return Config.hiddenAbilityModifierCost * getMultiplier(pokemon);
    }
}
