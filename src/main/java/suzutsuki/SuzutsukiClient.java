package suzutsuki;

import suzutsuki.discord.SuzutsukiDiscord;
import suzutsuki.server.SuzutsukiServer;
import suzutsuki.util.SuzutsukiConfig;

public class SuzutsukiClient {
    public static void main(String[] args) throws Exception {
        System.setProperty("vertx.disableDnsResolver", "true");
        SuzutsukiConfig suzutsukiConfig = new SuzutsukiConfig();
        SuzutsukiDiscord suzutsukiDiscord = new SuzutsukiDiscord(suzutsukiConfig)
                .loadSuzutsuki()
                .awaitForReady();
        new SuzutsukiServer(suzutsukiDiscord, suzutsukiConfig)
                .loadRoutes()
                .startServer();
    }
}
