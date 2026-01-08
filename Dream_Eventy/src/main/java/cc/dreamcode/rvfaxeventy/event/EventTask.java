package cc.dreamcode.rvfaxeventy.event;

import org.bukkit.entity.Player;

public interface EventTask {
    void onStart();

    boolean checkAnswer(Player player, String message);
}
