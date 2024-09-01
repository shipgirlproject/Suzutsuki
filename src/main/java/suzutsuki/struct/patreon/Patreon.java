package suzutsuki.struct.patreon;

import io.vertx.core.json.JsonObject;

public class Patreon {
	public final String userId;
	public final String tierName;
	public final String tierId;

	public Patreon(String userId, String tierName, String tierId) {
		this.userId = userId;
		this.tierName = tierName;
		this.tierId = tierId;
	}

	@Override
	public String toString() {
		return JsonObject.mapFrom(this).encodePrettily();
	}
}
