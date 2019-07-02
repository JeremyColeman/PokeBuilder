package net.eterniamc.pokebuilder.modifiers;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonItemsHeld;
import net.eterniamc.pokebuilder.Configuration.Config;
import net.eterniamc.pokebuilder.ModifierData;
import org.spongepowered.api.item.ItemType;

/**
 * Created by Justin
 */
public class MaxHappinessModifier implements Modifier {

    @Override
    public boolean run(ModifierData data) {
        Pokemon pixelmon = data.getPokemon();
        pixelmon.setFriendship(255);
        return true;
    }

    @Override
    public ItemType getItem() {
        return (ItemType) PixelmonItemsHeld.choiceScarf;
    }

    @Override
    public double getCost(Pokemon pokemon) {
        return Config.maxHappinessModifierCost * getMultiplier(pokemon);
    }
}
