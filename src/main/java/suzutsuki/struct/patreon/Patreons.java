package suzutsuki.struct.patreon;

import java.util.ArrayList;
import java.util.List;

import suzutsuki.struct.patreon.relationships.Relationship;
import suzutsuki.struct.patreon.relationships.shared.Data;
import suzutsuki.struct.patreon.tiers.PatreonTier;
import suzutsuki.util.SuzutsukiPatreonClient;

public class Patreons {
    private final SuzutsukiPatreonClient patreon;
    public final List<Relationship> relationships;
    public final List<User> users;
    public final List<Patreon> tiered;

    public Patreons(SuzutsukiPatreonClient patreon, List<Relationship> relationships, List<User> users) {
        this.patreon = patreon;
        this.relationships = relationships;
        this.users = users
            .stream()
            .filter(User::hasDiscordUserId)
            .toList();
        this.tiered = new ArrayList<>();

        for (PatreonTier tier : this.patreon.getTiers()) {
            for (User user : this.users) {
                Relationship relationship = this.relationships
                    .stream()
                    .filter(Relationship::hasPatreonUserId)
                    .filter(r -> user.id.equals(r.relationships.user.data.id))
                    .findFirst()
                    .orElse(null);
                
                if (relationship == null) continue;

                List<Data> subscription = relationship.relationships.currentlyEntitledTiers.data;

                if (subscription.size() == 0) continue;

                String userId = user.attributes.socialConnections.discord.userId;

                if (subscription.stream().noneMatch(data -> tier.getPatreonTierId().equals(data.id))) continue;

                this.tiered.add(new Patreon(userId, tier.getPatreonTierId(), tier.getTierName()));
            }
        }
    }
}
