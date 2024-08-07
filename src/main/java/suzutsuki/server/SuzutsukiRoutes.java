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
import org.slf4j.Logger;
import suzutsuki.database.SuzutsukiStore;
import suzutsuki.struct.config.SuzutsukiConfig;
import suzutsuki.struct.patreon.Patreon;
import suzutsuki.struct.patreon.Patreons;
import suzutsuki.struct.patreon.tiers.PatreonTier;
import suzutsuki.struct.rest.*;
import suzutsuki.util.SuzutsukiPatreonClient;
import suzutsuki.util.SuzutsukiRoleManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SuzutsukiRoutes {
	private final Logger logger;
	private final JDA client;
	private final SuzutsukiConfig config;
	private final SuzutsukiPatreonClient patreon;
	private final SuzutsukiStore store;
	private final SuzutsukiRoleManager roles;

	public SuzutsukiRoutes(Logger logger, JDA client, SuzutsukiConfig config, SuzutsukiPatreonClient patreon, SuzutsukiStore store, SuzutsukiRoleManager roles) {
		this.logger = logger;
		this.client = client;
		this.config = config;
		this.patreon = patreon;
		this.store = store;
		this.roles = roles;
	}

	public void triggerFail(RoutingContext context) {
		Throwable throwable = context.failure();
		HttpServerResponse response = context.response();
		int statusCode = context.statusCode();

		this.logger.warn("Failed REST Request; Code: {} Error: ", statusCode, throwable);

		response.setStatusCode(statusCode).end();
	}

	public void trigger(String endpoint, RoutingContext context) {
		HttpServerRequest request = context.request();
		HttpServerResponse response = context.response();

		if (!endpoint.equals("/avatars") && !this.config.disable.restAuth) {
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

		try {
			switch (endpoint) {
				case "/patreons/check":
				case "/patreons/check/user":
					this.checkUserPatreonStatus(request, response);
					break;
				case "/patreons/check/guild":
					this.checkGuildPatreonStatus(request, response);
					break;
				case "/patreons":
					this.currentPatreons(guild, response);
					break;
				case "/avatars":
					this.getAvatars(response);
				default:
					throw new RuntimeException("No such route saved for: " + endpoint);
			}
		} catch (NumberFormatException exception) {
			response.setStatusMessage("Bad request");
			context.fail(400, exception);
		} catch (Exception exception) {
			response.setStatusMessage("Internal Server Error");
			context.fail(500, exception);
		}
	}

	private void checkGuildPatreonStatus(HttpServerRequest request, HttpServerResponse response) {
		String guildId = request.getParam("id");

		PatreonGuildResponse guildResponse = new PatreonGuildResponse();
		guildResponse.guildId = guildId;

		if (guildId == null || guildId.isEmpty()) {
			response.end(JsonObject.mapFrom(guildResponse).toString());
			return;
		}

		Patreons patreons = this.patreon.getPatreons();

		List<Patreon> entries = new ArrayList<>();
		try {
			entries = this.store.filter(guildId)
				.stream()
				.map(e -> patreons.tiered.stream().filter(u -> u.userId.equals(e.userId)).findFirst().orElse(null))
				.filter(Objects::nonNull)
				.toList();
		} catch (SQLException exception) {
			this.logger.warn(exception.getMessage(), exception);
		}

		if (entries.isEmpty()) {
			response.end(JsonObject.mapFrom(guildResponse).toString());
			return;
		}

		for (Patreon entry : entries) {
			PatreonTier tier = this.patreon.getTier(entry.userId);
			if (tier == null) continue;
			PatreonUserResponse user = new PatreonUserResponse();
			user.userId = entry.userId;
			user.tier = tier.getTierName();
			guildResponse.users.add(user);
		}

		response.end(JsonObject.mapFrom(guildResponse).toString());
	}

	private void checkUserPatreonStatus(HttpServerRequest request, HttpServerResponse response) {
		String userId = request.getParam("id");

		PatreonUserResponse userResponse = new PatreonUserResponse();
		userResponse.userId = userId;

		if (userId == null || userId.isEmpty()) {
			response.end(JsonObject.mapFrom(userResponse).toString());
			return;
		}

		Patreons patreons = this.patreon.getPatreons();

		Optional<Patreon> opt = patreons.tiered
			.stream()
			.filter(p -> userId.equals(p.userId))
			.findFirst();

		Patreon patreon = (this.config.patreonCheckHonorsRole) ?
			opt.orElseGet(() -> this.roles.getRolePatreon(userId)) :
			opt.orElse(null);

		if (patreon == null) {
			userResponse.tier = null;
		} else {
			userResponse.tier = patreon.tierName;
		}

		response.end(JsonObject.mapFrom(userResponse).toString());
	}

	private void currentPatreons(Guild guild, HttpServerResponse response) {
		List<PatreonsTierUserResponse> array = new ArrayList<>();

		List<PatreonTier> tiers = this.patreon.getTiers();
		Patreons patreons = this.patreon.getPatreons();

		for (PatreonTier tier : tiers) {
			PatreonsTierUserResponse patreonsResponse = new PatreonsTierUserResponse();
			patreonsResponse.tierName = tier.getTierName();

			for (Patreon patreon : patreons.tiered) {
				if (!tier.getPatreonTierId().equals(patreon.tierId)) continue;

				Member member = guild.getMemberById(patreon.userId);

				PatreonTierUserResponse patreonReponse = new PatreonTierUserResponse();
				patreonReponse.userId = patreon.userId;
				if (member == null) {
					patreonReponse.username = null;
				} else {
					patreonReponse.username = member.getEffectiveName();
				}

				patreonsResponse.users.add(patreonReponse);
			}

			array.add(patreonsResponse);
		}

		response.end(new JsonArray(array).toString());
	}

	private void getAvatars(HttpServerResponse response) {
		List<AvatarUrlResponse> array = new ArrayList<>();
		List<String> ids = this.config.avatarUserIds;

		for (String id : ids) {
			User user = this.client.getUserById(id);
			if (user == null) continue;
			AvatarUrlResponse avatar = new AvatarUrlResponse();
			avatar.userId = user.getId();
			avatar.avatarUrl = user.getEffectiveAvatarUrl() + "?size=512";
			array.add(avatar);
		}

		response.end(JsonObject.mapFrom(array).toString());
	}
}
