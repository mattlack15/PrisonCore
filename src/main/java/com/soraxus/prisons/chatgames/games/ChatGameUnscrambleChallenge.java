package com.soraxus.prisons.chatgames.games;

import com.soraxus.prisons.chatgames.ChatGame;
import com.soraxus.prisons.chatgames.ModuleChatGames;
import com.soraxus.prisons.crate.Crate;
import com.soraxus.prisons.crate.CrateManager;
import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.util.ListUtil;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.math.MathUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChatGameUnscrambleChallenge extends ChatGame {
    private static final List<String> words = new ArrayList<>();

    static {
        words.add("Soraxus");
        words.add("Target");
        words.add("McDonalds");
        words.add("Building");
        words.add("Games");
        words.add("Prison");
        words.add("Bunker");
        words.add("Unscramble");
        words.add("Superficial");
        words.add("Extraordinary");
        words.add("Deoxyribose");
        words.add("Heart");
        words.add("Nuclear");
    }

    private String target;

    public ChatGameUnscrambleChallenge() {
        super("Unscramble Challenge");
        this.setDescription(new ChatBuilder("Unscramble the word!"));
    }


    @Override
    protected boolean check0(Player player, String message) {
        return message.equalsIgnoreCase(target);
    }

    @Override
    protected void start0() {
        target = ListUtil.randomElement(words);

        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(""));
        ModuleChatGames.getInstance().broadcast("&7Unscramble &c" + scramble(target) + "\n");
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

    private String scramble(String word) {
        if (word.isEmpty()) {
            return word;
        }

        char[] chars = word.toCharArray();
        final List<Character> charList = new ArrayList<>();
        for (char ch : chars) {
            charList.add(ch);
        }
        Collections.shuffle(charList);
        return StringUtils.join(charList, "");
    }
}
