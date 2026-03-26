package de.presti.smphelper.commands;

import de.presti.smphelper.Main;
import de.presti.smphelper.utils.Config;
import de.presti.smphelper.utils.ThreadUtil;
import io.github.freya022.botcommands.api.commands.CommandPath;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.Cooldown;
import io.github.freya022.botcommands.api.commands.application.SlashOptionChoiceProvider;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Command
@Cooldown(cooldown = 10, unit = ChronoUnit.SECONDS)
public class LobbyCommand implements SlashOptionChoiceProvider {

    @Cooldown(cooldown = 30, unit = ChronoUnit.SECONDS)
    @TopLevelSlashCommandData(description = "Host lobbies!")
    @JDASlashCommand(name = "lobby", subcommand = "create", description = "Create a lobby!")
    public void onLobbyRequest(
            GuildSlashEvent event,
            @SlashOption(description = "Is this lobby invite-only?", name = "private") boolean privateLobby,
            @SlashOption(description = "Whats your location? (EU, ASIA, AMERICA, Northpole??, SPACE??????)", name = "location") String location,
            @SlashOption(description = "If any, what VPN software should be used to connect? (rAdmin, ZeroTier, Hamachi, TailScale)", name = "vpnsoftware") String vpnSoftware,
            @SlashOption(description = "If vpn is used, define network name! (ZeroTier -> NetId, rAdmin -> network name)", name = "name") String vpnLoginName,
            @SlashOption(description = "If vpn is used, define network password! (ZeroTier -> None, rAdmin -> network password)", name = "password") String vpnLoginPassword,
            @SlashOption(description = "If mods are used or not!", name = "modded") boolean usingMods

    ) {
        event.deferReply(true).queue();

        if (Main.isLockdown()) {
            event.getInteraction().getHook().sendMessage("Can't create a lobby while lockdown is active!").queue();
            return;
        }

        if (Main.getTempVoiceChannelAndOwnerIds().containsValue(event.getMember().getIdLong())) {
            event.getInteraction().getHook().sendMessage("You already have a lobby!").queue();
            return;
        }

        String lobbyName = event.getUser().getGlobalName() + "'s lobby";

        boolean usesVPN = !(vpnSoftware == null || vpnSoftware.equalsIgnoreCase("none"));
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(lobbyName);
        embedBuilder.setColor(privateLobby ? Color.RED : Color.GREEN);
        embedBuilder.addField("**Location**", location, true);
        embedBuilder.addField("**Modded**", usingMods ? "Yes" : "No", true);
        if (usesVPN) {
            embedBuilder.addBlankField(true);
            embedBuilder.addField("**VPN**", "", true);
            embedBuilder.addField("**Software**", vpnSoftware, true);
            embedBuilder.addField("**Login**", vpnLoginName + ":" + vpnLoginPassword, true);
        }

        event.getJDA().getCategoryById(Config.getInstance().getTemporalVoiceCategory()).createVoiceChannel(lobbyName).queue(channel -> {
            Main.getTempVoiceChannelAndOwnerIds().put(channel.getIdLong(), event.getMember().getIdLong());

            MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
            messageCreateBuilder.setEmbeds(embedBuilder.build());
            messageCreateBuilder.setContent("A new lobby is available -> " + channel.getAsMention());

            event.getJDA().getTextChannelById(Config.getInstance().getLobbyShareChannel()).sendMessage(messageCreateBuilder.build()).queue();
            if (privateLobby) {
                channel.getManager().putRolePermissionOverride(channel.getGuild().getPublicRole().getIdLong(), null, Collections.singleton(Permission.VOICE_CONNECT)).queue();
                channel.getManager().putMemberPermissionOverride(event.getMember().getIdLong(), Collections.singleton(Permission.VOICE_CONNECT), null).queue();
            }

            if (event.getMember().getVoiceState() != null && event.getMember().getVoiceState().inAudioChannel()) {
                event.getGuild().moveVoiceMember(event.getMember(), channel).queue();
            }
            event.getInteraction().getHook().sendMessage("Your lobby has been created feel free to join -> " + channel.getAsMention()).queue();
            ThreadUtil.createThread(x -> {
                var currentChannelState = event.getGuild().getVoiceChannelById(channel.getIdLong());
                if (currentChannelState != null && currentChannelState.getMembers().isEmpty()) {
                    currentChannelState.delete().queue();
                }
            }, Duration.ofSeconds(30), false, false);
        });
    }

