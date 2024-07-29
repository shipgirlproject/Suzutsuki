package suzutsuki.struct.patreon.relationships;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import suzutsuki.struct.patreon.relationships.shared.Data;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    public Data data;
    public Links links;
}
