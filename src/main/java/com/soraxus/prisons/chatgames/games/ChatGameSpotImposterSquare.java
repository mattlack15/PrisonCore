package com.soraxus.prisons.chatgames.games;

import com.soraxus.prisons.chatgames.ChatGame;
import com.soraxus.prisons.chatgames.ModuleChatGames;
import com.soraxus.prisons.crate.Crate;
import com.soraxus.prisons.crate.CrateManager;
import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.display.chat.ClickUtil;
import com.soraxus.prisons.util.display.chat.HoverUtil;
import com.soraxus.prisons.util.math.MathUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ChatGameSpotImposterSquare extends ChatGame {

    private static final int dimensions = 5;

    private static List<ChatColor> possible = new ArrayList<>();

    static {
        possible.add(ChatColor.RED);
        possible.add(ChatColor.BLUE);
        possible.add(ChatColor.GREEN);
        possible.add(ChatColor.DARK_BLUE);
        possible.add(ChatColor.GOLD);
        possible.add(ChatColor.WHITE);
        possible.add(ChatColor.YELLOW);
        possible.add(ChatColor.LIGHT_PURPLE);
        possible.add(ChatColor.DARK_PURPLE);
        possible.add(ChatColor.DARK_AQUA);
    }

    private String key = "";

    public ChatGameSpotImposterSquare() {
        super("Imposing Square");
        this.setDescription(new ChatBuilder("Click the correct square!"));
    }

    @Override
    protected boolean check0(Player player, String message) {
        return message.equalsIgnoreCase(key);
    }

    public void sendBlock(int dimensions) {
        Random random = new Random(System.currentTimeMillis());
        ChatColor color = possible.get(random.nextInt(possible.size()));
        int tx = random.nextInt(dimensions);
        int ty = random.nextInt(dimensions);
        for (int y = 0; y < dimensions; y++) {
            ChatBuilder builder = new ChatBuilder();
            for (int x = 0; x < dimensions; x++) {
                if (tx == x && ty == y) {
                    builder.addText(color + "&l█", HoverUtil.text("This one!"), ClickUtil.runnable((p) -> {
                        if (this.isRunning()) {
                            ModuleChatGames.getInstance().currentGameCheck(p, key, true);
                        }
                    }, 120000));
                } else {
                    ChatColor c = color;
                    while (c == color) c = possible.get(random.nextInt(possible.size()));

                    builder.addText(c + "&l█");
                }
            }
            if (y == dimensions - 1)
                builder.addText("\n");
            ModuleChatGames.getInstance().broadcast(builder);
        }
        ModuleChatGames.getInstance().broadcast("&cClick the square that is " + color + color.name().toLowerCase().replace("_", " "));
    }

    @Override
    protected void start0() {
        key = UUID.randomUUID().toString();
        sendBlock(dimensions);
    }

    @Override
    public void reward(Player player, int position) {
        Random random = new Random();
        List<Crate> crates = CrateManager.instance.getLoaded();
        if (random.nextBoolean() && crates.size() > 0) {
            int min = 0;
            int max = crates.size();
            int index = Math.min(max - 1, MathUtils.lowWeightedInt(min, max, 2));
            Crate crate = crates.get(index);
            player.getInventory().addItem(crate.getItem());
        } else {
            long bal = Economy.money.getBalance(player.getUniqueId());
            bal *= 0.05D;
            bal += 10000;
            player.getInventory().addItem(Economy.money.createNote(bal));
        }
    }
}
