package suzutsuki.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import suzutsuki.discord.events.MessageReceived;
import suzutsuki.discord.events.Ready;
import suzutsuki.struct.config.SuzutsukiConfig;
import suzutsuki.util.Threads;

import javax.security.auth.login.LoginException;

public class SuzutsukiDiscord {
    public static JDA create(SuzutsukiConfig config, Logger logger, Threads threads) throws LoginException, InterruptedException {
        JDA client = JDABuilder.createDefault(config.tokens.getBot())
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT
            )
            .disableCache(
                CacheFlag.ACTIVITY,
                CacheFlag.CLIENT_STATUS,
                CacheFlag.EMOJI,
                CacheFlag.FORUM_TAGS,
                CacheFlag.ONLINE_STATUS,
                CacheFlag.ROLE_TAGS,
                CacheFlag.SCHEDULED_EVENTS,
                CacheFlag.STICKER,
                CacheFlag.VOICE_STATE
            )
            .setChunkingFilter(ChunkingFilter.ALL)
            .build();

        logger.info("JDA Client built!");

        client.addEventListener(
            new Ready(client, logger, threads),
            new MessageReceived(client, logger, threads, config)
        );

        logger.info("Event Listeners are loaded!");

        client.awaitReady();

        return client;
    }
}