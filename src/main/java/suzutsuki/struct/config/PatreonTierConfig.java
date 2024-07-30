package suzutsuki.struct.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class PatreonTierConfig {
    public int order;
    public String name;
    public String discordRoleId;
    public String patreonTierId;
}
