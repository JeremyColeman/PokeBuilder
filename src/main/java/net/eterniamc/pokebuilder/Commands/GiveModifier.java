package net.eterniamc.pokebuilder.Commands;

import net.eterniamc.pokebuilder.PokeBuilder;
import net.eterniamc.pokebuilder.modifiers.Modifier;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.stream.Collectors;

public class GiveModifier implements CommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .permission("pokebuilder.modifier.give")
                .arguments(
                        GenericArguments.playerOrSource(Text.of("player")),
                        GenericArguments.choices(
                                Text.of("modifier"),
                                () -> PokeBuilder.getModifiers().stream().map(m -> m.getClass().getSimpleName().replace("Modifier", "")).collect(Collectors.toList()),
                                s -> {
                                    Optional<Modifier> optionalModifier = PokeBuilder.getModifiers().stream().filter(m -> m.getClass().getSimpleName().replace("Modifier", "").equals(s)).findFirst();
                                    if (!optionalModifier.isPresent()) return Optional.empty();
                                    return optionalModifier.get();
                                }
                        )
                )
                .executor(new GiveModifier())
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        Player player = args.<Player>getOne("player").get();
        Modifier modifier = args.<Modifier>getOne("modifier").get();
        player.getInventory().offer(modifier.getItemStack(player, null));
        return CommandResult.success();
    }
}
