package suzutsuki.discord.events;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import suzutsuki.discord.SuzutsukiDiscord;
import suzutsuki.util.SuzutsukiConfig;

import javax.annotation.Nonnull;

public class GuildMessage extends ListenerAdapter {
    private final SuzutsukiDiscord suzutsukiDiscord;
    private final SuzutsukiConfig suzutsukiConfig;

    public GuildMessage(SuzutsukiDiscord suzutsukiDiscord, SuzutsukiConfig suzutsukiConfig) {
        this.suzutsukiDiscord = suzutsukiDiscord;
        this.suzutsukiConfig = suzutsukiConfig;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.isWebhookMessage() || event.getAuthor().isBot()) return;
        String content = event.getMessage().getContentRaw();
        if (!content.startsWith(suzutsukiConfig.commandPrefix)) return;
        String[] args = content.split("\\s+");
        String command = args[0].substring(suzutsukiConfig.commandPrefix.length());
        /*
        / To be honest, just a little something ¯\_(ツ)_/¯
        */
        switch (command) {
            case "ping":
                event.getChannel().sendMessage("Pong! Took `" + suzutsukiDiscord.client.getGatewayPing() + "ms`").queue();
                break;
            case "pong":
                event.getChannel().sendMessage("Ping! Took `" + suzutsukiDiscord.client.getGatewayPing() + "ms`").queue();
        }
    }
}
