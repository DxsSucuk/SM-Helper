package de.presti.smphelper.listener;

import de.presti.smphelper.Main;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.nio.charset.StandardCharsets;

public class ComponentListener extends ListenerAdapter {

    Modal modalCache;

    public Modal getModalCache() {
        if (modalCache == null) {
            TextInput subject = TextInput.create("lobby_size", "Lobby Size", TextInputStyle.SHORT)
                    .setPlaceholder("How many players where in the lobby?")
                    .setRequiredRange(1, 2)
                    .setMaxLength(2)
                    .setMinLength(1)
                    .setRequired(true)
                    .build();

            TextInput body = TextInput.create("crash_description", "Description", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Give us a description of what happened!")
                    .setMinLength(15)
                    .setMaxLength(1000)
                    .setRequired(true)
                    .build();

            return modalCache = Modal.create("crash_report", "Crash Report")
                    .addComponents(ActionRow.of(subject), ActionRow.of(body))
                    .build();
        }

        return modalCache;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("open_report_modal")) {
            event.replyModal(getModalCache()).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("crash_report")) {
            int lobbyCount = -1;
            try {
                 lobbyCount = Integer.parseInt(event.getValue("lobby_size").getAsString());
                 if (lobbyCount > 16) throw new IllegalArgumentException();
            } catch (Exception exception) {
                event.reply("Invalid lobby size. Please enter a valid number between 1 and 16.").setEphemeral(true).queue();
                return;
            }
            String description = event.getValue("crash_description").getAsString();

            Container container = Container.of(
                    Section.of(
                            Thumbnail.fromFile(Main.getResourceAsFileUpload("/minispidey.png")),
                            TextDisplay.of("## Lobby Size"),
                            TextDisplay.of(String.valueOf(lobbyCount))
                    ),

                    Separator.createDivider(Separator.Spacing.SMALL),

                    Section.of(
                            Thumbnail.fromFile(Main.getResourceAsFileUpload("/minispideysad.png")),
                            TextDisplay.of("## What happened?"),
                            TextDisplay.of(description)
                    ),
                    Separator.createDivider(Separator.Spacing.LARGE),
                    FileDisplay.fromFile(FileUpload.fromData("Sample Log".getBytes(StandardCharsets.UTF_8), "placeholder.log")).withUniqueId(1001),
                    FileDisplay.fromFile(FileUpload.fromData("Sample Dump".getBytes(StandardCharsets.UTF_8), "placeholder.dmp")).withUniqueId(1002),
                    Separator.createDivider(Separator.Spacing.LARGE),
                    TextDisplay.of("## How do I add my log and dump file?"),
                    TextDisplay.of("Send your files in this channel and afterwards -> Right click the sent message -> Apps -> Upload file to report"),
                    TextDisplay.of("They will automatically be added to this overview!"),
                    Separator.createDivider(Separator.Spacing.SMALL),
                    TextDisplay.of("## What now?"),
                    TextDisplay.of("If you played with multiple people you should probably ping them here!"),
                    TextDisplay.of("Just so we can keep track of them! Otherwise patiently wait!"),
                    Separator.createDivider(Separator.Spacing.LARGE),
                    TextDisplay.of("## Reported by"),
                    TextDisplay.of(event.getMember().getAsMention())
            );

            Main.setCurrentIndex(Main.getCurrentIndex() + 1);

            event.getJDA().getForumChannelById(Main.getForumChannelId()).createForumPost("crash-" + (Main.getCurrentIndex()) + "-" + event.getMember().getIdLong(),
                    new MessageCreateBuilder().addComponents(container).useComponentsV2().build()).queue(x -> {
                x.getThreadChannel().pinMessageById(x.getThreadChannel().getLatestMessageId()).queue();
                x.getThreadChannel().addThreadMember(event.getMember()).queue();
                x.getThreadChannel().getManager().setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_1_WEEK).queue();
                event.reply("Crash reported, please send your DMP and log file into this channel -> " + x.getThreadChannel().getAsMention()).setEphemeral(true).queue();
            });

        }
    }
}
