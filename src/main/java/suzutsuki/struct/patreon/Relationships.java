package suzutsuki.struct.patreon;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import suzutsuki.struct.patreon.relationships.Relationship;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class Relationships {
	public Attributes attributes;
	public String id;
	public Relationship relationships;
}
