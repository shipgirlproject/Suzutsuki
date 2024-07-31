package suzutsuki.struct.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import suzutsuki.SuzutsukiClient;

@JsonAutoDetect
public class SuzutsukiConfig {
    public int port;
    public int threads;
    public Tokens tokens;
    public Disable disable;
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
        String directory = file.getPath().replace(file.getName(), "config.json");
        try (InputStream stream = new FileInputStream(directory)) {
            byte[] bytes = stream.readAllBytes();
            JsonObject config = new JsonObject(new String(bytes, StandardCharsets.UTF_8));
            return config.mapTo(SuzutsukiConfig.class);
        }
    }
}
