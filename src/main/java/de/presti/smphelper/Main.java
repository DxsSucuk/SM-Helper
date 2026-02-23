package de.presti.smphelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.presti.smphelper.listener.ComponentListener;
import de.presti.smphelper.listener.MessageListener;
import de.presti.smphelper.utils.Config;
import de.presti.smphelper.utils.CrashReport;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Getter
public class Main {

    private static JDA botInstance = null;

    @Getter
    private static long guildId = 673246641066737674L;

    @Getter
    private static long initialChannel = 1116305428050104340L;

    @Getter
    private static long forumChannelId = 1409833736756793425L, testerForumChannelId = 1475510715120095325L, testerReportChannelId = 1325356201076330496L, respondToMessageCategory = 1321252725291483137L;

    @Getter
    private static long currentIndex = 0;

    @Getter
    private static Config config;

    @Getter
    private static List<CrashReport> crashReports = new ArrayList<>();

    private static final Path configPath = Path.of("config.json");

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            log.error("Please provide a command line argument");
            log.error("Usage: java -jar smhelper.jar <bot-token>");
            return;
        }

        if (!Files.exists(configPath)) {
            log.error("Config file not found! Shutdown...");
            return;
        }

        Gson gson = new Gson();
        try {
            config = gson.fromJson(Files.readString(configPath), Config.class);
        } catch (Exception exception) {
            log.error("Failed to read config file! Shutdown...");
            return;
        }

        guildId = config.getGuildId();
        forumChannelId = config.getForumChannelId();
        testerForumChannelId = config.getTesterForumChannelId();
        initialChannel = config.getChannelId();
        testerReportChannelId = config.getTesterReportChannelId();
        respondToMessageCategory = config.getRespondToMessageCategory();

        if (config.getReportList() != null)
            crashReports.addAll(config.getReportList());

        log.info("Loaded config!");

        botInstance = JDABuilder.createLight(args[0])
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableCache(CacheFlag.FORUM_TAGS)
                .addEventListeners(new ComponentListener(), new MessageListener()).build();
        botInstance.awaitReady();

        log.info("JDA started!");

        var guild = botInstance.getGuildById(guildId);
        if (guild == null) {
            System.out.println("Could not find guild with ID " + guildId);
            return;
        }

        guild.updateCommands().addCommands(
                Commands.message("Upload file to report"),
                Commands.slash("send", "Send initial messages (DEV STUFF)")
                        .addOption(OptionType.INTEGER, "typ", "The typ of message which should be send again.", true)
                        .addOption(OptionType.CHANNEL, "channel", "The channel which should receive the message.", true),
                Commands.slash("forum", "Set the Forum tags (DEV STUFF)")
                        .addOption(OptionType.CHANNEL, "channel", "The Forum channel which should receive the tags.", true)
        ).queue();

        if (config.isSendInitialMessage()) {
            var channel = botInstance.getTextChannelById(config.getChannelId());
            if (channel != null) {
                channel.sendMessageComponents(createInitialMessageForReport()).useComponentsV2().queue();
            }

            channel = botInstance.getTextChannelById(config.getTesterReportChannelId());
            if (channel != null) {
                channel.sendMessageComponents(createInitialMessageForReport()).useComponentsV2().queue();
            }
        }

        if (config.isSetInitialForumTag()) {
            var channel = botInstance.getForumChannelById(config.getForumChannelId());
            if (channel != null) {
                setTags(channel);
            }

            channel = botInstance.getForumChannelById(config.getTesterForumChannelId());
            if (channel != null) {
                setTags(channel);
            }
        }
    }

    public static void setTags(ForumChannel channel) {
        channel.getManager().setAvailableTags(List.of(
                new ForumTagData("Open").setModerated(true).setEmoji(Emoji.fromUnicode("🟩")),
                new ForumTagData("Working on fix").setModerated(true).setEmoji(Emoji.fromUnicode("🟧")),
                new ForumTagData("Closed").setModerated(true).setEmoji(Emoji.fromUnicode("🟥")))).queue();
    }

    public static Container createInitialMessageForReport() {
        return Container.of(
                Section.of(
                        Thumbnail.fromFile(getResourceAsFileUpload("/minispideysad.png")),
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

    private static Container commonIssueContainer, releaseContainer, needSupportContainer;

    public static Container createCommonIssues() {
        return Objects.requireNonNullElseGet(commonIssueContainer, () -> commonIssueContainer = Container.of(
                Section.of(
                        Thumbnail.fromFile(getResourceAsFileUpload("/minispidey.png")),
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
        return Objects.requireNonNullElseGet(releaseContainer, () -> releaseContainer = Container.of(
                Section.of(
                        Thumbnail.fromFile(getResourceAsFileUpload("/minispideysad.png")),
                        TextDisplay.of("## When release??????????"),
                        TextDisplay.of("Open Beta will release once the roadmap goals have been reached!"),
                        TextDisplay.of("Check <#1355809528750543000> for more info, and refrain from asking this everyday <3")
                )
        ));
    }

    public static Container createNeedSupportContainer() {
        return Objects.requireNonNullElseGet(needSupportContainer, () -> needSupportContainer = Container.of(
                Section.of(
                        Thumbnail.fromFile(getResourceAsFileUpload("/minispideysad.png")),
                        TextDisplay.of("## Need help with the mod?"),
                        TextDisplay.of("Please connect your Patreon with Discord and check out <#1410107482998571048> for help!")
                )
        ));
    }

    public static void setCurrentIndex(long index) {
        currentIndex = index;
        config.setCurrentIndex(currentIndex);
        saveConfig();
    }

    public static void addCrashReport(CrashReport crashReport) {
        if (getCrashReports().stream().noneMatch(x -> x.getChannelId() == crashReport.getChannelId())) {
            crashReports.add(crashReport);
            saveCrashReports();
        }
    }

    public static void saveCrashReports() {
        config.setReportList(getCrashReports());
        saveConfig();
    }

    public static void saveConfig() {
        try {
            Files.writeString(configPath, new GsonBuilder().setPrettyPrinting().create().toJson(config), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to write config file!");
        }
    }

    public static boolean isCrashReport(long channelId) {
        return getCrashReports().stream().anyMatch(x -> x.getChannelId() == channelId);
    }

    public static CrashReport getCrashReportFromChanneldId(long channelId) {
        var report = getCrashReports().stream().filter(x -> x.getChannelId() == channelId).findFirst();
        return report.orElse(null);

    }

    public static FileUpload getResourceAsFileUpload(String path) {
        final int lastSeparatorIndex = path.lastIndexOf('/');
        final String fileName = path.substring(lastSeparatorIndex + 1);

        final InputStream stream = Main.class.getResourceAsStream(path);
        if (stream == null)
            throw new IllegalArgumentException("Could not find resource at: " + path);

        return FileUpload.fromData(stream, fileName);
    }
}
