package suzutsuki.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import suzutsuki.struct.config.SuzutsukiConfig;
import suzutsuki.struct.patreon.Patreon;
import suzutsuki.struct.patreon.Patreons;
import suzutsuki.struct.patreon.tiers.PatreonTier;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class RoleManager {
	private final SuzutsukiConfig config;
	private final Logger logger;
	private final PatreonClient patreon;
	private final JDA client;
	private final Threads threads;
	private boolean log;

	public RoleManager(JDA client, Logger logger, Threads threads, PatreonClient patreon, SuzutsukiConfig config) {
		this.config = config;
		this.logger = logger;
		this.patreon = patreon;
		this.client = client;
		this.threads = threads;
		this.log = true;

		this.threads.scheduled.scheduleAtFixedRate(this::check, 0, 20, TimeUnit.SECONDS);
	}

	public Patreon getRolePatreon(String id) {
		Guild guild = this.client.getGuildById(this.config.guildId);

		if (guild == null) return null;

		Member member = guild.getMemberById(id);

		if (member == null) return null;

		List<PatreonTier> tiers = this.patreon.getTiers();
		List<Role> roles = member.getRoles();

		PatreonTier tier = tiers.stream()
			.filter(t -> roles.stream().anyMatch(role -> role.getId().equals(t.getDiscordRoleId())))
			.findFirst()
			.orElse(null);

		if (tier == null) return null;

		return new Patreon(id, tier.getTierName(), tier.getPatreonTierId());
	}

	private void check() {
		Patreons patreon = this.patreon.getPatreons();
		List<PatreonTier> tiers = this.patreon.getTiers();

		Guild guild = this.client.getGuildById(this.config.guildId);

		if (guild == null) {
			this.logger.debug("Guild ({}) not found. Can't execute patreon checks", this.config.guildId);
			return;
		}

		this.threads.normal.execute(() -> this.addPatreonsRole(guild, patreon.tiered, tiers));
		this.threads.normal.execute(() -> this.removePatreonsRole(guild, patreon.tiered, tiers));
	}

	private void addPatreonsRole(Guild guild, List<Patreon> patreons, List<PatreonTier> tiers) {
		for (Patreon patreon : patreons) {
			Member member = guild.getMemberById(patreon.userId);

			if (member == null) {
				this.logger.debug("User ({}) => Not found as member can't add Patreon Roles", patreon.userId);
				continue;
			}

			List<Role> roles = member.getRoles();

			// do not do anything on a member with patreon ignore role
			if (this.config.patreonIgnoreRoleId != null && roles.stream().anyMatch(role -> role.getId().equals(this.config.patreonIgnoreRoleId))) {
				this.logger.debug("User @{}({}) has patreon ignore role. Not adding anything", member.getEffectiveName(), member.getUser().getId());
				continue;
			}

			// check and add global patreon role
			boolean didAddGlobal = false;
			if (this.config.patreonGlobalRoleId != null && roles.stream().noneMatch(role -> role.getId().equals(this.config.patreonGlobalRoleId))) {
				Role role = guild.getRoles()
					.stream()
					.filter(r -> r.getId().equals(this.config.patreonGlobalRoleId))
					.findFirst()
					.orElse(null);

				if (role != null) {
					this.addRole(guild, member, role);
					didAddGlobal = true;
				}
			}

			PatreonTier tier = tiers.stream()
				.filter(t -> t.getPatreonTierId().equals(patreon.tierId))
				.findFirst()
				.orElse(null);

			if (tier == null) continue;

			// check and add specific patreon role
			if (roles.stream().noneMatch(role -> role.getId().equals(tier.getDiscordRoleId()))) {
				Role role = guild.getRoles()
					.stream()
					.filter(r -> r.getId().equals(tier.getDiscordRoleId()))
					.findFirst()
					.orElse(null);

				if (role == null) continue;

				this.addRole(guild, member, role);

				if (!this.log) continue;
				this.logger.info("New patreon! Added Role: ({}) | Global Role Added: {} | User: @{}({})", role.getName(), didAddGlobal, member.getEffectiveName(), member.getUser().getId());
			}

			if (!this.config.disable.roleAdd) return;
			this.log = false;
		}
	}

	private void removePatreonsRole(Guild guild, List<Patreon> patreons, List<PatreonTier> tiers) {
		List<Member> members = guild.getMembers()
			.stream()
			.filter(member -> member.getRoles().stream().anyMatch(role -> tiers.stream().anyMatch(tier -> role.getId().equals(tier.getDiscordRoleId()))))
			.toList();

		for (Member member : members) {
			if (patreons.stream().anyMatch(patreon -> patreon.userId.equals(member.getUser().getId()))) continue;

			List<Role> roles = member.getRoles();

			// do not do anything on a member with patreon ignore role
			if (this.config.patreonIgnoreRoleId != null && roles.stream().anyMatch(role -> role.getId().equals(this.config.patreonIgnoreRoleId))) {
				this.logger.debug("User @{}({}) has patreon ignore role. Not removing anything", member.getEffectiveName(), member.getUser().getId());
				continue;
			}

			// check and remove global patreon role
			boolean didRemoveGlobal = false;
			if (this.config.patreonGlobalRoleId != null && roles.stream().anyMatch(r -> r.getId().equals(this.config.patreonGlobalRoleId))) {
				Role role = guild.getRoles()
					.stream()
					.filter(r -> r.getId().equals(this.config.patreonGlobalRoleId))
					.findFirst()
					.orElse(null);

				if (role != null) {
					this.removeRole(guild, member, role);
					didRemoveGlobal = true;
				}
			}

			Role role = roles
				.stream()
				.filter(r -> tiers.stream().anyMatch(tier -> tier.getDiscordRoleId().equals(r.getId())))
				.findFirst()
				.orElse(null);

			if (role == null) continue;

			this.removeRole(guild, member, role);

			if (!this.log) continue;
			this.logger.info("Removed patreon! Removed Role: ({}) | Global Role Removed: {} | User: @{}({})", role.getName(), didRemoveGlobal, member.getEffectiveName(), member.getUser().getId());
		}

		if (!this.config.disable.roleAdd) return;
		this.log = false;
	}

	private void addRole(Guild guild, Member member, Role role) {
		if (this.config.disable.roleAdd) return;
		guild.addRoleToMember(member, role).queue();
	}

	private void removeRole(Guild guild, Member member, Role role) {
		if (this.config.disable.roleAdd) return;
		guild.removeRoleFromMember(member, role).queue();
	}
}
