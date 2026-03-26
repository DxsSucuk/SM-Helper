package de.presti.smphelper.utils;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;

import java.util.Objects;

public class ComponentUtil {

    private static Container commonIssueContainer, releaseContainer, needSupportContainer;

    public static Container createInitialMessageForReport() {
        return Container.of(
                Section.of(
                        Thumbnail.fromFile(ResourceUtil.getResourceAsFileUpload("/minispideysad.png")),
                        TextDisplay.of("## How to report a crash"),
                        TextDisplay.of("Simple guide to help your report crashes efficiently!")
                ),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("## What do I need before reporting?"),
                TextDisplay.of("You will need to find your crash log and DMP file!"),
                TextDisplay.of("Both of these files should be in your SMT/Logs folder!"),

                Separator.createDivider(Separator.Spacing.LARGE),

                TextDisplay.of("## What now?"),
                TextDisplay.of("Just press the \"Report bug!\" button and follow the instructions!"),

                Separator.createDivider(Separator.Spacing.SMALL),
                ActionRow.of(Button.of(ButtonStyle.DANGER, "open_report_modal", "Report bug!"))
        );
    }

    public static Container createCommonIssues() {
        return Objects.requireNonNullElseGet(commonIssueContainer, () -> commonIssueContainer = Container.of(
                Section.of(
                        Thumbnail.fromFile(ResourceUtil.getResourceAsFileUpload("/minispidey.png")),
                        TextDisplay.of("## Common Issues with the Mod!"),
                        TextDisplay.of("Below you will find common Issues with the mod ways to solve them!")
                ),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("## Stuck in the loading screen?"),
                TextDisplay.of("The host needs to \"restart from checkpoint\"!"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("## Camera UI stuck on my screen :c"),
                TextDisplay.of("Death shall fix your Issue!"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("## Throws are not synced!"),
                TextDisplay.of("This can lead to softlocks in certain content, abandon the mission if it occurs."),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("## Player dialog does not play!"),
                TextDisplay.of("Spider-Man’s throwing animation is not synced between players (Throwing Weaponry, yanking scaffoldings, and throwing weapons is synced)."),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("## Animations are locked?"),
                TextDisplay.of("Other Spider-Men animations may get locked after the completion of a fisk or prisoner base. (Restart checkpoint to fix this issue)."),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("## My Issue is not listed !!!!!"),
                TextDisplay.of("When in doubt, restart checkpoint!"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("## Latest build where?"),
                ActionRow.of(Button.of(ButtonStyle.LINK, "https://www.patreon.com/c/hbgda", "Patreon"))
        ));
    }

    public static Container createReleaseContainer() {
        return Objects.requireNonNullElseGet(releaseContainer,
                () -> releaseContainer = Container.of(createReleaseSection()));
    }

    public static Section createReleaseSection() {
        return Section.of(
                Thumbnail.fromFile(ResourceUtil.getResourceAsFileUpload("/minispideysad.png")),
                TextDisplay.of("## When release??????????"),
                TextDisplay.of("Open Beta will release once the roadmap goals have been reached!"),
                TextDisplay.of("Check <#1355809528750543000> for more info, and refrain from asking this everyday <3")
        );
    }

    public static Container createNeedSupportContainer() {
        return Objects.requireNonNullElseGet(needSupportContainer,
                () -> needSupportContainer = Container.of(createNeedSupportSection()));
    }

    public static Section createNeedSupportSection() {
        return Section.of(
                Thumbnail.fromFile(ResourceUtil.getResourceAsFileUpload("/minispideysad.png")),
                TextDisplay.of("## Need help with the mod?"),
                TextDisplay.of("Please connect your Patreon with Discord and check out <#1410107482998571048> for help!")
        );
    }

}
