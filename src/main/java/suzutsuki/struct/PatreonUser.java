package suzutsuki.struct;

public class PatreonUser {
    public final String id;
    public final String username;
    public final String discriminator;

    public PatreonUser(String id, String username, String discriminator) {
        this.id = id;
        this.username = username;
        this.discriminator = discriminator;
    }
}
