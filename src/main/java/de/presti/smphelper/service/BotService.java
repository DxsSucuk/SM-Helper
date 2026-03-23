package de.presti.smphelper.service;

import com.neovisionaries.ws.client.WebSocketFactory;
import de.presti.smphelper.Main;
import de.presti.smphelper.listener.ComponentListener;
import de.presti.smphelper.listener.MessageListener;
import de.presti.smphelper.listener.ReadyListener;
import de.presti.smphelper.utils.Config;
import io.github.freya022.botcommands.api.core.JDAService;
import io.github.freya022.botcommands.api.core.events.BReadyEvent;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLSocketFactory;
import java.util.Set;

import static net.dv8tion.jda.api.utils.cache.CacheFlag.FORUM_TAGS;

@Slf4j
@BService
public class BotService extends JDAService {
    private final Config config;

    public BotService(Config config) {
        this.config = config;
    }

    @NotNull
    @Override
    public Set<CacheFlag> getCacheFlags() {
        return Set.of(FORUM_TAGS);
    }

    @NotNull
    @Override
    public Set<GatewayIntent> getIntents() {
        return defaultIntents(GatewayIntent.MESSAGE_CONTENT);
    }

    @Override
    public void createJDA(@NotNull BReadyEvent event, @NotNull IEventManager eventManager) {
        var builder = createLight(config.getBotToken())
                .setActivity(Activity.playing("Marvel's Spider-Man Multiplayer"))
                .setEnabledIntents(getIntents())
                .enableCache(getCacheFlags())
                .setEventManager(eventManager)
                .addEventListeners(new ComponentListener(), new MessageListener(), new ReadyListener());

        if (config.isTrustAllSsl()) {
            SSLSocketFactory sslSocketFactory;
            try {
                sslSocketFactory = Main.trustAllSSLSocketFactory();
            } catch (Exception e) {
                log.error("Failed to do the SSL cert skip", e);
                throw new RuntimeException(e);
            }

            builder = builder
                    .setHttpClientBuilder(new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, Main.trustAllCert()))
                    .setWebsocketFactory(new WebSocketFactory().setVerifyHostname(false).setSSLSocketFactory(sslSocketFactory));
        }

        builder.build();
    }
}