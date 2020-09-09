package suzutsuki.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import suzutsuki.discord.events.GuildMemberRoleAdd;
import suzutsuki.discord.events.GuildMessage;
import suzutsuki.discord.events.Ready;
import suzutsuki.util.SuzutsukiConfig;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SuzutsukiDiscord {
    private final SuzutsukiConfig suzutsukiConfig;
    public final Logger suzutsukiLog;
    public final ScheduledExecutorService scheduler;
    public final JDA client;

    public SuzutsukiDiscord(SuzutsukiConfig suzutsukiConfig, Logger suzutsukiLog) throws LoginException {
        this.suzutsukiConfig = suzutsukiConfig;
        this.suzutsukiLog = suzutsukiLog;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.client = JDABuilder.createDefault(suzutsukiConfig.token)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES
                )
                .disableCache(
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOTE,
                        CacheFlag.VOICE_STATE
                )
                .setChunkingFilter(ChunkingFilter.ALL)
                .build();
    }

    public SuzutsukiDiscord loadSuzutsuki() {
        client.addEventListener(
                new Ready(this),
                new GuildMessage(this, suzutsukiConfig),
                new GuildMemberRoleAdd(this.suzutsukiConfig)
        );
        suzutsukiLog.info("Events are now loaded!");
        return this;
    }

    public SuzutsukiDiscord awaitForReady() throws InterruptedException {
        client.awaitReady();
        return this;
    }
}