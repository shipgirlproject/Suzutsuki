package suzutsuki.util;

import org.json.JSONObject;
import org.json.JSONTokener;
import suzutsuki.SuzutsukiClient;
import suzutsuki.struct.PatreonTiers;
import suzutsuki.struct.StaticUserIds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class SuzutsukiConfig {
    public final int port;
    public final int threads;
    public final String commandPrefix;
    public final String token;
    public final String pass;
    public final String routePrefix;
    public final String guildID;
    public final String annoucementChannelID;
    public final String donatorRoleID;
    public final String boomersRoleID;
    public final String patreonGlobalRoleID;
    public final PatreonTiers patreonTiers;
    public final StaticUserIds statisIds;

    public SuzutsukiConfig() throws FileNotFoundException, URISyntaxException {
        File file = new File(SuzutsukiClient.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String directory = file.getPath().replace(file.getName(), "");
        InputStream is = new FileInputStream(directory + "config.json");
        JSONObject config = new JSONObject(new JSONTokener(is));
        port = config.getInt("port");
        threads = config.getInt("threads");
        commandPrefix = config.getString("commandPrefix");
        token = config.getString("token");
        pass = config.getString("pass");
        routePrefix = config.getString("routePrefix");
        guildID = config.getString("guildID");
        annoucementChannelID = config.getString("annoucementChannelID");
        donatorRoleID = config.getString("donatorRoleID");
        boomersRoleID = config.getString("boomersRoleID");
        patreonGlobalRoleID = config.getString("patreonGlobalRoleID");
        patreonTiers = new PatreonTiers(config.getJSONObject("patreonTiers"));
        statisIds = new StaticUserIds(config.getJSONObject("miscIds"));
    }
}
