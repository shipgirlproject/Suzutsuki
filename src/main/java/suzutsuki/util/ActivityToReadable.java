package suzutsuki.util;

import net.dv8tion.jda.api.entities.Activity;

public class ActivityToReadable {
	public static String convertActivityToReadableName(Activity activity) {
		switch (activity.getType()) {
			case PLAYING -> {
				return "Playing";
			}
			case STREAMING -> {
				return "Streaming";
			}
			case LISTENING -> {
				return "Listening";
			}
			case WATCHING -> {
				return "Watching";
			}
			case COMPETING -> {
				return "Competing";
			}
			case CUSTOM_STATUS ->  {
				return "Custom Status";
			}
		}

		throw new RuntimeException("Should not reach here");
	}
}
