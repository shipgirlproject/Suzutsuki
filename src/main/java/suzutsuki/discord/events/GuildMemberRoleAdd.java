package suzutsuki.discord.events;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import suzutsuki.util.SuzutsukiConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GuildMemberRoleAdd extends ListenerAdapter {
    private final SuzutsukiConfig suzutsukiConfig;

    public GuildMemberRoleAdd(SuzutsukiConfig suzutsukiConfig) {
        this.suzutsukiConfig = suzutsukiConfig;
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        Guild guild = event.getGuild();
        if (!guild.getId().equals(suzutsukiConfig.guildID)) return;
        TextChannel channel = guild.getTextChannelById(suzutsukiConfig.annoucementChannelID);
        if (channel == null) return;
        CompletableFuture
                .delayedExecutor(10, TimeUnit.SECONDS)
                .execute(() -> {
                    List<Role> roles = event.getRoles();
                    if (roles.stream().noneMatch(role -> role.getId().equals(suzutsukiConfig.patreonGlobalRoleID))) return;
                    Role role = roles.stream()
                            .filter(r ->
                                    r.getId().equals(suzutsukiConfig.patreonTiers.heroes) ||
                                            r.getId().equals(suzutsukiConfig.patreonTiers.specials) ||
                                            r.getId().equals(suzutsukiConfig.patreonTiers.benefactors) ||
                                            r.getId().equals(suzutsukiConfig.patreonTiers.contributors))
                            .findFirst()
                            .orElse(null);
                    if (role == null) return;
                    channel.sendMessage("<a:too_hype:480054627820371977> " + event.getUser().getAsMention() + " became a Patreon! Thanks for the support \\❤").queue();
                });

    }
}
