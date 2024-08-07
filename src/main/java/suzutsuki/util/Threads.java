package suzutsuki.util;

import org.slf4j.Logger;
import suzutsuki.struct.config.SuzutsukiConfig;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Threads {
	public final ExecutorService normal;
	public final ExecutorService virtual;
	public final ScheduledExecutorService scheduled;

	public Threads(SuzutsukiConfig config, Logger logger) {
		UncaughtExceptionHandler handler = (thread, error) -> logger.error(error.getMessage(), error);
		Thread.setDefaultUncaughtExceptionHandler(handler);

		this.normal = Executors.newCachedThreadPool();
		this.virtual = Executors.newVirtualThreadPerTaskExecutor();
		this.scheduled = Executors.newScheduledThreadPool(config.threads);
	}
}