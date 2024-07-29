package suzutsuki.struct.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect
public class Tokens {
    private final String bot;
    private final String rest;
    private final String patreon;

    public Tokens(@JsonProperty("bot") String bot, @JsonProperty("rest") String rest, @JsonProperty("patreon") String patreon) {
        this.bot = bot;
        this.rest = rest;
        this.patreon = patreon;
    }

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
}
