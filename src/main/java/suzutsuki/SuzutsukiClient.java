package suzutsuki;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suzutsuki.discord.SuzutsukiDiscord;
import suzutsuki.server.SuzutsukiServer;
import suzutsuki.util.SuzutsukiConfig;

public class SuzutsukiClient {
    public static void main(String[] args) throws Exception {
        System.setProperty("vertx.disableDnsResolver", "true");
        SuzutsukiConfig suzutsukiConfig = new SuzutsukiConfig();
        Logger suzutsukiLog = LoggerFactory.getLogger(SuzutsukiClient.class);
        SuzutsukiDiscord suzutsukiDiscord = new SuzutsukiDiscord(suzutsukiConfig,  suzutsukiLog)
                .loadSuzutsuki()
                .awaitForReady();
        new SuzutsukiServer(suzutsukiDiscord, suzutsukiConfig, suzutsukiLog)
                .loadRoutes()
                .startServer();
    }
}
