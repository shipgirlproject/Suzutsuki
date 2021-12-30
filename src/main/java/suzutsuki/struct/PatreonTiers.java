package suzutsuki.struct;

import org.json.JSONObject;

public class PatreonTiers {
    public final String heroes;
    public final String specials;
    public final String benefactors;
    public final String boosters;
    public final String contributors;

    public PatreonTiers(JSONObject data) {
        this.heroes = data.getString("heroes");
        this.specials = data.getString("specials");
        this.benefactors = data.getString("benefactors");
        this.boosters = data.getString("boosters");
        this.contributors = data.getString("contributors");
    }
}
