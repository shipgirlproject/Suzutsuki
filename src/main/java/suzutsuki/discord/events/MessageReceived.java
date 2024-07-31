package suzutsuki.discord.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import suzutsuki.struct.config.SuzutsukiConfig;
import suzutsuki.util.Threads;

import org.slf4j.Logger;

public class MessageReceived extends ListenerAdapter {
    private final SuzutsukiConfig config;
    @SuppressWarnings("unused")
    private final Logger logger;
    private final JDA client;
    private final Threads threads;
    
    public MessageReceived(JDA client, Logger logger, Threads threads, SuzutsukiConfig config) {
        this.config = config;
        this.logger = logger;
        this.client = client;
        this.threads = threads;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        this.threads.virtual.execute(() -> this.handleMessage(event));
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
