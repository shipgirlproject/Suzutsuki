package suzutsuki.struct.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class PatreonTierResponse {
	public String tierId = null;
	public String tierName = null;
	public int tierOrder = -1;
}
