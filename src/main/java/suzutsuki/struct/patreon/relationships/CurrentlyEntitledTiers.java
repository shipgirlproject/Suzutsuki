package suzutsuki.struct.patreon.relationships;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import suzutsuki.struct.patreon.relationships.shared.Data;

import java.util.List;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentlyEntitledTiers {
	public List<Data> data;
}
