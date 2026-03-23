package de.presti.smphelper;

import de.presti.smphelper.dto.Punishments;
import de.presti.smphelper.utils.Config;
import de.presti.smphelper.utils.ResourceUtil;
import io.github.freya022.botcommands.api.core.BotCommands;
import de.presti.smphelper.dto.CrashReport;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
import net.dv8tion.jda.api.utils.FileUpload;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.hibernate.service.ServiceRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Getter
public class Main {

    @Getter
    private static long guildId = 673246641066737674L;

    @Getter
    private static long initialChannel = 1116305428050104340L;

    @Getter
    private static long forumChannelId = 1409833736756793425L,
            testerForumChannelId = 1475510715120095325L,
            testerReportChannelId = 1325356201076330496L,
            respondToMessageCategory = 1321252725291483137L,
            temporalVoiceCategory = 1324598005944422400L;

    @Getter
    private static long currentIndex = 0;

    @Getter
    private final static List<CrashReport> crashReports = new ArrayList<>();

    @Getter
    private final static List<Punishments> punishments = new ArrayList<>();

    @Getter
    private static HashMap<Long, Long> tempVoiceChannelAndOwnerIds = new HashMap<>();

    @Getter
    private final static List<String> releaseTrigger = new ArrayList<>();

    public static void main(String[] args) {
        Config.getInstance();

        guildId = Config.getInstance().getGuildId();
        forumChannelId = Config.getInstance().getForumChannelId();
        testerForumChannelId = Config.getInstance().getTesterForumChannelId();
        initialChannel = Config.getInstance().getChannelId();
        testerReportChannelId = Config.getInstance().getTesterReportChannelId();
        respondToMessageCategory = Config.getInstance().getRespondToMessageCategory();
        currentIndex = Config.getInstance().getCurrentIndex();
        temporalVoiceCategory = Config.getInstance().getTemporalVoiceCategory();

        if (Config.getInstance().getReportList() != null) {
            crashReports.addAll(Config.getInstance().getReportList());
        }

        if (Config.getInstance().getPunishmentsList() != null) {
            punishments.addAll(Config.getInstance().getPunishmentsList());
        }

        if (Config.getInstance().getTempVoiceChannelAndOwnerIds() != null) {
            tempVoiceChannelAndOwnerIds = new HashMap<>(Config.getInstance().getTempVoiceChannelAndOwnerIds());
        }

        if (Config.getInstance().getReleaseTrigger() != null) {
            releaseTrigger.addAll(Config.getInstance().getReleaseTrigger());
        }

        log.info("Loaded config!");

        BotCommands.create(builder -> {
            builder.addPredefinedOwners(Config.getInstance().getDevUserId());
            builder.addSearchPath("de.presti.smphelper.utils");
            builder.addSearchPath("de.presti.smphelper.service");
            builder.addSearchPath("de.presti.smphelper.commands");

            builder.textCommands(textCommands -> textCommands.usePingAsPrefix(true));
        });

        log.info("JDA started!");
    }

    /**
     * Build a new SessionFactory.
     *
     * @return The SessionFactory.
     */
    public static SessionFactory buildSessionFactory() {

        try {
            Configuration configuration = new Configuration().addProperties(ResourceUtil.getResourceAsProperties("hibernate.properties"));

            Set<Class<?>> classSet = new Reflections(
                    ConfigurationBuilder
                            .build()
                            .forPackage("de.presti.smphelper.dto", ClasspathHelper.staticClassLoader()))
                    .getTypesAnnotatedWith(Table.class);

            if (classSet.isEmpty()) {
                log.error("No Entities found!");
            }

            classSet.forEach(configuration::addAnnotatedClass);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();

            return configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            log.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <R> Optional<R> getEntity(@NotNull R r, @NotNull String sqlQuery, @Nullable Map<String, Object> parameters) {
        sqlQuery = sqlQuery.isEmpty() ? "FROM " + r.getClass().getSimpleName() : sqlQuery;

        try (SessionFactory sessionFactory = buildSessionFactory();
                Session session = sessionFactory.openSession()) {

            session.beginTransaction();

            Query<R> query = (Query<R>) session.createQuery(sqlQuery, r.getClass());

            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }

            session.getTransaction().commit();

            return Optional.ofNullable(query.setMaxResults(1).getSingleResultOrNull());
        } catch (Exception exception) {
            log.error("Failed to get Entity", exception);
            throw exception;
        }
    }

    public static <R> R updateEntity(R r) {
        if (r.getClass().isAssignableFrom(Optional.class)) {
            log.error("Calling the UpdateEntity Method with a Optional is not supported.");
            return null;
        }

        try (SessionFactory sessionFactory = buildSessionFactory();
             Session session = sessionFactory.openSession()) {

            session.beginTransaction();

            R newEntity = session.merge(r);

            session.getTransaction().commit();

            return newEntity;
        }
    }

    public static <R> void deleteEntity(R r) {
        if (r == null) return;

        if (r.getClass().isAssignableFrom(Optional.class)) {
            log.error("Calling the UpdateEntity Method with a Optional is not supported.");
            return;
        }

        try (SessionFactory sessionFactory = buildSessionFactory();
             Session session = sessionFactory.openSession()) {

            session.beginTransaction();

            session.remove(r);

            session.getTransaction().commit();
        } catch (Exception exception) {
            log.error("Failed to delete Entity", exception);
            throw exception;
        }
    }

    public static void setTags(ForumChannel channel) {
        List<ForumTagData> toAdd = new ArrayList<>(channel.getAvailableTags().stream().map(ForumTagData::from).toList());

        if (toAdd.stream().noneMatch(x -> x.getName().equalsIgnoreCase("open"))) {
            toAdd.add(new ForumTagData("Open").setEmoji(Emoji.fromUnicode("🟩")).setModerated(true));
        }

        if (toAdd.stream().noneMatch(x -> x.getName().equalsIgnoreCase("Working on fix"))) {
            toAdd.add(new ForumTagData("Working on fix").setEmoji(Emoji.fromUnicode("🟧")).setModerated(true));
        }

        if (toAdd.stream().noneMatch(x -> x.getName().equalsIgnoreCase("closed"))) {
            toAdd.add(new ForumTagData("Closed").setEmoji(Emoji.fromUnicode("🟥")).setModerated(true));
        }

        channel.getManager().setAvailableTags(toAdd).queue();
    }

    public static void addTag(ForumChannel channel, String tagName) {
        List<ForumTagData> toAdd = new ArrayList<>(channel.getAvailableTags().stream().map(ForumTagData::from).toList());
        if (toAdd.stream().noneMatch(x -> x.getName().equalsIgnoreCase(tagName))) {
            toAdd.add(new ForumTagData(tagName).setEmoji(Emoji.fromUnicode("💥")).setModerated(true));
        }
        channel.getManager().setAvailableTags(toAdd).complete();
    }

}
