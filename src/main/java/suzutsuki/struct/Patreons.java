package suzutsuki.struct;

import java.util.ArrayList;
import java.util.List;

import suzutsuki.struct.patreon.User;
import suzutsuki.struct.patreon.relationships.Relationship;
import suzutsuki.struct.patreon.relationships.shared.Data;

public class Patreons {
    public final List<User> users;
    public final List<Relationship> relationships;

    public final List<String> heroes;
    public final List<String> specials;
    public final List<String> benefactors;
    public final List<String> contributors;

    public final int total;

    public Patreons(List<User> users, List<Relationship> relationships) {
        this.users = users
            .stream()
            .filter(User::hasDiscordUserId)
            .toList();
        
        this.relationships = relationships;

        this.heroes = new ArrayList<>();
        this.specials = new ArrayList<>();
        this.benefactors = new ArrayList<>();
        this.contributors = new ArrayList<>();

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

            if (subscription.stream().filter(d -> PatreonTierId.heroes.equals(d.id)).findFirst().orElse(null) != null) {
                this.heroes.add(userId);
                continue;
            }
            if (subscription.stream().filter(d -> PatreonTierId.specials.equals(d.id)).findFirst().orElse(null) != null) {
                this.specials.add(userId);
                continue;
            }
            if (subscription.stream().filter(d -> PatreonTierId.benefactors.equals(d.id)).findFirst().orElse(null) != null) {
                this.benefactors.add(userId);
                continue;
            }
            if (subscription.stream().filter(d -> PatreonTierId.contributors.equals(d.id)).findFirst().orElse(null) != null) {
                this.contributors.add(userId);
                continue;
            }
        }

        this.total = this.heroes.size() + this.specials.size() + this.benefactors.size() + this.contributors.size();
    }
}
