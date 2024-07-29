package suzutsuki.util;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import suzutsuki.struct.config.SuzutsukiConfig;
import suzutsuki.struct.patreon.Patreons;
import suzutsuki.struct.patreon.User;
import suzutsuki.struct.patreon.relationships.Relationship;
import suzutsuki.struct.patreon.tiers.PatreonTier;

public class SuzutsukiPatreonClient {
    private final Logger logger;
    private final SuzutsukiConfig config;
    private final String url;
    private final WebClient client;
    private final List<PatreonTier> tiers;
    private volatile Patreons patreons;

    public SuzutsukiPatreonClient(Vertx vertx, Logger logger, SuzutsukiConfig config, Threads threads) {
        this.logger = logger;
        this.config = config;
        this.url = "https://www.patreon.com/api/oauth2/v2/campaigns/1877973/members?include=currently_entitled_tiers,user&fields%5Buser%5D=social_connections&page%5Bsize%5D=10000";
        this.client = WebClient.create(vertx, new WebClientOptions());
        this.tiers = this.config.patreonTiers
            .stream()
            .map(tierConfig -> new PatreonTier(this.config.patreonGlobalRoleId, tierConfig))
            .toList();
        this.patreons = new Patreons(this, new ArrayList<>(), new ArrayList<>());

        threads.scheduled.scheduleAtFixedRate(() -> HandleThread.error(this::fetch, this.logger), 0, 30, TimeUnit.SECONDS);

        this.logger.info("Patreon now scheduled to run!");
    }

    public List<PatreonTier> getTiers() {
        return this.tiers;
    }

    public Patreons getPatreons() {
        return this.patreons;
    }

    private Patreons fetch() {
        JsonObject response = this.get(this.url);

        JsonArray included = response.getJsonArray("included");
        JsonArray data = response.getJsonArray("data");

        if (response.containsKey("links")) {
            String url = response.getJsonObject("links").getString("next");
            while (url != null) {
                response = this.get(url);
                if (response.containsKey("links")) {
                    url = response.getJsonObject("links").getString("next");
                } else {
                    url = null;
                }
                included.addAll(response.getJsonArray("included"));
                data.addAll(response.getJsonArray("data"));
            }
        }

        @SuppressWarnings("unchecked")
        List<User> users = included
            .getList()
            .stream()
            .map(user -> {
                JsonObject json = JsonObject.mapFrom(user);
                return json.mapTo(User.class);
            })
            .toList();

        @SuppressWarnings("unchecked")
        List<Relationship> relationships = data
            .getList()
            .stream()
            .map(relationship -> {
                JsonObject json = JsonObject.mapFrom(relationship);
                return json.mapTo(Relationship.class);
            })
            .toList();

        this.patreons = new Patreons(this, relationships, users);

        return this.patreons;
    }

    private JsonObject get(String url) {
        CompletableFuture<HttpResponse<Buffer>> future  = new CompletableFuture<>();
        this.client.requestAbs(HttpMethod.GET, url)
            .putHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .putHeader("Authorization", "Bearer " + this.config.tokens.getPatreon())
            .send(response -> this.handleResult(future, response));
        HttpResponse<Buffer> response = future.join();
        return response.bodyAsJsonObject();
    }

    private void handleResult(CompletableFuture<HttpResponse<Buffer>> completableFuture, AsyncResult<HttpResponse<Buffer>> asyncResult) {
        if (asyncResult.failed()) {
            completableFuture.completeExceptionally(asyncResult.cause());
            return;
        }
        completableFuture.complete(asyncResult.result());
    }
}
 