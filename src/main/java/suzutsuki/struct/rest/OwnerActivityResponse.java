package suzutsuki.struct.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class OwnerActivityResponse {
	public String type;
	public String name;
	public String url;
	public String emoji;
	public Long start;
	public Long end;
	public String details;
	public String state;
	public String largeImage;
	public String smallImage;
}