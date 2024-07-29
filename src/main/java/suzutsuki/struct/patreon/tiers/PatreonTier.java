package suzutsuki.struct.patreon.tiers;

import suzutsuki.struct.config.PatreonTierConfig;

public class PatreonTier implements InterfacePatreonTier {
    private final String globalRoleId;
    private final PatreonTierConfig config;

    public PatreonTier(String globalRoleId, PatreonTierConfig config) {
        this.globalRoleId = globalRoleId;
        this.config = config;
    }
    
    @Override
    public String getTierName() {
        return this.config.name;
    }

    @Override
    public String getGlobalRoleId() {
        return this.globalRoleId;
    }

    @Override
    public String getDiscordRoleId() {
        return this.config.discordRoleId;
    }

    @Override
    public String getPatreonTierId() {
        return this.config.patreonTierId;
    }
}
