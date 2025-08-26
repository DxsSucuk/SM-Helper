package de.presti.smphelper.listener;

import de.presti.smphelper.Main;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("Upload file to report")) {
            if (event.getChannelType() == ChannelType.GUILD_PUBLIC_THREAD) {
                if (!event.getUser().isBot() && event.getMessageChannel().getName().matches(Main.pattern)) {
                    String userId = event.getMessageChannel().getName().split("-")[2];
                    if (event.getUser().getId().equalsIgnoreCase(userId)) {
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
