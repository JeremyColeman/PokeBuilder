package net.eterniamc.pokebuilder.modifiers;

import com.codehusky.huskyui.StateContainer;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.action.ActionType;
import com.codehusky.huskyui.states.action.runnable.RunnableAction;
import com.codehusky.huskyui.states.element.ActionableElement;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonItemsBadges;
import com.pixelmonmod.pixelmon.config.PixelmonItemsHeld;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.eterniamc.pokebuilder.Configuration.Config;
import net.eterniamc.pokebuilder.ModifierData;
import net.eterniamc.pokebuilder.Utils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Justin
 */
public class GrowthModifier implements Modifier {

    @Override
    public boolean run(ModifierData data) {
        Pokemon pixelmon = data.getPokemon();
        Player player = data.getPlayer();
        StateContainer container = data.getGui();
        if (container.hasState("growth"))
            container.removeState("growth");
        Page.PageBuilder natureModifier = Page.builder()
                .setAutoPaging(true)
                .setTitle(Text.of("Growth Modifier"))
                .setParent("editor")
                .setEmptyStack(Utils.empty());
        List<EnumGrowth> growths = new ArrayList<>(Arrays.asList(EnumGrowth.values()));
        growths.sort((growth1, growth2) -> growth1.scaleValue - growth2.scaleValue > 0 ? 1 : growth1.scaleValue - growth2.scaleValue < 0 ? -1 : 0);
        for (EnumGrowth growth : growths)
            natureModifier.addElement(new ActionableElement(
                            new RunnableAction(container, ActionType.CLOSE, "", context -> {
                                double cost = Config.growthModifierCost * (Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(p -> pixelmon.getSpecies() == p) || pixelmon.getSpecies() == EnumSpecies.Ditto ? Config.legendaryOrDittoMultiplier : 1);
                                if (!Utils.withdrawBalance(player, cost)) {
                                    Utils.sendPlayerError(player, "You can't afford this!");
                                    return;
                                }
                                pixelmon.setGrowth(growth);

                            }),
                            ItemStack.builder()
                                    .itemType((ItemType) PixelmonItemsBadges.voltageBadge)
                                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, growth.getLocalizedName()))
                                    .add(Keys.ITEM_LORE, Collections.singletonList(Text.of("Give this growth to your Pokemon")))
                                    .build()
                    )
            );
        container.addState(natureModifier.build("growth"));
        container.openState(player, "growth");
        return false;
    }

    @Override
    public ItemType getItem() {
        return (ItemType) PixelmonItemsHeld.dnaSplicers;
    }

    @Override
    public double getCost(Pokemon pokemon) {
        return Config.growthModifierCost * getMultiplier(pokemon);
    }
}
