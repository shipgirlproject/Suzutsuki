package suzutsuki.struct.patreon.relationships;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import suzutsuki.struct.patreon.relationships.shared.Data;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentlyEntitledTiers {
    public List<Data> data;
}
