package suzutsuki.struct.patreon;

public class Patreon {
    public final String userId;
    public final String tierName;
    public final String tierId;

    public Patreon(String userId, String tierName, String tierId) {
        this.userId = userId;
        this.tierName = tierName;
        this.tierId = tierId;
    }
}
