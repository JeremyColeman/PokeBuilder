package net.eterniamc.pokebuilder.modifiers;

import com.codehusky.huskyui.StateContainer;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.action.ActionType;
import com.codehusky.huskyui.states.action.runnable.RunnableAction;
import com.codehusky.huskyui.states.element.ActionableElement;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonItemsPokeballs;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
import net.eterniamc.pokebuilder.Configuration.Config;
import net.eterniamc.pokebuilder.ModifierData;
import net.eterniamc.pokebuilder.Utils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Justin
 */
public class PokeballModifier implements Modifier {

    @Override
    public boolean run(ModifierData data) {
        Pokemon pixelmon = data.getPokemon();
        Player player = data.getPlayer();
        StateContainer container = data.getGui();
        if (container.hasState("pokeball"))
            container.removeState("pokeball");
        Page.PageBuilder natureModifier = Page.builder()
                .setAutoPaging(true)
                .setTitle(Text.of("Pokeball Modifier"))
                .setParent("editor")
                .setEmptyStack(Utils.empty());
        for (EnumPokeballs ball : EnumPokeballs.values())
            natureModifier.addElement(new ActionableElement(
                            new RunnableAction(container, ActionType.CLOSE, "", context -> {
                                pixelmon.setCaughtBall(ball);
                                Utils.withdraw(player, Config.pokeballModifierCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1));

                            }),
                            ItemStack.builder()
                                    .itemType((ItemType) ball.getItem())
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, Utils.fromCamelToDisplay(ball.name())))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of("Set the ball your Pokemon is in to this")))
                                    .build()
                    )
            );
        container.addState(natureModifier.build("pokeball"));
        container.openState(player, "pokeball");
        return false;
    }

    @Override
    public ItemType getItem() {
        return (ItemType) PixelmonItemsPokeballs.pokeBall;
    }

    @Override
    public double getCost(Pokemon pokemon) {
        return Config.pokeballModifierCost * getMultiplier(pokemon);
    }
}
