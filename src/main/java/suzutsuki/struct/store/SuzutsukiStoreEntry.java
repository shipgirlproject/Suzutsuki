package suzutsuki.struct.store;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuzutsukiStoreEntry {
	public String guildId;
	public String userId;
}
