package suzutsuki.struct.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class PatreonUserResponse {
	public String userId;
	public String tierName = null;
	public String tierId = null;
	public int tierOrder = -1;
}
