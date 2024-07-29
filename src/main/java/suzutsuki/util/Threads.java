package suzutsuki.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import suzutsuki.struct.config.SuzutsukiConfig;

public class Threads {
    public final ExecutorService normal;
    public final ExecutorService virtual; 
    public final ScheduledExecutorService scheduled;

    public Threads(SuzutsukiConfig config) {
        this.normal = Executors.newCachedThreadPool();
        this.virtual = Executors.newVirtualThreadPerTaskExecutor();
        this.scheduled = Executors.newScheduledThreadPool(config.threads);
    }
}