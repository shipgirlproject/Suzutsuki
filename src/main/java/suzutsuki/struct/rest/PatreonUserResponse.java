package suzutsuki.struct.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class PatreonUserResponse {
	public String userId;
	public String tier;
}
