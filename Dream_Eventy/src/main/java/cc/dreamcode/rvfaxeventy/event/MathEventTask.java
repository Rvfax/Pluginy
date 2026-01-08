package cc.dreamcode.rvfaxeventy.event;

import cc.dreamcode.rvfaxeventy.config.MessageConfig;
import cc.dreamcode.utilities.builder.MapBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Random;

@RequiredArgsConstructor
public class MathEventTask implements EventTask {

    private final MessageConfig messageConfig;
    @Getter
    private String answer;

    @Override
    public void onStart() {
        Random random = new Random();
        int a = random.nextInt(100);
        int b = random.nextInt(100);

        boolean addition = random.nextBoolean();
        int result;
        String expression;

        if (addition) {
            result = a + b;
            expression = a + " + " + b;
        } else {
            if (a < b) {
                int temp = a;
                a = b;
                b = temp;
            }
            result = a - b;
            expression = a + " - " + b;
        }

        this.answer = String.valueOf(result);

        this.messageConfig.eventMathStart.send(
                new java.util.ArrayList<org.bukkit.command.CommandSender>(Bukkit.getOnlinePlayers()),
                new MapBuilder<String, Object>()
                        .put("expression", expression)
                        .build());
    }

    @Override
    public boolean checkAnswer(Player player, String message) {
        return message.equals(this.answer);
    }
}
