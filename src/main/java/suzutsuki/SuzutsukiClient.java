package suzutsuki;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import net.dv8tion.jda.api.JDA;
import suzutsuki.database.SuzutsukiStore;
import suzutsuki.discord.SuzutsukiDiscord;
import suzutsuki.server.SuzutsukiServer;
import suzutsuki.struct.config.SuzutsukiConfig;
import suzutsuki.util.SuzutsukiPatreonClient;
import suzutsuki.util.SuzutsukiRoleManager;
import suzutsuki.util.Threads;

import java.io.File;

public class SuzutsukiClient {
    public static void main(String[] args) throws Exception {
        File file = new File(SuzutsukiClient.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String directory = file.getPath().replace(file.getName(), "");

        System.setProperty("vertx.disableDnsResolver", "true");

        SuzutsukiConfig config = SuzutsukiConfig.loadConfig(directory);
        Logger logger = LoggerFactory.getLogger(SuzutsukiClient.class);
        Threads threads = new Threads(config, logger);
        Vertx vertx = Vertx.vertx(new VertxOptions());
        SuzutsukiStore store = new SuzutsukiStore(logger, directory);
        SuzutsukiPatreonClient patreon = new SuzutsukiPatreonClient(vertx, logger, store, config, threads);
        JDA client = SuzutsukiDiscord.create(config, logger, store, patreon, threads);
        SuzutsukiRoleManager roles = new SuzutsukiRoleManager(client, logger, threads, patreon, config);

        new SuzutsukiServer(vertx, client, logger, patreon, store, roles, config);
    }
}
