package suzutsuki.discord.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.slf4j.Logger;

public class Ready extends ListenerAdapter {
    private final JDA client;
    private final ScheduledExecutorService scheduler;
    private final Logger logger;
    private final String[] status = {
            "Suzutsuki, heading out!",
            "Admiral, I'll protect you forever ❤",
            "Did you call, Admiral?"
    };
    private int counter = 0;

    public Ready(JDA client, Logger logger, ScheduledExecutorService scheduler) {
        this.client = client;
        this.logger = logger;
        this.scheduler = scheduler;
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        this.scheduler.scheduleAtFixedRate(this::updateStatus, 0, 120, TimeUnit.SECONDS);
        this.logger.info(client.getSelfUser().getAsTag() + " is now ready!");
    }

    private void updateStatus() {
        if (counter > status.length - 1) counter = 0;
        client.getPresence().setActivity(Activity.playing(status[counter]));
        counter++;
    }
}
