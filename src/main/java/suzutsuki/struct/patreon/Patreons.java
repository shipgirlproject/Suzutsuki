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

            for (PatreonTier tier : this.patreon.getTiers()) {
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
