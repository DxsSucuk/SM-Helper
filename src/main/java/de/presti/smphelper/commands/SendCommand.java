package de.presti.smphelper.commands;

import de.presti.smphelper.Main;
import de.presti.smphelper.utils.Config;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Command
public class SendCommand {
    @JDASlashCommand(name = "send", description = "Send predefined messages.")
    public void onSendRequest(
            GuildSlashEvent event,
            @SlashOption(description = "What message should be sent.", usePredefinedChoices = true) int action,
            @SlashOption(description = "What channel should be used.", usePredefinedChoices = true) TextChannel channel
    ) {
        if (event.getMember().getIdLong() != Config.getInstance().getDevUserId()) {
            event.reply("Not allowed!").setEphemeral(true).queue();
            return;
        }


        event.deferReply(true).queue();
        switch (action) {
            case 1 -> channel.sendMessageComponents(Main.createInitialMessageForReport()).useComponentsV2().queue();
            case 2 -> channel.sendMessageComponents(Main.createCommonIssues()).useComponentsV2().queue();
        }
        event.getInteraction().getHook().sendMessage("Work done!").queue();
    }
}