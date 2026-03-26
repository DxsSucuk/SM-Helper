package de.presti.smphelper.listener;

import de.presti.smphelper.Main;
import de.presti.smphelper.utils.ComponentUtil;
import de.presti.smphelper.utils.Config;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ReadyListener extends ListenerAdapter {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        if (Config.getInstance().isSendInitialMessage()) {
            var channel = event.getJDA().getTextChannelById(Config.getInstance().getChannelId());
            if (channel != null) {
                channel.sendMessageComponents(ComponentUtil.createInitialMessageForReport()).useComponentsV2().queue();
            }

            channel = event.getJDA().getTextChannelById(Config.getInstance().getTesterReportChannelId());
            if (channel != null) {
                channel.sendMessageComponents(ComponentUtil.createInitialMessageForReport()).useComponentsV2().queue();
            }
        }

        if (Config.getInstance().isSetInitialForumTag()) {
            var channel = event.getJDA().getForumChannelById(Config.getInstance().getForumChannelId());
            if (channel != null) {
                Main.setTags(channel);
            }

            channel = event.getJDA().getForumChannelById(Config.getInstance().getTesterForumChannelId());
            if (channel != null) {
                Main.setTags(channel);
            }
        }

        if (Config.getInstance().isClearTemporalVCsOnStart()) {
            var category = event.getJDA().getCategoryById(Main.getTemporalVoiceCategory());
            if (category != null) {
                for (var channel : category.getVoiceChannels()) {
                    if (channel.getMembers().isEmpty()) {
                        channel.delete().queue();
                    }
                }
            }
        }
    }
}
