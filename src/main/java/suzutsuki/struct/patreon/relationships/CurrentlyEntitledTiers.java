package suzutsuki.struct.patreon.relationships;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import suzutsuki.struct.patreon.relationships.shared.Data;

@JsonAutoDetect
public class CurrentlyEntitledTiers {
    public List<Data> data;
}
