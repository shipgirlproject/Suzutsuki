package suzutsuki.discord.events;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import suzutsuki.discord.SuzutsukiDiscord;
import suzutsuki.util.SuzutsukiConfig;

public class GuildMessage extends ListenerAdapter {
    private final SuzutsukiDiscord suzutsukiDiscord;
    private final SuzutsukiConfig suzutsukiConfig;

    public GuildMessage(SuzutsukiDiscord suzutsukiDiscord, SuzutsukiConfig suzutsukiConfig) {
        this.suzutsukiDiscord = suzutsukiDiscord;
        this.suzutsukiConfig = suzutsukiConfig;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        // soon
    }
}
