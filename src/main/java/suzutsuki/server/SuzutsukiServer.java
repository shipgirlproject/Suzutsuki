package suzutsuki.server;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import io.vertx.ext.web.RoutingContext;
import suzutsuki.discord.SuzutsukiDiscord;
import suzutsuki.util.SuzutsukiConfig;

public class SuzutsukiServer {
    private final SuzutsukiConfig suzutsukiConfig;
    private final HttpServer server;
    private final Router routePrefix;
    private final Router apiRoutes;
    private final SuzutsukiRoutes suzutsukiRoutes;

    public SuzutsukiServer(SuzutsukiDiscord suzutsukiDiscord, SuzutsukiConfig suzutsukiConfig) {
        this.suzutsukiConfig = suzutsukiConfig;
        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(suzutsukiConfig.threads));
        this.server = vertx.createHttpServer();
        this.routePrefix = Router.router(vertx);
        this.apiRoutes = Router.router(vertx);
        this.suzutsukiRoutes = new SuzutsukiRoutes(suzutsukiDiscord, suzutsukiConfig);
    }

    public SuzutsukiServer loadRoutes() {
        apiRoutes.route(HttpMethod.GET, "/checkPatreonStatus/")
                .produces("application/json")
                .blockingHandler((RoutingContext context) -> suzutsukiRoutes.trigger("checkPatreonStatus", context), false)
                .enable();
        apiRoutes.route(HttpMethod.GET, "/checkDonatorStatus/")
                .produces("application/json")
                .blockingHandler((RoutingContext context) -> suzutsukiRoutes.trigger("checkDonatorStatus", context), false)
                .enable();
        apiRoutes.route(HttpMethod.GET, "/currentPatreons/")
                .produces("application/json")
                .blockingHandler((RoutingContext context) -> suzutsukiRoutes.trigger("currentPatreons", context), false)
                .enable();
        routePrefix.mountSubRouter(suzutsukiConfig.routePrefix, apiRoutes);
        return this;
    }

    public void startServer() {
        server.requestHandler(routePrefix).listen(suzutsukiConfig.port);
    }
}
