package de.presti.smphelper.commands;

import de.presti.smphelper.Main;
import de.presti.smphelper.utils.Config;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Command
public class SetTagsCommand {
    @JDASlashCommand(name = "send", description = "Send predefined messages.")
    public void onSetTagRequest(
            GuildSlashEvent event,
            @SlashOption(description = "What channel should be used.", usePredefinedChoices = true) ForumChannel channel
    ) {
        if (event.getMember().getIdLong() != Config.getInstance().getDevUserId()) {
            event.reply("Not allowed!").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();
        Main.setTags(channel);
        event.getInteraction().getHook().sendMessage("Work done!").queue();
    }
}