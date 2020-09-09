package suzutsuki.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import suzutsuki.discord.events.GuildMemberRoleAdd;
import suzutsuki.discord.events.GuildMessage;
import suzutsuki.discord.events.Ready;
import suzutsuki.util.SuzutsukiConfig;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SuzutsukiDiscord {
    private final SuzutsukiConfig suzutsukiConfig;
    public final ScheduledExecutorService scheduler;
    public final JDA client;

    public SuzutsukiDiscord(SuzutsukiConfig suzutsukiConfig) throws LoginException {
        this.suzutsukiConfig = suzutsukiConfig;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.client = JDABuilder.createDefault(suzutsukiConfig.token)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .disableIntents(
                        GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MESSAGE_TYPING,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_EMOJIS,
                        GatewayIntent.GUILD_BANS
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
        return this;
    }

    public SuzutsukiDiscord awaitForReady() throws InterruptedException {
        client.awaitReady();
        return this;
    }
}