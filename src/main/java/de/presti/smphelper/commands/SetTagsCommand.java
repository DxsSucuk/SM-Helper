package de.presti.smphelper.commands;

import de.presti.smphelper.Main;
import de.presti.smphelper.utils.Config;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.text.annotations.RequireOwner;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;

@Command
@RequireOwner
public class SetTagsCommand {
    @JDASlashCommand(name = "set-tags", description = "Set the tags of a forum-channel.")
    public void onSetTagRequest(
            GuildSlashEvent event,
            @SlashOption(description = "What channel should be used.", name = "channel") ForumChannel channel
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