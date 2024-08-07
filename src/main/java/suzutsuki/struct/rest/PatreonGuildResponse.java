package suzutsuki.struct.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect
public class PatreonGuildResponse {
	public String guildId;
	public final List<PatreonUserResponse> users = new ArrayList<>();
}
