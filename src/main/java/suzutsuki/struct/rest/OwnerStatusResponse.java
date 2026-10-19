package suzutsuki.struct.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.List;

@JsonAutoDetect
public class OwnerStatusResponse extends AvatarUrlResponse {
	public String displayName;
	public String bannerUrl;
	public String status;
	public List<OwnerActivityResponse> activities;
}