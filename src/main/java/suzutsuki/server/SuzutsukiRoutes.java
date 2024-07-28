package suzutsuki.server;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import suzutsuki.struct.Patreons;
import suzutsuki.struct.rest.PatreonUser;
import suzutsuki.struct.rest.StaticUserIds;
import suzutsuki.util.SuzutsukiConfig;
import suzutsuki.util.SuzutsukiPatreonClient;

import java.util.List;

import org.slf4j.Logger;

public class SuzutsukiRoutes {
    private final Logger logger;
    private final JDA client;
    private final SuzutsukiConfig config;
    private final SuzutsukiPatreonClient patreonClient;

    public SuzutsukiRoutes(Logger logger, JDA client, SuzutsukiConfig config, SuzutsukiPatreonClient patreonClient) {
        this.logger = logger;
        this.client = client;
        this.config = config;
        this.patreonClient = patreonClient;
    }

    public void triggerFail(RoutingContext context) {
        Throwable throwable = context.failure();
        HttpServerResponse response = context.response();
        int statusCode = context.statusCode();

        if (throwable != null) {
            this.logger.error("Failed REST Request; Error: ", throwable);
        } else {
            this.logger.warn("Failed REST Request; Code: " + statusCode + " Reason: " + response.getStatusMessage());
        }

        response.setStatusCode(statusCode).end();
    }

    public void trigger(String endpoint, RoutingContext context) {
        HttpServerRequest request = context.request();
        HttpServerResponse response = context.response();
        String auth = request.getHeader("authorization");

        if (endpoint != "/avatars" && (auth == null || !auth.equals(this.config.pass))) {
            response.setStatusMessage("Unauthorized");
            context.fail(401);
            return;
        }

        Guild guild = this.client.getGuildById(this.config.guildId);
        if (guild == null) {
            response.setStatusMessage("Internal Server Error");
            context.fail(500);
            return;
        }

        switch (endpoint) {
            case "/patreons/check":
                this.checkPatreonStatus(guild, request, response, context);
                break;
            case "/patreons":
                this.currentPatreons(guild, response);
                break;
            case "/avatars":
                this.getAvatars(response);
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

            Patreons patreons = this.patreonClient.getPatreons();
            
            if (this.isTieredPatreon(userId, patreons.heroes)) {
                json.put("status", "Heroes");
            } else if (this.isTieredPatreon(userId, patreons.specials)) {
                json.put("status", "Specials");
            } else if (this.isTieredPatreon(userId, patreons.benefactors)) {
                json.put("status", "Benefactors");
            } else if (this.isTieredPatreon(userId, patreons.contributors)) {
                json.put("status", "Contributors");
            } else {
                json.putNull("status");
            }
        }

        response.end(json.toString());
    }

    public void currentPatreons(Guild guild, HttpServerResponse response) {
        Patreons patreons = this.patreonClient.getPatreons();

        List<PatreonUser> heroes = this.getPatreonUsers(guild, patreons.heroes);
        List<PatreonUser> specials = this.getPatreonUsers(guild, patreons.specials);
        List<PatreonUser> benefactors = this.getPatreonUsers(guild, patreons.benefactors);
        List<PatreonUser> contributors = this.getPatreonUsers(guild, patreons.contributors);

        JsonObject json = new JsonObject()
            .put("Heroes", new JsonArray(heroes))
            .put("Specials", new JsonArray(specials))
            .put("Benefactors", new JsonArray(benefactors))
            .put("Contributors", new JsonArray(contributors));
        
        response.end(json.toString());
    }

    public void getAvatars(HttpServerResponse response) {
        // refactor in near future
        StaticUserIds ids = this.config.staticIds;
        User[] users = { 
            this.client.getUserById(ids.saya),
            this.client.getUserById(ids.takase),
            this.client.getUserById(ids.rattley),
            this.client.getUserById(ids.alex),
            this.client.getUserById(ids.yanga),
            this.client.getUserById(ids.keita)
        };

        JsonObject json = new JsonObject();
        for (User user : users) {
            json.put(user.getId(), user.getEffectiveAvatarUrl() + "?size=512"); 
        }
        response.end(json.toString());
    }

    private boolean isTieredPatreon(String userId, List<String> patreons) {
        return patreons
            .stream()
            .anyMatch(id -> id == userId);
    }

    private List<PatreonUser> getPatreonUsers(Guild guild, List<String> patreons) {
        return patreons.stream()
            .map(id -> guild.getMemberById(id))
            .filter(member -> member != null)
            .map(member -> new PatreonUser(member.getId(), member.getUser().getName()))
            .toList();
    }
}
