package suzutsuki.struct.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import suzutsuki.SuzutsukiClient;

@JsonAutoDetect
public class SuzutsukiConfig {
    public Boolean debug;
    public int port;
    public int threads;
    public Tokens tokens;
    public String prefix;
    public String guildId;
    public String annoucementChannelId;
    public String donatorRoleId;
    public String boostersRoleId;
    public String patreonIgnoreRoleId;
    public String patreonGlobalRoleId;
    public List<PatreonTierConfig> patreonTiers;
    public Boolean patreonCheckHonorsRole;
    public List<String> avatarUserIds;

    public static SuzutsukiConfig loadConfig() throws URISyntaxException, IOException {
        File file = new File(SuzutsukiClient.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String directory = file.getPath().replace(file.getName(), "");
        try (InputStream stream = new FileInputStream(directory + "config.json")) {
            JsonObject config = new JsonObject(stream.toString());
            return config.mapTo(SuzutsukiConfig.class);
        }
    }
}