    @JDASlashCommand(name = "lobby", subcommand = "invite", description = "Invite people to your lobby!")
    public void onLobbyInvite(
            GuildSlashEvent event,
            @SlashOption(description = "Who would you like to invite?", name = "invite") Member inviteMember
    ) {
        event.deferReply(true).queue();

        if (event.getMember().getVoiceState() != null && event.getMember().getVoiceState().inAudioChannel() && event.getMember().getVoiceState().getChannel() != null) {
            var channel = event.getMember().getVoiceState().getChannel();
            var ownerId = Main.getTempVoiceChannelAndOwnerIds().get(channel.getIdLong());
            if (event.getMember().getIdLong() == ownerId) {
                var isPrivate = channel.getRolePermissionOverrides().stream().anyMatch(x -> x.isRoleOverride() && x.getRole().isPublicRole() && x.getDenied().contains(Permission.VOICE_CONNECT));

                if (isPrivate) {
                    channel.getManager().putMemberPermissionOverride(inviteMember.getIdLong(), Collections.singleton(Permission.VOICE_CONNECT), null).queue();
                    event.getInteraction().getHook().sendMessage("Connect permission has been granted for " + inviteMember.getAsMention() + "!").queue();
                } else {
                    event.getInteraction().getHook().sendMessage("This is a public lobby, you can't invite people!").queue();
                }
            } else {
                event.getInteraction().getHook().sendMessage("This isn't your lobby!").queue();
            }
        } else {
            event.getInteraction().getHook().sendMessage("You are not in a voice-channel, please join your own lobby voice-channel and try again!").queue();
        }
    }

    @JDASlashCommand(name = "lobby", subcommand = "kick", description = "Kick people from your lobby!")
    public void onLobbyKick(
            GuildSlashEvent event,
            @SlashOption(description = "Who would you like to kick?", name = "kick") Member kickMemeber
    ) {
        event.deferReply(true).queue();

        if (event.getMember().getVoiceState() != null && event.getMember().getVoiceState().inAudioChannel() && event.getMember().getVoiceState().getChannel() != null) {
            var channel = event.getMember().getVoiceState().getChannel();
            var ownerId = Main.getTempVoiceChannelAndOwnerIds().get(channel.getIdLong());
            if (event.getMember().getIdLong() == ownerId) {
                var isPrivate = channel.getRolePermissionOverrides().stream().anyMatch(x -> x.isRoleOverride() && x.getRole().isPublicRole() && x.getDenied().contains(Permission.VOICE_CONNECT));

                if (isPrivate) {
                    channel.getManager().removePermissionOverride(kickMemeber.getIdLong()).queue();
                    event.getInteraction().getHook().sendMessage("Connect permission has been denied for " + kickMemeber.getAsMention() + "!").queue();
                } else {
                    event.getInteraction().getHook().sendMessage("This is a public lobby, you can't kick people!").queue();
                }
            } else {
                event.getInteraction().getHook().sendMessage("This isn't your lobby!").queue();
            }
        } else {
            event.getInteraction().getHook().sendMessage("You are not in a voice-channel, please join your own lobby voice-channel and try again!").queue();
        }
    }

    @JDASlashCommand(name = "lobby", subcommand = "delete", description = "Delete your lobby!")
    public void onLobbyDelete(
            GuildSlashEvent event
    ) {
        event.deferReply(true).queue();

        var channelIdOptional = Main.getTempVoiceChannelAndOwnerIds().entrySet().stream().filter(x -> x.getValue() == event.getMember().getIdLong()).findFirst();
        if (channelIdOptional.isPresent()) {
            var channel = event.getJDA().getVoiceChannelById(channelIdOptional.get().getKey());
            if (channel != null) {
                channel.delete().queue();
                event.getInteraction().getHook().sendMessage("Your lobby has been deleted.").queue();
                Main.getTempVoiceChannelAndOwnerIds().remove(channel.getIdLong());
            } else {
                event.getInteraction().getHook().sendMessage("Your lobby doesn't exist anymore???").queue();
                Main.getTempVoiceChannelAndOwnerIds().remove(channelIdOptional.get().getKey());
            }
        } else {
            event.getInteraction().getHook().sendMessage("You don't have a lobby!").queue();
        }
    }

    @NotNull
    @Override
    public List<Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, @NotNull String option) {
        if (commandPath.getName().equalsIgnoreCase("lobby") && commandPath.getSubname() != null && commandPath.getSubname().equalsIgnoreCase("create")) {
            return switch (option.toLowerCase()) {
                case "location" -> List.of(
                        new Choice("EU", "EU"), new Choice("ASIA", "Asia"),
                        new Choice("AMERICA", "America"), new Choice("AUSTRALIA", "Australia"),
                        new Choice("ANTARCTIC", "Antarctic")
                );
                case "vpnsoftware" -> List.of(
                        new Choice("rAdmin", "rAdmin"), new Choice("ZeroTier", "ZeroTier"),
                        new Choice("Hamachi", "Hamachi"), new Choice("TailScale", "TailScale"),
                        new Choice("Other", "other"), new Choice("None", "none")
                );
                default -> List.of();
            };
        }
        return List.of();
    }
}
