package suzutsuki.struct.patreon;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import suzutsuki.struct.patreon.social.Connections;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attributes {
    @JsonProperty("social_connections")
    public Connections socialConnections;
}
