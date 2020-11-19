package com.soraxus.prisons.util.display.chat;

import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;
import java.util.function.Consumer;

public class Handler {
    private static final String cmdName = "/_";
    private static final long DEFAULT_EXPIRY = 60000;
    private static Handler instance;
    private Map<UUID, HandledElement> data = new HashMap<>();

    /**
     * Do not call externally
     */
    private Handler() {
        EventSubscriptions.instance.subscribe(this);
    }

    public static Handler getHandler() {
        if (instance == null) {
            instance = new Handler();
        }
        return instance;
    }

    /**
     * Register a handler with default timeout
     *
     * @param run Executor
     * @return Handler ID
     */
    public String registerHandler(Consumer<Player> run) {
        return registerHandler(run, DEFAULT_EXPIRY);
    }

    /**
     * Register a handler with a custom timeout
     *
     * @param run    Executor
     * @param expiry Timeout
     * @return Handler ID
     */
    public String registerHandler(Consumer<Player> run, long expiry) {
        UUID genId = UUID.randomUUID();
        data.put(genId, new HandledElement(run, expiry));
        update();
        return cmdName + " " + genId;
    }

    /**
     * Register a handler with a custom timeout and use count
     *
     * @param run    Executor
     * @param expiry Timeout
     * @param uses   Use count
     * @return Handler ID
     */
    public String registerHandler(Consumer<Player> run, long expiry, int uses) {
        UUID genId = UUID.randomUUID();
        data.put(genId, new HandledElement(run, expiry, uses));
        update();
        return cmdName + " " + genId;
    }

    private void update() {
        List<UUID> marked = new ArrayList<>();
        long t = System.currentTimeMillis();
        data.forEach((i, h) -> {
            if (h.expiry < t || h.used >= h.uses) {
                marked.add(i);
            }
        });
        for (UUID id : marked) {
            data.remove(id);
        }
    }

    @EventSubscription
    public void onCmd(PlayerCommandPreprocessEvent e) {
        String cmd = e.getMessage();
        if (!cmd.startsWith(cmdName)) {
            return;
        }
        try {
            e.setCancelled(true);
            UUID id = UUID.fromString(cmd.substring(cmdName.length() + 1));
            if (data.containsKey(id)) {
                HandledElement el = data.get(id);
                el.used ++;
                el.run.accept(e.getPlayer());
                update();
            }
        } catch (Exception ignored) {
        } // Invalid UUID, ignore
    }

    private static class HandledElement {
        private final Consumer<Player> run;
        private final long expiry;
        private final int uses;
        private int used = 0;

        public HandledElement(Consumer<Player> run, long length) {
            this.run = run;
            this.uses = 1;
            this.expiry = System.currentTimeMillis() + length;
        }

        public HandledElement(Consumer<Player> run, long length, int uses) {
            this.uses = uses;
            this.run = run;
            this.expiry = System.currentTimeMillis() + length;
        }
    }
}
