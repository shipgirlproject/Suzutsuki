package suzutsuki.util;

import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import suzutsuki.struct.config.SuzutsukiConfig;
import suzutsuki.struct.patreon.Patreon;
import suzutsuki.struct.patreon.Patreons;
import suzutsuki.struct.patreon.tiers.PatreonTier;

public class SuzutsukiRoleManager {
    private final SuzutsukiConfig config;
    private final Logger logger;
    private final SuzutsukiPatreonClient patreon;
    private final JDA client;
    private final Threads threads;

    public SuzutsukiRoleManager(JDA client, Logger logger, Threads threads, SuzutsukiPatreonClient patreon, SuzutsukiConfig config) {
        this.config = config;
        this.logger = logger;
        this.patreon = patreon;
        this.client = client;
        this.threads = threads;

        this.threads.scheduled.scheduleAtFixedRate(this::handlePatreonRoles, 0, 20, TimeUnit.SECONDS);
    }

    private void handlePatreonRoles() {
        Patreons patreon = this.patreon.getPatreons();
        List<PatreonTier> tiers = this.patreon.getTiers();

        Guild guild = this.client.getGuildById(this.config.guildId);

        if (guild == null) {
            this.logger.info("Notice: Guild (" + this.config.guildId + ") not found. Can\'t execute patreon checks");
            return;
        }

        this.threads.normal.execute(() -> HandleThread.error(() -> this.addPatreonsRole(guild, patreon.tiered, tiers), this.logger));
        this.threads.normal.execute(() -> HandleThread.error(() -> this.removePatreonsRole(guild, patreon.tiered, tiers), this.logger));
    }

    private void addPatreonsRole(Guild guild, List<Patreon> patreons, List<PatreonTier> tiers) {
        for (Patreon patreon : patreons) {
            Member member = guild.getMemberById(patreon.userId);

            if (member == null) {
                this.logger.info("Notice: User (" + patreon.userId + ") => Not found can\'t add Patreon Roles");
                continue;
            }

            List<Role> roles = member.getRoles();

            // do not do anything on a member with patreon ignore role
            if (this.config.patreonIgnoreRoleId != null && roles.stream().anyMatch(role -> role.getId().equals(this.config.patreonIgnoreRoleId))) continue;

            // check and add global patreon role
            if (this.config.patreonGlobalRoleId != null && roles.stream().noneMatch(role -> role.getId().equals(this.config.patreonGlobalRoleId))) {
                Role role = guild.getRoles()
                    .stream()
                    .filter(r -> r.getId().equals(this.config.patreonGlobalRoleId))
                    .findFirst()
                    .orElse(null);
                
                if (role != null) {
                    guild.addRoleToMember(member, role).queue();
                    this.logger.info("Notice: Added Global Patreon (" + role.getName() +") Role | User: @" + member.getEffectiveName());
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

                guild.addRoleToMember(member, role).queue();
                this.logger.info("Notice: Added Patreon (" + role.getName() +") Role | User: @" + member.getEffectiveName());
            }
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
            if (this.config.patreonIgnoreRoleId != null && roles.stream().anyMatch(role -> role.getId().equals(this.config.patreonIgnoreRoleId))) continue;

            // check and remove global patreon role
            if (this.config.patreonGlobalRoleId != null && roles.stream().anyMatch(r -> r.getId().equals(this.config.patreonGlobalRoleId))) {
                Role role = guild.getRoles()
                    .stream()
                    .filter(r -> r.getId().equals(this.config.patreonGlobalRoleId))
                    .findFirst()
                    .orElse(null);
                
                if (role != null) {
                    guild.removeRoleFromMember(member, role).queue();
                    this.logger.info("Notice: Removed Global Patreon (" + role.getName() +") Role | User: @" + member.getEffectiveName());
                }
            }

            Role role = member.getRoles()
                .stream()
                .filter(r -> tiers.stream().anyMatch(tier -> tier.getDiscordRoleId().equals(r.getId())))
                .findFirst()
                .orElse(null);
            
            if (role == null) continue;
            
            guild.removeRoleFromMember(member, role).queue();
            this.logger.info("Notice: Removed Patreon (" + role.getName() +") Role | User: @" + member.getEffectiveName());
        }
    }
}
