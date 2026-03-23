package de.presti.smphelper.utils;

import de.presti.smphelper.Main;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ResourceUtil {

    public static String getOffsetFromLog(String log) {
        List<String> allFrameOffsets = Arrays.stream(log.split("\n"))
                .filter(x -> x.contains("Frame") && x.contains("Spider-Man.exe+")).toList();

        return allFrameOffsets.getFirst().split("Spider-Man.exe+")[1].trim();
    }

    public static FileUpload getResourceAsFileUpload(String path) {
        final int lastSeparatorIndex = path.lastIndexOf('/');
        final String fileName = path.substring(lastSeparatorIndex + 1);

        final InputStream stream = Main.class.getResourceAsStream(path);
        if (stream == null)
            throw new IllegalArgumentException("Could not find resource at: " + path);

        return FileUpload.fromData(stream, fileName);
    }

    public static Properties getResourceAsProperties(String path) {
        Properties prop = new Properties();

        final InputStream stream = Main.class.getResourceAsStream(path);
        if (stream == null)
            throw new IllegalArgumentException("Could not find resource at: " + path);

        try {
            prop.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return prop;
    }
}
