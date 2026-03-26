package de.presti.smphelper.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.presti.smphelper.dto.CrashReport;
import de.presti.smphelper.dto.Punishments;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Config {
    private static Config INSTANCE = null;

    String botToken;
    long guildId;
    long channelId;
    long testerReportChannelId;
    long forumChannelId, testerForumChannelId;
    long respondToMessageCategory;
    boolean clearTemporalVCsOnStart;
    boolean sendInitialMessage;
    boolean setInitialForumTag;
    long minViolationsUntilTimeout;
    long devUserId;
    long temporalVoiceCategory;
    long lobbyShareChannel;

    boolean trustAllSsl;

    HashMap<Long, Long> tempVoiceChannelAndOwnerIds;

    @BService
    public static Config getInstance() {
        if (INSTANCE == null) {
            INSTANCE = readConfig();
        }

        return INSTANCE;
    }

    public void saveConfig() {
        try {
            Files.writeString(Path.of("config.json"), new GsonBuilder().setPrettyPrinting().create().toJson(this), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to write config file!");
        }
    }

    public static void createConfig() {
        INSTANCE = new Config();
        INSTANCE.saveConfig();
    }

    private static Config readConfig() {
        if (!Files.exists(Path.of("config.json"))) {
            log.error("Config file not found!");
            return null;
        }

        Config config = null;

        Gson gson = new Gson();
        try {
            config = gson.fromJson(Files.readString(Path.of("config.json")), Config.class);
        } catch (Exception exception) {
            log.error("Failed to read config file!");
        }

        return config;
    }
}
