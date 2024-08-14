package suzutsuki.struct.config;

import com.fasterxml.jackson.annotation.*;
import io.vertx.core.json.JsonObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@JsonAutoDetect
public class SuzutsukiConfig {
	@JsonIgnore
	public final String host = Optional.ofNullable(System.getenv("HOST")).orElse("0.0.0.0");
	@JsonIgnore
	public final int port = Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElse("1024"));
	@JsonIgnore
	public final int threads = Integer.parseInt(Optional.ofNullable(System.getenv("THREADS")).orElse(String.valueOf(Runtime.getRuntime().availableProcessors())));
	@JsonIgnore
	public final Tokens tokens = new Tokens();
	@JsonIgnore
	public final String prefix = Optional.ofNullable(System.getenv("PREFIX")).orElse("*");
	@JsonIgnore
	public final int color = Integer.decode(Optional.ofNullable(System.getenv("COLOR")).orElse("0xc0c0c0"));

	public String guildId;
	public String annoucementChannelId;
	public String donatorRoleId;
	public String boostersRoleId;
	public String patreonIgnoreRoleId;
	public String patreonGlobalRoleId;
	public List<String> disableFeatures;
	public List<PatreonTierConfig> patreonTiers;
	public List<MockPatreon> mockPatreons;
	public Boolean patreonCheckHonorsRole;
	public List<String> avatarUserIds;
	public int storeCleanDelayTimeMinutes;

	public static SuzutsukiConfig loadConfig(String directory) throws IOException {
		try (InputStream stream = new FileInputStream(directory + "config.json")) {
			byte[] bytes = stream.readAllBytes();
			JsonObject config = new JsonObject(new String(bytes, StandardCharsets.UTF_8));
			return config.mapTo(SuzutsukiConfig.class);
		}
	}
}
