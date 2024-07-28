package suzutsuki.struct.rest;

import java.util.List;

public class PatreonUsers {
    public final List<PatreonUser> heroes;
    public final List<PatreonUser> specials;
    public final List<PatreonUser> benefactors;
    public final List<PatreonUser> contributors;

    public PatreonUsers(List<PatreonUser> heroes, List<PatreonUser> specials, List<PatreonUser> benefactors, List<PatreonUser> contributors) {
        this.heroes = heroes;
        this.specials = specials;
        this.benefactors = benefactors;
        this.contributors = contributors;
    }
}
