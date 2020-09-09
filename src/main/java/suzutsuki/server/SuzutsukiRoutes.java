package suzutsuki.server;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import suzutsuki.discord.SuzutsukiDiscord;
import suzutsuki.struct.PatreonList;
import suzutsuki.util.SuzutsukiConfig;

import java.util.stream.Stream;

public class SuzutsukiRoutes {
    private final SuzutsukiDiscord suzutsukiDiscord;
    private final SuzutsukiConfig suzutsukiConfig;

    public SuzutsukiRoutes(SuzutsukiDiscord suzutsukiDiscord, SuzutsukiConfig suzutsukiConfig) {
        this.suzutsukiDiscord = suzutsukiDiscord;
        this.suzutsukiConfig = suzutsukiConfig;
    }

    public void trigger(String endpoint, RoutingContext context) {
        HttpServerRequest request = context.request();
        HttpServerResponse response = context.response();
        String auth = request.getHeader("authorization");
        if (auth == null || !auth.equals(suzutsukiConfig.pass)) {
            response.setStatusCode(401).setStatusMessage("Unauthorized").end();
            return;
        }
        Guild guild = suzutsukiDiscord.client.getGuildById(suzutsukiConfig.guildID);
        if (guild == null) {
            response.setStatusCode(500).setStatusMessage("Guild not found").end();
            return;
        }
        switch (endpoint) {
            case "checkPatreonStatus":
                checkPatreonStatus(guild, request, response);
                break;
            case "checkDonatorStatus":
                checkDonatorStatus(guild, request, response);
                break;
            case "currentPatreons":
                currentPatreons(guild, response);
        }
    }

    public void checkPatreonStatus(Guild guild, HttpServerRequest request, HttpServerResponse response) {
        String userID = request.getParam("user_id");
        if (userID == null) {
            JsonObject json = new JsonObject()
                    .put("id", "Unknown")
                    .put("status", false);
            response.end(json.toString());
            return;
        }
        Member member = guild.getMemberById(userID);
        if (member == null) {
            JsonObject json = new JsonObject()
                    .put("id", userID)
                    .put("status", false);
            response.end(json.toString());
            return;
        }
        Stream<Role> roles = member.getRoles().stream();
        if (roles.noneMatch(role -> role.getId().equals(suzutsukiConfig.patreonGlobalRoleID))) {
            if (roles.anyMatch(role -> role.getId().equals(suzutsukiConfig.boomersRoleID))) {
                JsonObject json = new JsonObject()
                        .put("id", userID)
                        .put("status", "NitroBoosters");
                response.end(json.toString());
                return;
            }
            JsonObject json = new JsonObject()
                    .put("id", userID)
                    .put("status", false);
            response.end(json.toString());
            return;
        }
        if (roles.anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.heroes))) {
            JsonObject json = new JsonObject()
                    .put("id", userID)
                    .put("status", "Heroes");
            response.end(json.toString());
            return;
        }
        if (roles.anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.specials))) {
            JsonObject json = new JsonObject()
                    .put("id", userID)
                    .put("status", "Specials");
            response.end(json.toString());
            return;
        }
        if (roles.anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.benefactors))) {
            JsonObject json = new JsonObject()
                    .put("id", userID)
                    .put("status", "Benefactors");
            response.end(json.toString());
            return;
        }
        if (roles.anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.contributors))) {
            JsonObject json = new JsonObject()
                    .put("id", userID)
                    .put("status", "Contributors");
            response.end(json.toString());
            return;
        }
        response.setStatusCode(404).setStatusMessage("User has Patreons role but not assigned in any tiers").end();
    }

    public void checkDonatorStatus(Guild guild, HttpServerRequest request, HttpServerResponse response) {
        String userID = request.getParam("user_id");
        if (userID == null) {
            JsonObject json = new JsonObject()
                    .put("id", "Unknown")
                    .put("status", false);
            response.end(json.toString());
            return;
        }
        Member member = guild.getMemberById(userID);
        if (member == null) {
            JsonObject json = new JsonObject()
                    .put("id", userID)
                    .put("status", false);
            response.end(json.toString());
            return;
        }
        Stream<Role> roles = member.getRoles().stream();
        if (roles.noneMatch(role -> role.getId().equals(suzutsukiConfig.donatorRoleID))) {
            JsonObject json = new JsonObject()
                    .put("id", userID)
                    .put("status", false);
            response.end(json.toString());
            return;
        }
        JsonObject json = new JsonObject()
                .put("id", userID)
                .put("status", true);
        response.end(json.toString());
    }

    public void currentPatreons(Guild guild, HttpServerResponse response) {
        Stream<Member> members = guild
                .getMemberCache()
                .stream()
                .filter(member -> member
                        .getRoles()
                        .stream()
                        .anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonGlobalRoleID)) );
        Stream<Member> heroes = members.filter(member -> member
                .getRoles()
                .stream()
                .anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.heroes)) );
        Stream<Member> specials = members.filter(member -> member
                .getRoles()
                .stream()
                .anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.specials)) );
        Stream<Member> benefactors = members.filter(member -> member
                .getRoles()
                .stream()
                .anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.benefactors)) );
        Stream<Member> contributors = members.filter(member -> member
                .getRoles()
                .stream()
                .anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.contributors)) );
        PatreonList list = new PatreonList(heroes, specials, benefactors, contributors);
        JsonObject json = new JsonObject()
                .put("Heroes", list.heroes.toArray())
                .put("Specials", list.specials.toArray())
                .put("Benefactors", list.benefactors.toArray())
                .put("Contributors", list.contributors.toArray());
        response.end(json.toString());
    }
}
