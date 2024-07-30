package suzutsuki.server;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import suzutsuki.struct.config.SuzutsukiConfig;
import suzutsuki.struct.patreon.Patreon;
import suzutsuki.struct.patreon.Patreons;
import suzutsuki.struct.patreon.tiers.PatreonTier;
import suzutsuki.util.SuzutsukiPatreonClient;

import java.util.List;

import org.slf4j.Logger;

public class SuzutsukiRoutes {
    private final Logger logger;
    private final JDA client;
    private final SuzutsukiConfig config;
    private final SuzutsukiPatreonClient patreon;

    public SuzutsukiRoutes(Logger logger, JDA client, SuzutsukiConfig config, SuzutsukiPatreonClient patreon) {
        this.logger = logger;
        this.client = client;
        this.config = config;
        this.patreon = patreon;
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

        if (endpoint != "/avatars" && !this.config.disable.restAuth) {
            String auth = request.getHeader("authorization");
            if (auth == null || !auth.equals(this.config.tokens.getRest())) {
                response.setStatusMessage("Unauthorized");
                context.fail(401);
                return;
            }
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

        if (userId == null) {
            json.putNull("status");
            response.end(json.toString());
            return;
        }

        Patreons patreons = this.patreon.getPatreons();

        Patreon result = patreons.tiered
            .stream()
            .filter(patreon -> userId.equals(patreon.userId))
            .findFirst()
            .orElse(null);
        
        if (result == null) {
            json.putNull("status");
        } else {
            json.put("status", result.tierName);
        }

        response.end(json.toString());
    }

    public void currentPatreons(Guild guild, HttpServerResponse response) {
        JsonObject json = new JsonObject();

        List<PatreonTier> tiers = this.patreon.getTiers();
        Patreons patreons = this.patreon.getPatreons();

        for (PatreonTier tier : tiers) {
            JsonArray array = new JsonArray();

            for (Patreon patreon : patreons.tiered) {
                if (!tier.getPatreonTierId().equals(patreon.tierId)) continue;
                
                Member member = guild.getMemberById(patreon.userId);

                JsonObject user = new JsonObject()
                    .put("id", patreon.userId);
                
                if (member == null) {
                    user.putNull("username");
                } else {
                    user.put("username", member.getEffectiveName());
                }

                array.add(user);
            }

            json.put(tier.getTierName(), array);
        }
        
        response.end(json.toString());
    }

    public void getAvatars(HttpServerResponse response) {
        List<String> ids = this.config.avatarUserIds;
        JsonObject json = new JsonObject();
        for (String id : ids) {
            User user = this.client.getUserById(id);
            if (user == null) continue;
            json.put(user.getId(), user.getEffectiveAvatarUrl() + "?size=512"); 
        }
        response.end(json.toString());
    }
}
