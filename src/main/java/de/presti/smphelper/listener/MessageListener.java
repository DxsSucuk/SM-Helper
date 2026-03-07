package de.presti.smphelper.listener;

import de.presti.smphelper.Main;
import de.presti.smphelper.utils.Config;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;

@Slf4j
public class MessageListener extends ListenerAdapter {

    HashMap<Long, Long> timedOutUsers = new HashMap<>();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);
        if (event.getChannel().getType() == ChannelType.TEXT && event.getChannel().asTextChannel().getParentCategoryIdLong() == Main.getRespondToMessageCategory()) {
            if (!event.getMessage().getEmbeds().isEmpty() || !event.getMessage().getAttachments().isEmpty()) return;

            var systemTimeOfTimeout = timedOutUsers.getOrDefault(event.getAuthor().getIdLong(), -1L);
            var timeOutDifference = System.currentTimeMillis() - systemTimeOfTimeout;

            if (systemTimeOfTimeout != -1L && timeOutDifference < Duration.ofSeconds(30).toMillis()) {
                log.info("User -> {} ignored because of timeout.", event.getAuthor().getName());
                return;
            }

            String content = event.getMessage().getContentRaw().toLowerCase();

            String[] words = content.split("\\s+");

            boolean hasWebHeadRole = event.getMember() != null && event.getMember().getRoles().stream().anyMatch(x -> x.getIdLong() == 1321252725291483137L);
            //boolean hasHigherRoleThanWebHead = event.getMember() != null && event.getGuild().getRoleById(1321252725291483137L).getPosition() < event.getMember().getRoles().getFirst().getPosition();

            if (content.contains("free") || content.contains("release")) {
                var punishment = Main.getPunishmentOfUserOrDefault(event.getAuthor().getIdLong(), 0);
                punishment.addViolation(1);

                if (punishment.getPunishmentCount() > Config.getInstance().getMinViolationsUntilTimeout()) {
                    var minuteOfTimeOut = Math.max(1, punishment.getPunishmentCount() - Config.getInstance().getMinViolationsUntilTimeout());
                    if (event.getGuild().getSelfMember().canInteract(event.getMember())) {
                        event.getMember().timeoutFor(Duration.ofMinutes(minuteOfTimeOut)).reason("Triggered the free release message " + punishment.getPunishmentCount() + " times!").queue();
                    }
                }

                Main.addPunishment(punishment);

                log.info("User -> {} triggered release.", event.getMember().getEffectiveName());
                event.getMessage().replyComponents(Main.createReleaseContainer()).useComponentsV2().queue();
                timedOutUsers.put(event.getAuthor().getIdLong(), System.currentTimeMillis());
            } else if ((Arrays.stream(words).anyMatch(x -> x.equalsIgnoreCase("help")) && Arrays.stream(words).anyMatch(x -> x.equalsIgnoreCase("mod"))) && !hasWebHeadRole/* && !hasHigherRoleThanWebHead*/) {
                event.getMessage().replyComponents(Main.createNeedSupportContainer()).useComponentsV2().queue();
                timedOutUsers.put(event.getAuthor().getIdLong(), System.currentTimeMillis());
            }
        }
    }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("Upload file to report")) {
            if (event.getChannelType() == ChannelType.GUILD_PUBLIC_THREAD) {
                if (!event.getUser().isBot() && Main.isCrashReport(event.getChannelIdLong())) {

                    var crashReport = Main.getCrashReportFromChanneldId(event.getChannelIdLong());

                    if (crashReport == null) {
                        log.error("This should not happen??????????????????");
                        return;
                    }

                    if (event.getUser().getIdLong() == crashReport.getOwnerId()) {
                        if (event.getTarget().getAttachments().isEmpty()) {
                            event.reply("Seems like you selected a message without any file! Please select a .log or .dmp file!").setEphemeral(true).queue();
                        } else {
                            event.getMessageChannel().retrievePinnedMessages().queue(pinnedMessage -> {
                                var initialMessage = pinnedMessage.getFirst();
                                boolean doneSmth = false;

                                MessageEditBuilder messageEditBuilder = MessageEditBuilder.fromMessage(initialMessage.getMessage());

                                for (Message.Attachment attachment : event.getTarget().getAttachments()) {
                                    if (attachment.getFileExtension() == null) continue;

                                    int idToUse = attachment.getFileExtension().equals("log") ? 1001 : attachment.getFileExtension().equals("dmp") ? 1002 : 0;

                                    if (idToUse > 0) {
                                        messageEditBuilder.setComponents(messageEditBuilder.getComponentTree().replace(ComponentReplacer.byUniqueId(idToUse,
                                                FileDisplay.fromFile(FileUpload.fromStreamSupplier(attachment.getFileName(), () -> attachment.getProxy().download().join())).withUniqueId(idToUse))));
                                        doneSmth = true;
                                    }
                                }

                                if (doneSmth) {
                                    event.reply("File upload successful! Thank you for your help!").setEphemeral(true).queue();
                                    initialMessage.getMessage().editMessage(messageEditBuilder.build()).queue(x -> event.getTarget().delete().queue());
                                } else {
                                    event.reply("Seems like you selected a message without a valid file! Please select a .log or .dmp file!").setEphemeral(true).queue();
                                }
                            });
                        }
                    } else {
                        event.reply("This isn't your report!").setEphemeral(true).queue();
                    }
                }
            }
        }
    }
}
