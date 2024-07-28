package suzutsuki.struct.patreon.relationships;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect
public class Relationships {
    @JsonProperty("currently_entitled_tiers")
    public CurrentlyEntitledTiers currentlyEntitledTiers;
    public User user;
}
