package suzutsuki.server;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import suzutsuki.discord.SuzutsukiDiscord;
import suzutsuki.struct.PatreonList;
import suzutsuki.struct.StaticUserIds;
import suzutsuki.util.SuzutsukiConfig;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SuzutsukiRoutes {
    private final SuzutsukiDiscord suzutsukiDiscord;
    private final SuzutsukiConfig suzutsukiConfig;

    public SuzutsukiRoutes(SuzutsukiDiscord suzutsukiDiscord, SuzutsukiConfig suzutsukiConfig) {
        this.suzutsukiDiscord = suzutsukiDiscord;
        this.suzutsukiConfig = suzutsukiConfig;
    }

    public void triggerFail(RoutingContext context) {
        Throwable throwable = context.failure();
        HttpServerResponse response = context.response();
        int statusCode = context.statusCode();
        if (throwable != null) {
            suzutsukiDiscord.suzutsukiLog.error("Failed REST Request; Error: ", throwable);
        } else {
            suzutsukiDiscord.suzutsukiLog.warn("Failed REST Request; Code: " + statusCode + " Reason: " + response.getStatusMessage());
        }
        response.setStatusCode(statusCode).end();
    }

    public void trigger(String endpoint, RoutingContext context) {
        HttpServerRequest request = context.request();
        HttpServerResponse response = context.response();
        String auth = request.getHeader("authorization");
        if (endpoint != "/avatars" && (auth == null || !auth.equals(suzutsukiConfig.pass))) {
            response.setStatusMessage("Unauthorized");
            context.fail(401);
            return;
        }
        Guild guild = suzutsukiDiscord.client.getGuildById(suzutsukiConfig.guildID);
        if (guild == null) {
            response.setStatusMessage("Internal Server Error");
            context.fail(500);
            return;
        }
        switch (endpoint) {
            case "/patreons/check":
                checkPatreonStatus(guild, request, response, context);
                break;
            case "/patreons":
                currentPatreons(guild, response);
                break;
            case "/avatars":
                getAvatars(response);
        }
    }

    public void checkPatreonStatus(Guild guild, HttpServerRequest request, HttpServerResponse response, RoutingContext context) {
        String userId = request.getParam("id");
        JsonObject json = new JsonObject().put("id", userId);
        runnable: {
            if (userId == null) {
                json.putNull("status");
                break runnable;
            }
            Member member = guild.getMemberById(userId);
            if (member == null) {
                json.putNull("status");
                break runnable;
            }
            List<Role> roles = member.getRoles();
            if (roles.stream().anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.heroes)))
                json.put("status", "Heroes");
            else if (roles.stream().anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.specials)))
                json.put("status", "Specials");
            else if (roles.stream().anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.benefactors)) ||
                    roles.stream().anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.boosters)))
                json.put("status", "Benefactors");
            else if (roles.stream().anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.contributors)))
                json.put("status", "Contributors");
            else
                json.putNull("status");
        }
        response.end(json.toString());
    }

    public void currentPatreons(Guild guild, HttpServerResponse response) {
        List<Member> members = guild
                .getMemberCache()
                .stream()
                .filter(member -> member
                        .getRoles()
                        .stream()
                        .anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonGlobalRoleID)) )
                .collect(Collectors.toList());
        Stream<Member> heroes = members.stream().filter(member -> member
                .getRoles()
                .stream()
                .anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.heroes)) );
        Stream<Member> specials =members.stream().filter(member -> member
                .getRoles()
                .stream()
                .anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.specials)) );
        Stream<Member> benefactors = members.stream().filter(member -> member
                .getRoles()
                .stream()
                .anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.benefactors)) );
        Stream<Member> contributors = members.stream().filter(member -> member
                .getRoles()
                .stream()
                .anyMatch(role -> role.getId().equals(suzutsukiConfig.patreonTiers.contributors)) );
        PatreonList list = new PatreonList(heroes, specials, benefactors, contributors);
        JsonObject json = new JsonObject()
                .put("Heroes", new JsonArray(list.heroes.collect(Collectors.toList())) )
                .put("Specials", new JsonArray(list.specials.collect(Collectors.toList())) )
                .put("Benefactors", new JsonArray(list.benefactors.collect(Collectors.toList())) )
                .put("Contributors", new JsonArray(list.contributors.collect(Collectors.toList())) );
        response.end(json.toString());
    }

    public void getAvatars(HttpServerResponse response) {
        StaticUserIds ids = this.suzutsukiConfig.statisIds;
        User[] users = { 
            this.suzutsukiDiscord.client.getUserById(ids.saya),
            this.suzutsukiDiscord.client.getUserById(ids.takase),
            this.suzutsukiDiscord.client.getUserById(ids.rattley),
            this.suzutsukiDiscord.client.getUserById(ids.alex),
            this.suzutsukiDiscord.client.getUserById(ids.yanga),
            this.suzutsukiDiscord.client.getUserById(ids.keita)
        };

        JsonObject json = new JsonObject();
        for (User user : users) {
            json.put(user.getId(), user.getEffectiveAvatarUrl() + "?size=512"); 
        }
        response.end(json.toString());
    }
}
