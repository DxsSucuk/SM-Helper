package de.presti.smphelper.listener;

import de.presti.smphelper.Main;
import de.presti.smphelper.dto.CrashReport;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ComponentListener extends ListenerAdapter {

    Modal modalCache;

    HashMap<Long, Long> timedOutUsers = new HashMap<>();

    public ComponentListener() {
        getModalCache();
    }

    public Modal getModalCache() {
        if (modalCache == null) {
            TextInput subject = TextInput.create("lobby_size", TextInputStyle.SHORT)
                    .setPlaceholder("How many players where in the lobby?")
                    .setRequiredRange(1, 2)
                    .setMaxLength(2)
                    .setMinLength(1)
                    .setRequired(true)
                    .build();

            TextInput body = TextInput.create("crash_description", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Give us a description of what happened!")
                    .setMinLength(15)
                    .setMaxLength(1000)
                    .setRequired(true)
                    .build();

            modalCache = Modal.create("crash_report", "Crash Report")
                    .addComponents(
                            Label.of("Lobby Size", subject),
                            Label.of("Description", body),
                            Label.of("Players", EntitySelectMenu.create("players", EntitySelectMenu.SelectTarget.USER).setMaxValues(12).build()),
                            Label.of("Crash Log", AttachmentUpload.create("log").setRequired(false).setMaxValues(1).build()),
                            Label.of("Crash Dump", AttachmentUpload.create("dump").setRequired(false).setMaxValues(1).build()))//,
                    //Label.of("Clip", AttachmentUpload.create("clip").setRequired(false).setMaxValues(1).build()))
                    .build();
        }

        return modalCache;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("open_report_modal")) {
            //event.deferReply(true).complete();

            var executionTime = System.currentTimeMillis();
            var systemTimeOfTimeout = timedOutUsers.getOrDefault(event.getUser().getIdLong(), -1L);

            if (systemTimeOfTimeout != -1L && (systemTimeOfTimeout + Duration.ofSeconds(30).toMillis()) > executionTime) {
                event.reply("You are on cooldown!").setEphemeral(true).queue();
                return;
            }

            timedOutUsers.put(event.getUser().getIdLong(), executionTime);

            event.replyModal(getModalCache()).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("crash_report")) {
            event.deferReply(true).queue();
            var lobbySize = event.getValue("lobby_size");
            int lobbyCount = -1;
            try {
                if (lobbySize != null && lobbySize.getType() == Component.Type.TEXT_INPUT) {
                    lobbyCount = Integer.parseInt(Objects.requireNonNull(event.getValue("lobby_size")).getAsString());
                }
                if (lobbyCount > 16) throw new IllegalArgumentException();
            } catch (Exception exception) {
                event.getInteraction().getHook().sendMessage("Invalid lobby size. Please enter a valid number between 1 and 16.").queue();
                return;
            }

            var descriptionComponent = event.getValue("crash_description");

            if (descriptionComponent == null || descriptionComponent.getType() != Component.Type.TEXT_INPUT) {
                return;
            }

            String description = descriptionComponent.getAsString();
            ModalMapping logInput = event.getValue("log");
            ModalMapping dumpInput = event.getValue("dump");

            var playersComponent = event.getValue("players");

            List<User> players = new ArrayList<>(List.of(event.getUser()));

            if (playersComponent != null && playersComponent.getType() == Component.Type.USER_SELECT) {
                players.addAll(playersComponent.getAsMentions().getUsers());
            }

            FileDisplay logDisplay = FileDisplay.fromFile(FileUpload.fromData("Sample Log".getBytes(StandardCharsets.UTF_8), "placeholder.log")).withUniqueId(1001);
            FileDisplay dumpDisplay = FileDisplay.fromFile(FileUpload.fromData("Sample Dump".getBytes(StandardCharsets.UTF_8), "placeholder.dmp")).withUniqueId(1002);
            // Maybe for the future?
            //FileDisplay clipDisplay = FileDisplay.fromFile(FileUpload.fromData("Clip".getBytes(StandardCharsets.UTF_8), "placeholder.mp4")).withUniqueId(1003);

            boolean replacedDump = false, replacedLog = false;

            try {
                if (logInput != null && logInput.getType() == Component.Type.FILE_UPLOAD && !logInput.getAsAttachmentList().isEmpty()) {
                    var logFile = logInput.getAsAttachmentList().getFirst();
                    if (logFile == null || logFile.getFileExtension() == null || !logFile.getFileExtension().equalsIgnoreCase("log")) {
                        event.getInteraction().getHook().sendMessage("Extension of the log file is not .log").queue();
                        return;
                    }
                    logDisplay = FileDisplay.fromFile(FileUpload.fromData(logFile.getProxy().download().join(), logFile.getFileName())).withUniqueId(1001);
                    replacedLog = true;
                }
            } catch (Exception ignored) {}

            try {
                if (dumpInput != null && dumpInput.getType() == Component.Type.FILE_UPLOAD && !dumpInput.getAsAttachmentList().isEmpty()) {
                    var dumpFile = dumpInput.getAsAttachmentList().getFirst();
                    if (dumpFile == null || dumpFile.getFileExtension() == null || !dumpFile.getFileExtension().equalsIgnoreCase("dmp")) {
                        event.getInteraction().getHook().sendMessage("Extension of the dump file is not .dmp").queue();
                        return;
                    }
                    dumpDisplay = FileDisplay.fromFile(FileUpload.fromData(dumpFile.getProxy().download().join(), dumpFile.getFileName())).withUniqueId(1002);
                    replacedDump = true;
                }
            } catch (Exception ignored) {}

            List<ContainerChildComponent> components = new ArrayList<>();

            if (!replacedLog || !replacedDump) {
                components.addAll(List.of(Separator.createDivider(Separator.Spacing.LARGE),
                        TextDisplay.of("## How do I add my log and dump file?"),
                        TextDisplay.of("Send your files in this channel and afterwards -> Right click the sent message -> Apps -> Upload file to report"),
                        TextDisplay.of("They will automatically be added to this overview!")));
            }

            if (players.isEmpty()) {
                if (!components.isEmpty()) {
                    components.add(Separator.createDivider(Separator.Spacing.SMALL));
                } else {
                    components.add(Separator.createDivider(Separator.Spacing.LARGE));
                }
                components.addAll(List.of(
                        TextDisplay.of("## What now?"),
                        TextDisplay.of("If you played with multiple people you should probably ping them here!"),
                        TextDisplay.of("Just so we can keep track of them! Otherwise patiently wait!")));
            }

            List<ContainerChildComponent> childComponents = new ArrayList<>(List.of(
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
                    logDisplay,
                    dumpDisplay));

            if (!components.isEmpty()) {
                childComponents.addAll(components);
            }

            childComponents.addAll(List.of(Separator.createDivider(Separator.Spacing.LARGE),
                            TextDisplay.of("## Reported by"),
                            TextDisplay.of(event.getMember().getAsMention())));

            if (!players.isEmpty()) {
                childComponents.addAll(List.of(Separator.createDivider(Separator.Spacing.SMALL),
                        TextDisplay.of("## Part of the Session"),
                        TextDisplay.of(String.join(" ", players.stream().map(User::getAsMention).toList()))));
            }

            Container container = Container.of(childComponents);

            var forumChannel = event.getJDA().getForumChannelById(event.getChannelIdLong() == Main.getTesterReportChannelId() ? Main.getTesterForumChannelId() : Main.getForumChannelId());
            if (forumChannel == null) {
                log.info("Failed to get Forum!");
                return;
            }

            Main.setCurrentIndex(Main.getCurrentIndex() + 1);

            forumChannel.createForumPost(description.substring(0, Math.min(description.length() - 1, 100)),
                    new MessageCreateBuilder().addComponents(container).useComponentsV2().build()).queue(x -> {
                x.getThreadChannel().getManager().setAppliedTags(forumChannel.getAvailableTagsByName("open", true)).queue();
                x.getThreadChannel().pinMessageById(x.getThreadChannel().getLatestMessageId()).queue();
                x.getThreadChannel().addThreadMember(event.getMember()).queue();
                x.getThreadChannel().getManager().setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_1_WEEK).queue();
                Main.addCrashReport(new CrashReport(x.getThreadChannel().getIdLong(), event.getUser().getIdLong()));
                event.getInteraction().getHook().sendMessage("Crash reported, thank you very much for the help! -> " + x.getThreadChannel().getAsMention()).queue();
                x.getThreadChannel().getManager().setAppliedTags(forumChannel.getAvailableTagsByName("Open", true)).queue();
            });

        }
    }
}
