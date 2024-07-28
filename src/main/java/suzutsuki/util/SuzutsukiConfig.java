package suzutsuki.util;

import org.json.JSONObject;
import org.json.JSONTokener;
import suzutsuki.SuzutsukiClient;
import suzutsuki.struct.rest.StaticUserIds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class SuzutsukiConfig {
    public final int port;
    public final int threads;
    public final String prefix;
    public final String token;
    public final String pass;
    public final String patreon;
    public final String guildId;
    public final String annoucementChannelId;
    public final String donatorRoleId;
    public final String boostersRoleId;
    public final StaticUserIds staticIds;

    public SuzutsukiConfig() throws FileNotFoundException, URISyntaxException {
        File file = new File(SuzutsukiClient.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String directory = file.getPath().replace(file.getName(), "");
        InputStream is = new FileInputStream(directory + "config.json");
        JSONObject config = new JSONObject(new JSONTokener(is));
        this.port = config.getInt("port");
        this.threads = config.getInt("threads");
        this.prefix = config.getString("prefix");
        this.token = config.getString("token");
        this.pass = config.getString("pass");
        this.patreon = config.getString("patreon");
        this.guildId = config.getString("guildId");
        this.annoucementChannelId = config.getString("annoucementChannelId");
        this.donatorRoleId = config.getString("donatorRoleId");
        this.boostersRoleId = config.getString("boostersRoleId");
        this.staticIds = new StaticUserIds(config.getJSONObject("miscIds"));
    }
}
