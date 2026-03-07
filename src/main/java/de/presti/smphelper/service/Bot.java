package de.presti.smphelper.service;

import de.presti.smphelper.listener.ComponentListener;
import de.presti.smphelper.listener.MessageListener;
import de.presti.smphelper.listener.ReadyListener;
import de.presti.smphelper.utils.Config;
import io.github.freya022.botcommands.api.core.JDAService;
import io.github.freya022.botcommands.api.core.events.BReadyEvent;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.dv8tion.jda.api.utils.cache.CacheFlag.FORUM_TAGS;

@BService
public class Bot extends JDAService {
    private final Config config;

    public Bot(Config config) {
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
        createLight(config.getBotToken())
                .setActivity(Activity.playing("Marvel's Spider-man Multiplayer"))
                .setEnabledIntents(getIntents())
                .enableCache(getCacheFlags())
                .setEventManager(eventManager)
                .addEventListeners(new ComponentListener(), new MessageListener(), new ReadyListener())
                .build();
    }
}