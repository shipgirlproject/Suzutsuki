package suzutsuki.discord.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import suzutsuki.database.SuzutsukiStore;
import suzutsuki.struct.config.SuzutsukiConfig;
import suzutsuki.struct.patreon.Patreon;
import suzutsuki.struct.patreon.tiers.PatreonTier;
import suzutsuki.struct.store.SuzutsukiStoreEntry;
import suzutsuki.util.Memory;
import suzutsuki.util.PatreonClient;
import suzutsuki.util.Threads;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class MessageReceived extends ListenerAdapter {
	private final SuzutsukiConfig config;
	@SuppressWarnings("unused")
	private final Logger logger;
	private final JDA client;
	private final Threads threads;
	private final PatreonClient patreon;
	private final SuzutsukiStore store;

	public MessageReceived(JDA client, Logger logger, Threads threads, SuzutsukiStore store, PatreonClient patreon, SuzutsukiConfig config) {
		this.config = config;
		this.logger = logger;
		this.client = client;
		this.threads = threads;
		this.patreon = patreon;
		this.store = store;
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		this.threads.virtual.execute(() -> this.handleMessage(event));
	}

	public void handleMessage(MessageReceivedEvent event) {
		if (event.isWebhookMessage() || !event.isFromGuild() || event.getAuthor().isBot()) return;
		String content = event.getMessage().getContentRaw();
		if (!content.startsWith(this.config.prefix)) return;
		String[] args = content.split("\\s+");
		String command = args[0].substring(this.config.prefix.length());
		MessageChannel channel = event.getChannel();
        /*
        / To be honest, just a little something
        */
		try {
			this.handleCommands(command, args, channel, event);
		} catch (SQLException exception) {
			this.logger.error(exception.getMessage(), exception);
			channel.sendMessageEmbeds(this.createMessageEmbed("Something unexpected happened on trying to save the data")).queue();
		} catch (Exception exception) {
			this.logger.error(exception.getMessage(), exception);
			channel.sendMessageEmbeds(this.createMessageEmbed("Something unexpected happened on executing the command")).queue();
		}
	}

	private void handleCommands(String command, String[] args, MessageChannel channel, MessageReceivedEvent event) throws Exception {
		User self = this.client.getSelfUser();
		User author = event.getAuthor();

		String[] premium = {"subscribe", "unsubscribe", "guilds"};

		Patreon patreon;
		PatreonTier tier = null;
		if (Arrays.asList(premium).contains(command)) {
			patreon = this.patreon.getPatreons()
				.tiered
				.stream()
				.filter(p -> p.userId.equals(author.getId()))
				.findFirst()
				.orElse(null);
			if (patreon == null) {
				channel.sendMessageEmbeds(this.createMessageEmbed("You are not yet a patreon, subscribe to access")).queue();
				return;
			}

			if (command.equals("subscribe") || command.equals("guilds")) {
				tier = this.patreon.getTiers()
					.stream()
					.filter(t -> t.getPatreonTierId().equals(patreon.tierId))
					.findFirst()
					.orElse(null);

				assert tier != null;

				if (command.equals("subscribe")) {
					int count = this.store.size(author.getId());
					if (count >= tier.getPatreonTierLimit()) {
						channel.sendMessageEmbeds(this.createMessageEmbed("You already reached the max amount of guild subscription(s) for your account. [" + count + "/" + tier.getPatreonTierLimit() + "]")).queue();
						return;
					}
				}
			}
		}

		switch (command) {
			case "ping": {
				channel.sendMessageEmbeds(this.createMessageEmbed("Pong! Took " + this.client.getGatewayPing() + " ms")).queue();
				break;
			}
			case "status": {
				Runtime runtime = Runtime.getRuntime();
				MessageEmbed embed = new EmbedBuilder()
					.setAuthor("Status", null, self.getEffectiveAvatarUrl())
					.setColor(this.config.getColor())
					.setDescription(
						"```ml\n" +
							"==== Caches\n" +
							"Guilds   :: " + this.client.getGuilds().size() + "\n" +
							"Users    :: " + this.client.getUsers().size() + "\n" +
							"==== Memory\n" +
							"JVM Free :: " + Memory.toHumanReadableSIPrefixes(runtime.freeMemory()) + "\n" +
							"JVM Max  :: " + Memory.toHumanReadableSIPrefixes(runtime.maxMemory()) + "\n" +
							"```"
					)
					.build();
				channel.sendMessageEmbeds(embed).queue();
				break;
			}
			case "subscribe":
			case "unsubscribe": {
				Long guildId;
				try {
					guildId = Long.parseLong(args[1]);
				} catch (NumberFormatException error) {
					guildId = null;
				}
				if (guildId == null) {
					channel.sendMessageEmbeds(this.createMessageEmbed("Invalid guild id provided")).queue();
					break;
				}
				String action;
				if (command.equals("subscribe")) {
					if (this.store.some(guildId.toString(), author.getId())) {
						channel.sendMessageEmbeds(this.createMessageEmbed("This guild is already been added")).queue();
						break;
					}
					this.store.add(guildId.toString(), author.getId());
					action = "Added";
				} else {
					if (!this.store.some(guildId.toString(), author.getId())) {
						channel.sendMessageEmbeds(this.createMessageEmbed("This guild is already been removed")).queue();
						break;
					}
					this.store.delete(guildId.toString(), author.getId());
					action = "Removed";
				}
				channel.sendMessageEmbeds(this.createMessageEmbed(action + " " + guildId + " to the list of your premium guild(s)")).queue();
				break;
			}
			case "guilds": {
				assert tier != null;
				List<SuzutsukiStoreEntry> entries = this.store.list(author.getId());
				if (entries.isEmpty()) {
					channel.sendMessageEmbeds(this.createMessageEmbed("You didn't subscribe any guild. You have " + tier.getPatreonTierLimit() + " subscription(s) available")).queue();
					break;
				}
				List<String> strings = entries
					.stream()
					.map(entry -> "+ " + entry.guildId)
					.toList();
				MessageEmbed embed = new EmbedBuilder()
					.setColor(this.config.getColor())
					.setAuthor("List of subscribed guild(s)", null, self.getEffectiveAvatarUrl())
					.setColor(this.config.getColor())
					.setDescription("```diff\n" + String.join("\n", strings) + "```")
					.setFooter("Subsribed Guild(s) [" + entries.size() + "/" + tier.getPatreonTierLimit() + "]", author.getEffectiveAvatarUrl())
					.build();
				channel.sendMessageEmbeds(embed).queue();
			}
		}
	}

	private MessageEmbed createMessageEmbed(String message) {
		return new EmbedBuilder()
			.setColor(this.config.getColor())
			.setAuthor(message, null, this.client.getSelfUser().getAvatarUrl())
			.build();
	}
}
