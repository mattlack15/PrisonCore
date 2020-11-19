package com.soraxus.prisons.chatgames.games;

import com.soraxus.prisons.chatgames.ChatGame;
import com.soraxus.prisons.chatgames.ModuleChatGames;
import com.soraxus.prisons.crate.Crate;
import com.soraxus.prisons.crate.CrateManager;
import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.math.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class ChatGameTypeChallenge extends ChatGame {

    private String target;

    public ChatGameTypeChallenge() {
        super("Type Challenge");
        this.setDescription(new ChatBuilder("Type the message in chat!"));
    }


    @Override
    protected boolean check0(Player player, String message) {
        return message.equalsIgnoreCase(target);
    }

    @Override
    protected void start0() {
        target = getIdentifier(10);

        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(""));
        ModuleChatGames.getInstance().broadcast("&7Type &c" + target + "\n");
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

    private String getIdentifier(int length) {
        Random random = new Random(System.currentTimeMillis());
        long val = random.nextLong();
        StringBuilder m = new StringBuilder(Long.toHexString(val));
        if (m.length() > length) {
            m = new StringBuilder(m.substring(m.length() - length));
        }
        while (m.length() < length) {
            int i = random.nextInt();
            String o = Integer.toHexString(i);
            m.insert(0, o.charAt(o.length() - 1));
        }
        return m.toString().toLowerCase();
    }
}
