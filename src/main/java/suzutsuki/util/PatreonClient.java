package suzutsuki.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.Logger;
import suzutsuki.database.SuzutsukiStore;
import suzutsuki.struct.config.SuzutsukiConfig;
import suzutsuki.struct.patreon.Patreon;
import suzutsuki.struct.patreon.Patreons;
import suzutsuki.struct.patreon.User;
import suzutsuki.struct.patreon.relationships.Relationship;
import suzutsuki.struct.patreon.tiers.PatreonTier;
import suzutsuki.struct.store.SuzutsukiStoreEntry;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PatreonClient {
	private final Logger logger;
	private final SuzutsukiConfig config;
	private final SuzutsukiStore store;
	private final Threads threads;
	private final String url;
	private final WebClient client;
	private final List<PatreonTier> tiers;
	private volatile Patreons patreons;

	public PatreonClient(Vertx vertx, Logger logger, SuzutsukiStore store, SuzutsukiConfig config, Threads threads) {
		this.logger = logger;
		this.config = config;
		this.store = store;
		this.threads = threads;
		this.url = "https://www.patreon.com/api/oauth2/v2/campaigns/1877973/members?include=currently_entitled_tiers,user&fields%5Buser%5D=social_connections&page%5Bsize%5D=10000";
		this.client = WebClient.create(vertx, new WebClientOptions());
		this.tiers = this.config.patreonTiers
			.stream()
			.map(tierConfig -> new PatreonTier(this.config.patreonGlobalRoleId, tierConfig))
			.sorted(Comparator.comparing(PatreonTier::getPatreonTierOrder))
			.toList();
		this.patreons = new Patreons(this, new ArrayList<>(), new ArrayList<>(), config);

		this.fetch();

		threads.scheduled.scheduleAtFixedRate(this::fetch, 30, 30, TimeUnit.SECONDS);

		logger.info("Patreon related processes is now loaded and scheduled to run!");
	}

	public List<PatreonTier> getTiers() {
		return this.tiers;
	}

	public Patreons getPatreons() {
		return this.patreons;
	}

	public PatreonTier getTier(String userId) {
		Patreons patreons = this.getPatreons();
		Patreon patreon = patreons.tiered.stream()
			.filter(p -> p.userId.equals(userId))
			.findFirst()
			.orElse(null);

		if (patreon == null) return null;

		List<PatreonTier> tiers = this.getTiers();

		PatreonTier tier = tiers.stream()
			.filter(t -> patreon.tierId.equals(t.getPatreonTierId()))
			.findFirst()
			.orElse(null);

		if (tier == null) {
			throw new NoSuchElementException("Tier should exist here, probably a broken patreon config");
		}

		return tier;
	}

	private void unschedule() {
		Set<String> newIds = this.patreons.tiered.stream()
			.map(p -> p.userId)
			.collect(Collectors.toCollection(HashSet::new));

		List<SuzutsukiStoreEntry> entries = new ArrayList<>();
		try {
			entries = this.store.scheduled();
		} catch (SQLException exception) {
			this.logger.error(exception.getMessage(), exception);
		}

		int count = 0;
		for (SuzutsukiStoreEntry entry : entries) {
			if (!newIds.contains(entry.userId)) continue;
			try {
				this.store.unschedule(entry.userId);
				count++;
			} catch (SQLException exception) {
				this.logger.error(exception.getMessage(), exception);
			}
		}

		if (count > 0) {
			this.logger.info("Unscheduled {} entries for deletion", count);
		}
	}

	private void schedule() {
		Set<String> newIds = this.patreons.tiered.stream()
			.map(p -> p.userId)
			.collect(Collectors.toCollection(HashSet::new));

		List<SuzutsukiStoreEntry> oldEntries = new ArrayList<>();
		try {
			oldEntries = this.store.active()
				.stream()
				.filter(e -> !newIds.contains(e.userId))
				.toList();
		} catch (SQLException exception) {
			this.logger.error(exception.getMessage(), exception);
		}

		long now = Instant.now().toEpochMilli() + ((long) this.config.storeCleanDelayTimeMinutes * 60000L);
		for (SuzutsukiStoreEntry entry : oldEntries) {
			try {
				this.store.schedule(entry.userId, now);
			} catch (SQLException exception) {
				this.logger.error(exception.getMessage(), exception);
			}
		}

		if (!oldEntries.isEmpty()) {
			this.logger.info("Scheduled {} entries for deletion in {} minute(s)", oldEntries.size(), this.config.storeCleanDelayTimeMinutes);
		}

		this.unschedule();
		this.purge();
	}

	private void purge() {
		try {
			int cleaned = this.store.purge();
			if (cleaned > 0) {
				this.logger.info("Cleaned {} expired entries from the database", cleaned);
			}
		} catch (SQLException exception) {
			this.logger.error(exception.getMessage(), exception);
		}
	}

	private void fetch() {
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

		this.patreons = new Patreons(this, relationships, users, this.config);

		this.threads.normal.execute(this::schedule);
	}

	private JsonObject get(String url) {
		CompletableFuture<AsyncResult<HttpResponse<Buffer>>> future = new CompletableFuture<>();

		this.client.requestAbs(HttpMethod.GET, url)
			.putHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
			.putHeader("Authorization", "Bearer " + this.config.tokens.getPatreon())
			.send(future::complete);

		AsyncResult<HttpResponse<Buffer>> async = future.join();

		if (async.failed()) {
			Throwable cause = async.cause();
			throw new RuntimeException(cause.getMessage(), cause);
		}

		HttpResponse<Buffer> response = async.result();

		return response.bodyAsJsonObject();
	}
}
 