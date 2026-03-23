package de.presti.smphelper.commands;

import de.presti.smphelper.Main;
import de.presti.smphelper.dto.BlacklistedWord;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.Cooldown;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import net.dv8tion.jda.api.Permission;

import java.time.temporal.ChronoUnit;

@Command
@Cooldown(cooldown = 5, unit = ChronoUnit.SECONDS)
public class AdminCommand {

    @TopLevelSlashCommandData(description = "Manage release trigger!")
    @JDASlashCommand(name = "admin", group = "releaseclarifytrigger", subcommand = "add", description = "Add trigger!")
    public void onAdminRequestClarifyAdd(
            GuildSlashEvent event,
            @SlashOption(description = "The trigger text that should be added", name = "text") String triggerText
    ) {
        event.deferReply(true).queue();
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.getInteraction().getHook().sendMessage("No permissions for this, get your ass out of here!").queue();
        }

        Main.updateEntity(new BlacklistedWord(triggerText));
        event.getInteraction().getHook().sendMessage("Work done!").queue();
    }

    @JDASlashCommand(name = "admin", group = "releaseclarifytrigger", subcommand = "remove", description = "Remove trigger!")
    public void onAdminRequestClarifyRemove(
            GuildSlashEvent event,
            @SlashOption(description = "The trigger text that should be removed", name = "text") String triggerText
    ) {
        event.deferReply(true).queue();

        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.getInteraction().getHook().sendMessage("No permissions for this, get your ass out of here!").queue();
        }

        Main.deleteEntity(new BlacklistedWord(triggerText));
        event.getInteraction().getHook().sendMessage("Work done!").queue();
    }
}