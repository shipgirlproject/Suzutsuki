package suzutsuki.struct.patreon;

import suzutsuki.struct.config.MockPatreon;
import suzutsuki.struct.config.SuzutsukiConfig;
import suzutsuki.struct.patreon.relationships.Relationship;
import suzutsuki.struct.patreon.relationships.shared.Data;
import suzutsuki.struct.patreon.tiers.PatreonTier;
import suzutsuki.util.SuzutsukiPatreonClient;

import java.util.ArrayList;
import java.util.List;

public class Patreons {
	public final List<Relationship> relationships;
	public final List<User> users;
	public final List<Patreon> tiered;

	public Patreons(SuzutsukiPatreonClient patreon, List<Relationship> relationships, List<User> users, SuzutsukiConfig config) {
		this.relationships = relationships;
		this.users = users
			.stream()
			.filter(User::hasDiscordUserId)
			.toList();
		this.tiered = new ArrayList<>();

		if (!config.mockPatreons.isEmpty()) {
			for (MockPatreon user : config.mockPatreons) {
				PatreonTier tier = patreon.getTiers().stream()
					.filter(t -> t.getPatreonTierId().equals(user.patreonTierId))
					.findFirst()
					.orElse(null);
				if (tier == null)
					throw new RuntimeException("Invalid tier id provided for mock patreon. Please ensure the tier id exists in patreon tier");

				this.tiered.add(new Patreon(user.userId, tier.getTierName(), tier.getPatreonTierId()));
			}
		}

		for (User user : this.users) {
			Relationship relationship = this.relationships
				.stream()
				.filter(Relationship::hasPatreonUserId)
				.filter(r -> user.id.equals(r.relationships.user.data.id))
				.findFirst()
				.orElse(null);

			if (relationship == null) continue;

			List<Data> subscription = relationship.relationships.currentlyEntitledTiers.data;

			if (subscription.isEmpty()) continue;

			String userId = user.attributes.socialConnections.discord.userId;

			for (PatreonTier tier : patreon.getTiers()) {
				if (subscription.stream().noneMatch(data -> tier.getPatreonTierId().equals(data.id))) continue;

				Patreon person = new Patreon(userId, tier.getTierName(), tier.getPatreonTierId());
				// for some reason, patreon has 2 entitled tiers for some people
				// we solve this by having the loop getTier() starting from the highest tier
				// so that we always ensure that we get the highest subscribed tier of the user
				if (this.tiered.stream().anyMatch(p -> p.userId.equals(person.userId))) continue;

				this.tiered.add(person);
			}
		}
	}
}
