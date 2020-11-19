package com.soraxus.prisons.chatgames;

import com.soraxus.prisons.util.display.chat.ChatBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ChatGame {
    @Getter
    private String name;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private ChatBuilder description = new ChatBuilder("");
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private int maxWinners = 1;

    private long startTime;
    @Getter
    private Map<UUID, Long> times = new HashMap<>();

    @Getter
    private volatile boolean running = false;

    public ChatGame(String name) {
        this.name = name;
    }

    public synchronized void start() {
        startTime = System.currentTimeMillis();
        times.clear();
        try {
            start0();
            running = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized Map<UUID, Long> end() {
        if (!running)
            return new HashMap<>(times);
        Map<UUID, Long> ret = times;
        running = false;
        startTime = -1;
        try {
            onEnd(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


    public boolean check(Player player, String message) {
        if (!check0(player, message)) {
            return false;
        }
        times.put(player.getUniqueId(), System.currentTimeMillis() - startTime);
        if (times.size() >= maxWinners) {
            end();
        }
        return true;
    }

    /**
     * Log a message
     *
     * @param player  The player
     * @param message The message
     * @return Whether or not the game should end
     */
    protected abstract boolean check0(Player player, String message);

    protected abstract void start0();

    void onEnd(Map<UUID, Long> times) {
    }

    public abstract void reward(Player player, int position);
}
