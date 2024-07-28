package suzutsuki.struct.patreon.relationships;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import suzutsuki.struct.patreon.relationships.shared.Data;

@JsonAutoDetect
public class User {
    public Data data;
    public Links links;
}
