package suzutsuki.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import suzutsuki.struct.Patreons;
import suzutsuki.struct.patreon.User;
import suzutsuki.struct.patreon.relationships.Relationship;

public class SuzutsukiPatreonClient {
    private final SuzutsukiConfig suzutsukiConfig;
    private final String url;
    private final WebClient client;
    private Patreons patreons;

    public SuzutsukiPatreonClient(Vertx vertx, SuzutsukiConfig suzutsukiConfig, ScheduledExecutorService scheduler) {
        this.suzutsukiConfig = suzutsukiConfig;
        this.url = "https://www.patreon.com/api/oauth2/v2/campaigns/1877973/members?include=currently_entitled_tiers,user&fields%5Buser%5D=social_connections&page%5Bsize%5D=10000";
        this.client = WebClient.create(vertx, new WebClientOptions());
        this.patreons = new Patreons(new ArrayList<>(), new ArrayList<>());

        scheduler.scheduleAtFixedRate(this::fetch, 0, 30, TimeUnit.SECONDS);
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

        this.patreons = new Patreons(users, relationships);

        return this.patreons;
    }

    private JsonObject get(String url) {
        CompletableFuture<HttpResponse<Buffer>> future  = new CompletableFuture<>();
        this.client.get(url)
            .putHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .putHeader("Authorization", "Bearer " + this.suzutsukiConfig.patreon)
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
 