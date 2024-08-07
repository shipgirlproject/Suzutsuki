package suzutsuki.struct.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class AvatarUrlResponse {
    public String userId;
    public String avatarUrl;
}
