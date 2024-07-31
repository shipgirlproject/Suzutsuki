package suzutsuki;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import net.dv8tion.jda.api.JDA;
import suzutsuki.discord.SuzutsukiDiscord;
import suzutsuki.server.SuzutsukiServer;
import suzutsuki.struct.config.SuzutsukiConfig;
import suzutsuki.util.SuzutsukiPatreonClient;
import suzutsuki.util.SuzutsukiRoleManager;
import suzutsuki.util.Threads;

public class SuzutsukiClient {
    public static void main(String[] args) throws Exception {
        System.setProperty("vertx.disableDnsResolver", "true");
        
        SuzutsukiConfig config = SuzutsukiConfig.loadConfig();

        Logger logger = LoggerFactory.getLogger(SuzutsukiClient.class);

        Threads threads = new Threads(config, logger);

        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(config.threads));

        SuzutsukiPatreonClient patreon = new SuzutsukiPatreonClient(vertx, logger, config, threads);

        JDA client = SuzutsukiDiscord.create(config, logger, threads);
        
        new SuzutsukiRoleManager(client, logger, threads, patreon, config);
        new SuzutsukiServer(vertx, client, logger, patreon, config);
    }
}
