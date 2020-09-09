package suzutsuki.discord.events;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import suzutsuki.discord.SuzutsukiDiscord;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class Ready extends ListenerAdapter {
    private final SuzutsukiDiscord suzutsukiDiscord;
    private final String[] status = {
            "Suzutsuki, heading out!",
            "Admiral, I'll protect you forever ❤",
            "Did you call, Admiral?"
    };
    private int counter = 0;

    public Ready(SuzutsukiDiscord suzutsukiDiscord) {
        this.suzutsukiDiscord = suzutsukiDiscord;
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        suzutsukiDiscord.scheduler.scheduleAtFixedRate(
                this::updateStatus,
                0,
                120,
                TimeUnit.SECONDS
        );
    }

    private void updateStatus() {
        if (counter > status.length - 1) counter = 0;
        suzutsukiDiscord.client.getPresence().setActivity(Activity.playing(status[counter]));
       counter++;
    }
}
