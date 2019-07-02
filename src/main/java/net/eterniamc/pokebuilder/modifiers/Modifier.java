package net.eterniamc.pokebuilder.modifiers;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.eterniamc.pokebuilder.Configuration.Config;
import net.eterniamc.pokebuilder.ModifierData;
import net.eterniamc.pokebuilder.PokeBuilder;
import net.eterniamc.pokebuilder.Utils;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Arrays;
import java.util.List;

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

    default String getDescription() {
        return null;
    }

    default ItemStack getItemStack(Player player, Pokemon pokemon) {
        List<Text> lore = Lists.newArrayList();
        if (getDescription() != null) {
            lore.add(TextSerializers.FORMATTING_CODE.deserialize(getDescription()));
        }
        if (pokemon != null && getCost(pokemon) > 0) {
            lore.add(Text.of(
                    getCost(pokemon) > Utils.getBal(player) ? TextColors.RED : TextColors.GREEN,
                    getCost(pokemon) + " " + PokeBuilder.getCurrency().getPluralDisplayName().toPlain()
            ));
        }
        ItemStack item = ItemStack.builder()
                .itemType(getItem())
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, getName()))
                .add(Keys.ITEM_LORE, lore).build();
        // theres definitely a better way to do this but who cares ;p
        NBTTagCompound nbt = ((net.minecraft.item.ItemStack) (Object) item).getTagCompound();
        nbt.setString("Modifier", getClass().getName());
        ((net.minecraft.item.ItemStack) (Object) item).setTagCompound(nbt);
        return item;
    }

    default double getMultiplier(Pokemon pokemon) {
        return Arrays.stream(EnumSpecies.LEGENDARY_ENUMS).anyMatch(e -> e == pokemon.getSpecies() || EnumSpecies.Ditto == pokemon.getSpecies()) ? Config.legendaryOrDittoMultiplier : 1;
    }
}
