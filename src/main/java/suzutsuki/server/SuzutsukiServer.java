package suzutsuki.server;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import suzutsuki.discord.SuzutsukiDiscord;
import suzutsuki.util.SuzutsukiConfig;

public class SuzutsukiServer {
    private final SuzutsukiConfig suzutsukiConfig;
    private final HttpServer server;
    private final Router routePrefix;
    private final Router apiRoutes;
    private final SuzutsukiRoutes suzutsukiRoutes;
    public final Logger suzutsukiLog;

    public SuzutsukiServer(SuzutsukiDiscord suzutsukiDiscord, SuzutsukiConfig suzutsukiConfig, Logger suzutsukiLog) {
        this.suzutsukiConfig = suzutsukiConfig;
        this.suzutsukiLog = suzutsukiLog;
        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(suzutsukiConfig.threads));
        this.server = vertx.createHttpServer();
        this.routePrefix = Router.router(vertx);
        this.apiRoutes = Router.router(vertx);
        this.suzutsukiRoutes = new SuzutsukiRoutes(suzutsukiDiscord, suzutsukiConfig);
    }

    public SuzutsukiServer loadRoutes() {
        apiRoutes.route(HttpMethod.GET, "/patreons/check")
            .produces("application/json")
            .blockingHandler((RoutingContext context) -> suzutsukiRoutes.trigger("/patreons/check", context), false)
            .failureHandler(suzutsukiRoutes::triggerFail)
            .enable();
        apiRoutes.route(HttpMethod.GET, "/patreons")
            .produces("application/json")
            .blockingHandler((RoutingContext context) -> suzutsukiRoutes.trigger("/patreons", context), false)
            .failureHandler(suzutsukiRoutes::triggerFail)
            .enable();
        apiRoutes.route(HttpMethod.GET, "/avatars")
            .produces("application/json")
            .blockingHandler((RoutingContext context) -> suzutsukiRoutes.trigger("/avatars", context), false)
            .failureHandler(suzutsukiRoutes::triggerFail)
            .enable();
        apiRoutes.route("/*")
            .handler(StaticHandler.create().setIndexPage("index.html"))
            .failureHandler(suzutsukiRoutes::triggerFail)
            .enable();
        routePrefix.mountSubRouter(suzutsukiConfig.routePrefix, apiRoutes);
        suzutsukiLog.info("API routes configured & loaded");
        return this;
    }

    public void startServer() {
        server.requestHandler(routePrefix).listen(suzutsukiConfig.port);
        suzutsukiLog.info("API routes set & running @ localhost:" + suzutsukiConfig.port);
    }
}
