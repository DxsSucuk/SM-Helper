package de.presti.smphelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.presti.smphelper.listener.ComponentListener;
import de.presti.smphelper.listener.MessageListener;
import de.presti.smphelper.utils.Config;
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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
    private static long forumChannelId = 1409833736756793425L;

    @Getter
    private static long currentIndex = 0;

    @Getter
    private static Config config;

    private static final Path configPath = Path.of("config.json");

    public static final String pattern = "^crash-(\\d+)-(\\d{17,19})$";

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
        initialChannel = config.getChannelId();

        botInstance = JDABuilder.createLight(args[0]).enableIntents(GatewayIntent.MESSAGE_CONTENT).addEventListeners(new ComponentListener(), new MessageListener()).build();
        botInstance.awaitReady();

        var guild = botInstance.getGuildById(guildId);
        if (guild == null) {
            System.out.println("Could not find guild with ID " + guildId);
            return;
        }

        guild.updateCommands().addCommands(
                Commands.message("Upload file to report"),
                Commands.slash("send", "Send initial messages (DEV STUFF)")
                        .addOption(OptionType.INTEGER, "typ", "The tpy of message which should be send again.", true)
                        .addOption(OptionType.CHANNEL, "channel", "The channel which should receive the message.", true)
                ).queue();

        if (config.isSendInitialMessage()) {
            var channel = botInstance.getTextChannelById(config.getChannelId());
            if (channel != null) {
                channel.sendMessageComponents(createInitialMessageForReport()).useComponentsV2().queue();
            }
        }
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

    private static Container commonIssueContainer;

    public static Container createCommonIssues() {
        return Objects.requireNonNullElseGet(commonIssueContainer, () -> commonIssueContainer = Container.of(
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
        ));
    }

    public static void setCurrentIndex(long index) {
        currentIndex = index;
        config.setCurrentIndex(currentIndex);
        try {
            Files.writeString(configPath, new GsonBuilder().setPrettyPrinting().create().toJson(config), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to write config file!");
        }
    }

    public static FileUpload getResourceAsFileUpload(String path)
    {
        final int lastSeparatorIndex = path.lastIndexOf('/');
        final String fileName = path.substring(lastSeparatorIndex + 1);

        final InputStream stream = Main.class.getResourceAsStream(path);
        if (stream == null)
            throw new IllegalArgumentException("Could not find resource at: " + path);

        return FileUpload.fromData(stream, fileName);
    }
}
