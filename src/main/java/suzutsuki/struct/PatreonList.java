package suzutsuki.struct;

import net.dv8tion.jda.api.entities.Member;

import java.util.stream.Stream;

public class PatreonList {
    public final Stream<PatreonUser> heroes;
    public final Stream<PatreonUser> specials;
    public final Stream<PatreonUser> benefactors;
    public final Stream<PatreonUser> contributors;

    public PatreonList(Stream<Member> heroes, Stream<Member> specials, Stream<Member> benefactors, Stream<Member> contributors) {
        this.heroes = heroes.map(member -> new PatreonUser(member.getId(), member.getUser().getName(), member.getUser().getDiscriminator()));
        this.specials = specials.map(member -> new PatreonUser(member.getId(), member.getUser().getName(), member.getUser().getDiscriminator()));
        this.benefactors = benefactors.map(member -> new PatreonUser(member.getId(), member.getUser().getName(), member.getUser().getDiscriminator()));
        this.contributors = contributors.map(member -> new PatreonUser(member.getId(), member.getUser().getName(), member.getUser().getDiscriminator()));
    }
}
