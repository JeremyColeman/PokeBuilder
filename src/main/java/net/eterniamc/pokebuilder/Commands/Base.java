package net.eterniamc.pokebuilder.Commands;

import net.eterniamc.pokebuilder.PokeBuilderGui;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class Base implements CommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .permission("pokebuilder.gui")
                .arguments(GenericArguments.playerOrSource(Text.of("player")))
                .executor(new Base())
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        Player player = src.hasPermission("pokebuilder.gui.other") ? args.<Player>getOne("player").get() : (Player) src;
        PokeBuilderGui.openGui(player);
        return CommandResult.success();
    }

}
