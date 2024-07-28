package suzutsuki.struct.patreon;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import suzutsuki.struct.patreon.relationships.Relationship;

@JsonAutoDetect
public class Relationships {
    public Attributes attributes;
    public String id;
    public Relationship relationships;
}
