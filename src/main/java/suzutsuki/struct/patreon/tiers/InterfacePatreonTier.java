package suzutsuki.struct.patreon.tiers;

public interface InterfacePatreonTier {
	int getPatreonTierOrder();

	int getPatreonTierLimit();

	String getTierName();

	String getGlobalRoleId();

	String getDiscordRoleId();

	String getPatreonTierId();
}