package suzutsuki;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import net.dv8tion.jda.api.JDA;
import suzutsuki.discord.SuzutsukiDiscord;
import suzutsuki.server.SuzutsukiServer;
import suzutsuki.util.SuzutsukiConfig;
import suzutsuki.util.SuzutsukiPatreonClient;

public class SuzutsukiClient {
    public static void main(String[] args) throws Exception {
        System.setProperty("vertx.disableDnsResolver", "true");

        SuzutsukiConfig config = new SuzutsukiConfig();
        Logger logger = LoggerFactory.getLogger(SuzutsukiClient.class);
        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(config.threads));
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(config.threads);
        SuzutsukiPatreonClient patreonClient = new SuzutsukiPatreonClient(vertx, config, scheduler);
        JDA client = SuzutsukiDiscord.create(config, logger, executor, scheduler);

        new SuzutsukiServer(vertx, client, config, logger, patreonClient)
                .loadRoutes()
                .startServer();
    }
}
