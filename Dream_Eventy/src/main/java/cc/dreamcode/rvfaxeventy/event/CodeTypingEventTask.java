package cc.dreamcode.rvfaxeventy.event;

import cc.dreamcode.rvfaxeventy.config.MessageConfig;
import cc.dreamcode.utilities.builder.MapBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class CodeTypingEventTask implements EventTask {

    private final MessageConfig messageConfig;
    @Getter
    private String answer;

    @Override
    public void onStart() {
        this.answer = this.generateRandomCode(8);

        this.messageConfig.eventCodeStart.send(
                new java.util.ArrayList<org.bukkit.command.CommandSender>(Bukkit.getOnlinePlayers()),
                new MapBuilder<String, Object>()
                        .put("code", this.answer)
                        .build());
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    public boolean checkAnswer(Player player, String message) {
        return message.equals(this.answer);
    }
}
