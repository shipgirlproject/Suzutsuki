package suzutsuki.struct.patreon.social;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect
public class Data {
    @JsonProperty("user_id")
    public String userId;
    public String url;
}

