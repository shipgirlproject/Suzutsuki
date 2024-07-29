package suzutsuki.struct.patreon.social;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class Connections {
    public Data discord;
    public Data facebook;
    public Data google;
    public Data instagram;
    public Data reddit;
    public Data spotify;
    public Data tiktok;
    public Data twitch;
    public Data twitter;
    public Data vimeo;
    public Data youtube;
}
