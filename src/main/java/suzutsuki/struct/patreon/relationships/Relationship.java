package suzutsuki.struct.patreon.relationships;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import suzutsuki.struct.patreon.Attributes;

@JsonAutoDetect
public class Relationship {
    public Attributes attributes;
    public String id;
    public Relationships relationships;
    public String type;

    public boolean hasPatreonUserId() {
        return this.relationships != null 
            && this.relationships.user != null
            && this.relationships.user.data != null
            && this.relationships.user.data.id != null;
    }
}
