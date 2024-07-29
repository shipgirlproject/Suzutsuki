package suzutsuki.struct.patreon.relationships;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class Relationships {
    @JsonProperty("currently_entitled_tiers")
    public CurrentlyEntitledTiers currentlyEntitledTiers;
    public User user;
}
