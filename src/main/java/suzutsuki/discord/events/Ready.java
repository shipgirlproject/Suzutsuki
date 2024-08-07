package suzutsuki.discord.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import suzutsuki.util.Threads;

import java.util.concurrent.TimeUnit;

public class Ready extends ListenerAdapter {
	private final JDA client;
	private final Threads threads;
	private final Logger logger;
	private final String[] status = {
		"Suzutsuki, heading out!",
		"Admiral, I'll protect you forever ❤",
		"Did you call, Admiral?"
	};
	private int counter = 0;

	public Ready(JDA client, Logger logger, Threads threads) {
		this.client = client;
		this.logger = logger;
		this.threads = threads;
	}

	@Override
	public void onReady(@NotNull ReadyEvent event) {
		this.threads.scheduled.scheduleAtFixedRate(this::updateStatus, 0, 120, TimeUnit.SECONDS);
		this.logger.info("{} is now ready!", client.getSelfUser().getAsTag());
	}

	private void updateStatus() {
		if (counter > status.length - 1) counter = 0;
		client.getPresence().setActivity(Activity.playing(status[counter]));
		counter++;
	}
}
