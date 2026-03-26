package de.presti.smphelper.listener;

import de.presti.smphelper.Main;
import de.presti.smphelper.utils.Config;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;

public class VoiceListener extends ListenerAdapter {

    @Override
    public void onGuildVoiceUpdate(@NonNull GuildVoiceUpdateEvent event) {
        super.onGuildVoiceUpdate(event);

        if (event.getChannelLeft() != null && event.getChannelLeft().getParentCategoryIdLong() == Config.getInstance().getTemporalVoiceCategory()) {
            if (event.getChannelLeft().getMembers().isEmpty()) {
                event.getChannelLeft().delete().reason("No one in lobby.").queue(success -> {
                    Main.getTempVoiceChannelAndOwnerIds().remove(event.getChannelLeft().getIdLong());
                });
            }
        }
    }

    @Override
    public void onChannelDelete(@NonNull ChannelDeleteEvent event) {
        super.onChannelDelete(event);
        if (event.getChannelType() == ChannelType.VOICE && event.getChannel().asAudioChannel().getParentCategoryIdLong() == Config.getInstance().getTemporalVoiceCategory()) {
            Main.getTempVoiceChannelAndOwnerIds().remove(event.getChannel().getIdLong());
        }
    }
}
