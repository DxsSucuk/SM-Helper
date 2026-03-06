package de.presti.smphelper.commands;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.Cooldown;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

import java.time.temporal.ChronoUnit;

@Command
@Cooldown(cooldown = 5, unit = ChronoUnit.SECONDS)
public class HelpCommand {

    @JDASlashCommand(name = "help", description = "Need help? Check out help menu!")
    public void onHelpRequest(
            GuildSlashEvent event
    ) {
        event.deferReply(true).queue();
        // TODO:: make a thingy with buttons.
        event.getInteraction().getHook().sendMessage("Work done!").queue();
    }
}