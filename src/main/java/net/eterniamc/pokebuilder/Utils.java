package net.eterniamc.pokebuilder;

import com.google.common.base.CaseFormat;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.math.BigDecimal;

public class Utils {
    private static Text textStarterError = TextSerializers.FORMATTING_CODE.deserialize("&c&lPokeBuilder >&f ");
    private static Text textStarter = TextSerializers.FORMATTING_CODE.deserialize("&c&lPokeBuilder &7&l>&f ");

    public static void sendPlayerError(Player player, String msg) {
        player.sendMessage(Text.join(textStarterError, Text.of(msg)));
    }

    public static void sendPlayerMessage(Player player, String msg) {
        player.sendMessage(Text.join(textStarter, Text.of(msg)));
    }

    public static ItemStack empty() {
        ItemStack empty = ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).build();
        empty.offer(Keys.DISPLAY_NAME, Text.of());
        empty.offer(Keys.DYE_COLOR, DyeColors.GRAY);
        return empty;
    }

    public static String fromCamelToDisplay(String s) {
        s = toDisplayCase(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, s));
        return s;
    }


    public static String toDisplayCase(String s) {
        s = s.replace("_", " ");
        final String ACTIONABLE_DELIMITERS = " '-/"; // these cause the character following
        // to be capitalized

        StringBuilder sb = new StringBuilder();
        boolean capNext = true;

        for (char c : s.toCharArray()) {
            c = (capNext)
                    ? Character.toUpperCase(c)
                    : Character.toLowerCase(c);
            sb.append(c);
            capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
        }
        return sb.toString();
    }

    public static void withdraw(Player player, double amount) {
        EconomyService es = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
        es.getOrCreateAccount(player.getUniqueId()).ifPresent(a -> a.withdraw(PokeBuilder.instance.currency, new BigDecimal(amount), Cause.of(EventContext.empty(), PokeBuilder.instance)));
    }

    public static double getBal(Player player) {
        EconomyService es = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
        return es.getOrCreateAccount(player.getUniqueId()).map(a -> a.getBalance(PokeBuilder.instance.currency).doubleValue()).orElse(0.0);
    }
}
