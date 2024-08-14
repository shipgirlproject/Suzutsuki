package suzutsuki.struct.config;

import java.util.InputMismatchException;
import java.util.Optional;

public class Tokens {
	private final String bot = Optional.ofNullable(System.getenv("BOT_TOKEN")).orElseThrow(() -> new InputMismatchException("Missing BOT_TOKEN env"));
	private final String rest = Optional.ofNullable(System.getenv("REST_TOKEN")).orElseThrow(() -> new InputMismatchException("Missing REST_TOKEN env"));
	private final String patreon = Optional.ofNullable(System.getenv("PATREON_TOKEN")).orElseThrow(() -> new InputMismatchException("Missing PATREON_TOKEN env"));

	public String getBot() {
		return this.bot;
	}

	public String getRest() {
		return this.rest;
	}

	public String getPatreon() {
		return this.patreon;
	}
}
