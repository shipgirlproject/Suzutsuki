package suzutsuki.struct.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class PatreonTierConfig {
    public String name;
    public String discordRoleId;
    public String patreonTierId;
}
