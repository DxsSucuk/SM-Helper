package de.presti.smphelper.commands;

import io.github.freya022.botcommands.api.commands.CommandPath;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.Cooldown;
import io.github.freya022.botcommands.api.commands.application.SlashOptionChoiceProvider;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Command
@Cooldown(cooldown = 5, unit = ChronoUnit.SECONDS)
public class LobbyCommand implements SlashOptionChoiceProvider {

    @TopLevelSlashCommandData(description = "Host lobbies!")
    @JDASlashCommand(name = "lobby", subcommand = "create", description = "Create a lobby!")
    public void onLobbyRequest(
            GuildSlashEvent event,
            @SlashOption(description = "Is this lobby invite-only?", name = "private") boolean privateLobby,
            @SlashOption(description = "Whats your location? (EU, ASIA, AMERICA, Northpole??, SPACE??????)", name = "location") String location,
            @SlashOption(description = "If any, what VPN software should be used to connect? (rAdmin, ZeroTier, Hamachi, TailScale)", name = "vpnsoftware") String vpnSoftware,
            @SlashOption(description = "If vpn is used, define connection stuff needed! (ZeroTier -> NetId, rAdmin -> name:pass)", name = "login") String vpnLogin,
            @SlashOption(description = "If mods are used, please name all of them, or a link to a pastebin with the names or zip!", name = "mods") String modList

    ) {
        event.deferReply(true).queue();
        boolean usesVPN = !(vpnSoftware == null || vpnSoftware.equalsIgnoreCase("none"));
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(event.getUser().getGlobalName() + "'s lobby");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField("**Location**", location, true);
        embedBuilder.addField("**Mods**", modList, true);
        if (usesVPN) {
            embedBuilder.addBlankField(true);
            embedBuilder.addField("**VPN**", "", false);
            embedBuilder.addField("**Software**", vpnSoftware, false);
            embedBuilder.addField("**Login**", vpnLogin, false);
        }
        // TODO:: make a thingy with buttons.
        event.getInteraction().getHook().sendMessage("Work done!").queue();
    }

    @NotNull
    @Override
    public List<Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, @NotNull String option) {
        if (commandPath.getName().equalsIgnoreCase("lobby") && commandPath.getSubname() != null && commandPath.getSubname().equalsIgnoreCase("create")) {
            return switch (option.toLowerCase()) {
                case "location" -> List.of(
                        new Choice("EU", "eu"), new Choice("ASIA", "asia"),
                        new Choice("AMERICA", "america"), new Choice("AUSTRALIA", "australia"),
                        new Choice("ANTARCTIC", "antarctic")
                );
                case "vpnsoftware" -> List.of(
                        new Choice("rAdmin", "radmin"), new Choice("ZeroTier", "zeroTier"),
                        new Choice("Hamachi", "hamachi"), new Choice("TailScale", "tailScale"),
                        new Choice("Other", "other")
                );
                default -> List.of();
            };
        }
        return List.of();
    }
}
