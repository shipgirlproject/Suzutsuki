package suzutsuki.struct.patreon;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import suzutsuki.struct.patreon.social.Connections;

@JsonAutoDetect
public class Attributes {
    @JsonProperty("social_connections")
    public Connections socialConnections;
}
