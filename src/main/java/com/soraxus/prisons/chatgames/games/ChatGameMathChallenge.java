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

public class ChatGameMathChallenge extends ChatGame {
    private String target;

    public ChatGameMathChallenge() {
        super("Math Challenge");
        setDescription(new ChatBuilder("Add two numbers!"));
    }


    protected boolean check0(Player player, String message) {
        return message.equalsIgnoreCase(target);
    }

    protected void start0() {
        int a = MathUtils.random(10, 100);
        int b = MathUtils.random(10, 100);

        target = (a + b) + "";

        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(""));
        ModuleChatGames.getInstance().broadcast("&7Find &c" + a + " + " + b + "\n");
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
