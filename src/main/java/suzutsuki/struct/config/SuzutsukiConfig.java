package suzutsuki.struct.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class SuzutsukiConfig {
    private int color;
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
    public int storeCleanDelayTimeMinutes;
    public List<PatreonTierConfig> patreonTiers;
    public List<MockPatreon> mockPatreons;
    public Boolean patreonCheckHonorsRole;
    public List<String> avatarUserIds;

    @JsonGetter("color")
    public int getColor() { return this.color; }

    @JsonSetter("color")
    public int setColor(String color) { return this.color = Integer.decode(color); }

    public static SuzutsukiConfig loadConfig(String directory) throws URISyntaxException, IOException {
        try (InputStream stream = new FileInputStream(directory + "config.json")) {
            byte[] bytes = stream.readAllBytes();
            JsonObject config = new JsonObject(new String(bytes, StandardCharsets.UTF_8));
            return config.mapTo(SuzutsukiConfig.class);
        }
    }
}
