package suzutsuki.struct.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;

@JsonAutoDetect
public class PatreonUserResponse {
    public String userId;
    public String tier;
}
