package suzutsuki.struct.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect
public class PatreonsTierUserResponse {
	public String tierName;
	public final List<PatreonTierUserResponse> users = new ArrayList<>();
}
