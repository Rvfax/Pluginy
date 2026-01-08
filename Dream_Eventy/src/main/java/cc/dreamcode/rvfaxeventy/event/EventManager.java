package cc.dreamcode.rvfaxeventy.event;

import cc.dreamcode.rvfaxeventy.config.MessageConfig;

import cc.dreamcode.utilities.builder.MapBuilder;
import eu.okaeri.injector.annotation.Inject;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class EventManager implements Listener {

    private final MessageConfig messageConfig;

    private EventTask currentEvent;

    public void startEvent(EventTask eventTask) {
        this.currentEvent = eventTask;
        this.currentEvent.onStart();
    }

    public void stopEvent() {
        this.currentEvent = null;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (this.currentEvent == null) {
            return;
        }

        if (this.currentEvent.checkAnswer(e.getPlayer(), e.getMessage())) {
            e.setCancelled(true);

            String answer = "";
            if (this.currentEvent instanceof MathEventTask) {
                answer = ((MathEventTask) this.currentEvent).getAnswer();
            } else if (this.currentEvent instanceof CodeTypingEventTask) {
                answer = ((CodeTypingEventTask) this.currentEvent).getAnswer();
            }

            this.messageConfig.eventWinner.send(
                    new java.util.ArrayList<org.bukkit.command.CommandSender>(Bukkit.getOnlinePlayers()),
                    new MapBuilder<String, Object>()
                            .put("player", e.getPlayer().getName())
                            .put("answer", answer)
                            .build());

            // TODO: Give reward

            this.stopEvent();
        }
    }
}
