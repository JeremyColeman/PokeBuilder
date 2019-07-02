package net.eterniamc.pokebuilder.modifiers;

import com.codehusky.huskyui.StateContainer;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.action.ActionType;
import com.codehusky.huskyui.states.action.runnable.RunnableAction;
import com.codehusky.huskyui.states.element.ActionableElement;
import com.codehusky.huskyui.states.element.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.config.PixelmonItemsTMs;
import net.eterniamc.pokebuilder.Configuration.Config;
import net.eterniamc.pokebuilder.ModifierData;
import net.eterniamc.pokebuilder.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Created by Justin
 */
public class MoveModifier implements Modifier {

    @Override
    public boolean run(ModifierData data) {
        Pokemon pixelmon = data.getPokemon();
        Player player = data.getPlayer();
        StateContainer container = data.getGui() == null ? new StateContainer() : data.getGui();
        if (container.hasState("move"))
            container.removeState("move");
        Page.PageBuilder builder = Page.builder()
                .setTitle(Text.of("Move Modifier"))
                .setAutoPaging(true)
                .setParent("editor")
                .setEmptyStack(Utils.empty());
        if (data.getGui() == null) {
            builder.setParent(null);
        }
        for (int it = 0; it < 4; it++) {
            final int i = it;
            builder.addElement(new Element(ItemStack.empty()));
            builder.addElement(
                    new ActionableElement(
                            new RunnableAction(container, ActionType.NONE, "", context -> {
                                Page.PageBuilder moveSlot = Page.builder()
                                        .setAutoPaging(true)
                                        .setParent("move")
                                        .setTitle(Text.of(pixelmon.getMoveset().get(i) != null ? "Replacing " + pixelmon.getMoveset().get(i).baseAttack.getLocalizedName() : "Teaching"))
                                        .setEmptyStack(Utils.empty());
                                for (Attack attack : pixelmon.getBaseStats().getAllMoves().stream().filter(s -> !pixelmon.getMoveset().contains(s)).collect(Collectors.toList()))
                                    moveSlot.addElement(new ActionableElement(
                                                    new RunnableAction(container, ActionType.CLOSE, "", action -> {
                                                        pixelmon.getMoveset().set(i, attack);
                                                        if (data.getGui() != null)
                                                            Utils.withdraw(player, getCost(pixelmon));
                                                        if (((EntityPlayerMP) player).getHeldItemMainhand().getCount() == 1)
                                                            ((EntityPlayerMP) player).setHeldItem(EnumHand.MAIN_HAND, net.minecraft.item.ItemStack.EMPTY);
                                                        ((EntityPlayerMP) player).getHeldItemMainhand().shrink(1);
                                                    }),
                                                    ItemStack.builder()
                                                            .itemType((ItemType) PixelmonItemsTMs.TMs.get(0))
                                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, attack.baseAttack.getLocalizedName()))
                                                            .add(Keys.ITEM_LORE, Collections.singletonList(Text.of("Teach your Pokemon this move")))
                                                            .build()
                                            )
                                    );
                                container.removeState("slot" + i);
                                container.addState(moveSlot.build("slot" + i));
                                container.openState(player, "slot" + i);
                            }),
                            pixelmon.getMoveset().get(i) != null ?
                                    ItemStack.builder()
                                            .itemType(ItemTypes.STAINED_GLASS_PANE)
                                            .add(Keys.DYE_COLOR, DyeColors.WHITE)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, pixelmon.getMoveset().get(i).baseAttack.getLocalizedName()))
                                            .build() :
                                    ItemStack.builder()
                                            .itemType(ItemTypes.BARRIER)
                                            .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, "Empty"))
                                            .build()
                    )
            );
        }
        container.addState(builder.build("move"));
        container.openState(player, "move");
        return false;
    }

    @Override
    public ItemType getItem() {
        return (ItemType) PixelmonItemsTMs.TMs.get(0);
    }

    @Override
    public double getCost(Pokemon pokemon) {
        return Config.moveModifierCost * getMultiplier(pokemon);
    }
}
