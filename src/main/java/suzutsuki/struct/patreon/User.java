package suzutsuki.struct.patreon;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class User {
    public String id;
    public String type;
    public Attributes attributes;

    public boolean hasDiscordUserId() {
        return this.attributes != null
            && this.attributes.socialConnections != null
            && this.attributes.socialConnections.discord != null
            && this.attributes.socialConnections.discord.userId != null;
    }
}