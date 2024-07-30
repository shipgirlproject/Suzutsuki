package suzutsuki.server;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import net.dv8tion.jda.api.JDA;

import org.slf4j.Logger;

import suzutsuki.struct.config.SuzutsukiConfig;
import suzutsuki.util.SuzutsukiPatreonClient;

public class SuzutsukiServer {
    private final SuzutsukiConfig config;
    private final HttpServer server;
    private final Router router;
    private final SuzutsukiRoutes routes;
    private final Logger logger;

    public SuzutsukiServer(Vertx vertx, JDA client, Logger logger, SuzutsukiPatreonClient patreon, SuzutsukiConfig config) {
        this.config = config;
        this.logger = logger;
        this.server = vertx.createHttpServer();
        this.router = Router.router(vertx);
        this.routes = new SuzutsukiRoutes(logger, client, config, patreon);
        this.loadRoutes();
        this.startServer();
    }

    private void loadRoutes() {
        this.router.route(HttpMethod.GET, "/patreons/check")
            .produces("application/json")
            .blockingHandler((RoutingContext context) -> this.routes.trigger("/patreons/check", context), false)
            .failureHandler(this.routes::triggerFail)
            .enable();
        this.router.route(HttpMethod.GET, "/patreons")
            .produces("application/json")
            .blockingHandler((RoutingContext context) -> this.routes.trigger("/patreons", context), false)
            .failureHandler(this.routes::triggerFail)
            .enable();
        this.router.route(HttpMethod.GET, "/avatars")
            .produces("application/json")
            .blockingHandler((RoutingContext context) -> this.routes.trigger("/avatars", context), false)
            .failureHandler(this.routes::triggerFail)
            .enable();
        this.router.route("/*")
            .handler(StaticHandler.create().setIndexPage("index.html"))
            .failureHandler(this.routes::triggerFail)
            .enable();
        this.logger.info("API routes configured & loaded");
    }

    private void startServer() {
        server.requestHandler(this.router).listen(this.config.port);
        this.logger.info("API routes set & running => http://localhost:" + this.config.port);
    }
}
