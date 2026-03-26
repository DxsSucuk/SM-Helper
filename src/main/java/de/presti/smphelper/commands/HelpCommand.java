package de.presti.smphelper.commands;

import de.presti.smphelper.utils.ResourceUtil;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.Cooldown;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.time.temporal.ChronoUnit;

@Command
@Cooldown(cooldown = 5, unit = ChronoUnit.SECONDS)
public class HelpCommand {

    @JDASlashCommand(name = "help", description = "Need help? Check out help menu!")
    public void onHelpRequest(
            GuildSlashEvent event
    ) {
        event.deferReply(true).queue();

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        messageCreateBuilder.setComponents(Container.of(Section.of(
                Thumbnail.fromFile(ResourceUtil.getResourceAsFileUpload("/minispidey.png")),
                TextDisplay.of("## What can I help you with?"),
                TextDisplay.of("Below you can select one of many FAQs!"),
                TextDisplay.of("If your question isn't answered feel free to ask in <#1321529763583492220>")
        ).withUniqueId(1), ActionRow.of(StringSelectMenu.create("help:select")
                .addOptions(
                        SelectOption.of("When release", "release"),
                        SelectOption.of("How to get mod", "mod-get"),
                        SelectOption.of("Help with mod", "mod-help")
                ).build())));
        messageCreateBuilder.useComponentsV2();

        event.getInteraction().getHook().sendMessage(messageCreateBuilder.build()).queue();
    }
}