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

import java.util.stream.Collectors;

public class GiveModifier implements CommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .permission("pokebuilder.modifier.give")
                .arguments(
                        GenericArguments.withSuggestions(
                                GenericArguments.string(Text.of("modifier")),
                                PokeBuilder.getModifiers().stream().map(m -> m.getClass().getSimpleName().replace("Modifier", "").toLowerCase()).collect(Collectors.toList())
                        ),
                        GenericArguments.playerOrSource(Text.of("player"))
                )
                .executor(new GiveModifier())
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        Player player = args.<Player>getOne("player").get();
        String s = args.<String>getOne("modifier").get();
        Modifier modifier = PokeBuilder.getModifiers().stream()
                .filter(m -> m.getClass().getSimpleName().replace("Modifier", "").equalsIgnoreCase(s))
                .findFirst()
                .orElseThrow(() -> new Error("Could not find modifier with the name \"" + s + "\""));
        return player.getInventory().offer(modifier.getItemStack(player, null)).getRejectedItems().isEmpty() ? CommandResult.empty() : CommandResult.empty();
    }
}
