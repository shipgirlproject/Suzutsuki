package suzutsuki.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import suzutsuki.discord.events.GuildMessage;
import suzutsuki.discord.events.Ready;
import suzutsuki.util.SuzutsukiConfig;

import javax.security.auth.login.LoginException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class SuzutsukiDiscord {
    public static JDA create(SuzutsukiConfig config, Logger logger, ExecutorService executor, ScheduledExecutorService scheduler) throws LoginException, InterruptedException {
        JDA client = JDABuilder.createDefault(config.token)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES )
            .disableCache(
                CacheFlag.ACTIVITY,
                CacheFlag.EMOTE,
                CacheFlag.VOICE_STATE)
            .setChunkingFilter(ChunkingFilter.ALL)
            .build();

        logger.info("JDA Client built!");

        client.addEventListener(
            new Ready(client, logger, scheduler),
            new GuildMessage(config, client, executor)
        );

        logger.info("Event Listeners are loaded!");

        client.awaitReady();

        return client;
    }
}