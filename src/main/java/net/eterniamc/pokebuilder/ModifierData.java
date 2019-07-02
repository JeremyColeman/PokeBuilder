package net.eterniamc.pokebuilder;

import com.codehusky.huskyui.StateContainer;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Created by Justin
 */
public class ModifierData {
    Pokemon pokemon;
    Player player;
    StateContainer gui;

    public ModifierData(Pokemon pokemon, Player player, StateContainer gui) {
        this.pokemon = pokemon;
        this.player = player;
        this.gui = gui;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public Player getPlayer() {
        return player;
    }

    public StateContainer getGui() {
        return gui;
    }
}
