package suzutsuki.discord.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import suzutsuki.util.SuzutsukiConfig;

import java.util.concurrent.ExecutorService;

public class MessageReceived extends ListenerAdapter {
    private final SuzutsukiConfig config;
    private final JDA client;
    private final ExecutorService executor;
    
    public MessageReceived(SuzutsukiConfig config, JDA client, ExecutorService executor) {
        this.config = config;
        this.client = client;
        this.executor = executor;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        this.executor.execute(() -> this.handleMessage(event));
    }

    public void handleMessage(MessageReceivedEvent event) {
        if (event.isWebhookMessage() || !event.isFromGuild() || event.getAuthor().isBot()) return;
        String content = event.getMessage().getContentRaw();
        if (!content.startsWith(this.config.prefix)) return;
        String[] args = content.split("\\s+");
        String command = args[0].substring(this.config.prefix.length());
        /*
        / To be honest, just a little something
        */
        switch (command) {
            case "ping":
                event.getChannel().sendMessage("Pong! Took `" + this.client.getGatewayPing() + "ms`").queue();
                break;
            case "pong":
                event.getChannel().sendMessage("Ping! Took `" + this.client.getGatewayPing() + "ms`").queue();
        }
    }
}
