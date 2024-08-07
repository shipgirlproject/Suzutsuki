package suzutsuki.struct.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonAutoDetect
public class Tokens {
	private String bot;
	private String rest;
	private String patreon;

	@JsonGetter("bot")
	public String getBot() {
		return this.bot;
	}

	@JsonGetter("rest")
	public String getRest() {
		return this.rest;
	}

	@JsonGetter("patreon")
	public String getPatreon() {
		return this.patreon;
	}

	@JsonSetter("bot")
	public String setBot(String token) {
		return this.bot = token;
	}

	@JsonSetter("rest")
	public String setRest(String token) {
		return this.rest = token;
	}

	@JsonSetter("patreon")
	public String setPatreon(String token) {
		return this.patreon = token;
	}
}
