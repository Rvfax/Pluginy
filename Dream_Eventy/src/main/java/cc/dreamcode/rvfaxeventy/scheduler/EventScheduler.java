package cc.dreamcode.rvfaxeventy.scheduler;

import cc.dreamcode.rvfaxeventy.config.MessageConfig;
import cc.dreamcode.rvfaxeventy.config.PluginConfig;
import cc.dreamcode.rvfaxeventy.event.CodeTypingEventTask;
import cc.dreamcode.rvfaxeventy.event.EventManager;
import cc.dreamcode.rvfaxeventy.event.MathEventTask;
import eu.okaeri.injector.annotation.Inject;
import lombok.RequiredArgsConstructor;

import java.util.Random;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class EventScheduler {

    private final EventManager eventManager;
    private final MessageConfig messageConfig;
    private final PluginConfig pluginConfig;
    private final cc.dreamcode.rvfaxeventy.RvfaxEventyPlugin plugin;

    public void start() {
        this.run();
    }

    private void run() {
        if (new Random().nextBoolean()) {
            this.eventManager.startEvent(new MathEventTask(this.messageConfig));
        } else {
            this.eventManager.startEvent(new CodeTypingEventTask(this.messageConfig));
        }

        // Schedule next run
        long time = this.pluginConfig.eventTime * 20L;
        plugin.getServer().getScheduler().runTaskLater(plugin, this::run, time);
    }
}
