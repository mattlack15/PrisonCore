package com.soraxus.prisons.chatgames;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.chatgames.games.*;
import com.soraxus.prisons.chatgames.menu.MenuModuleChatGames;
import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.event.chatgames.ChatGameEndEvent;
import com.soraxus.prisons.event.chatgames.ChatGameStartEvent;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.ListUtil;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.locks.CustomLock;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ModuleChatGames extends CoreModule {

    private static final AtomicReference<ModuleChatGames> instance = new AtomicReference<>();
    private final CustomLock gameLock = new CustomLock(true);
    private final List<ChatGame> rotation = new ArrayList<>();
    private final List<ChatGame> games = new ArrayList<>();
    private ChatGame currentGame;
    private int taskId = -1;

    public static ModuleChatGames getInstance() {
        return instance.get();
    }

    public static void setInstance(ModuleChatGames chatGames) {
        instance.set(chatGames);
    }

    @Override
    protected void onEnable() {
        setInstance(this);

        addGame(new ChatGameTypeChallenge());
        addGame(new ChatGameMathChallenge());
        addGame(new ChatGameSpotImposterSquare());
        addGame(new ChatGameUnscrambleChallenge());
//        addGame(new ChatGameTrivia());

        new CmdChatGame().register();

        taskId = Bukkit.getScheduler().runTaskTimer(SpigotPrisonCore.instance, this::update, 20 * 10, 20 * 60 * 2).getTaskId();
    }

    @Override
    protected void onDisable() {
        try {
            Bukkit.getScheduler().cancelTask(taskId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update() {
        gameLock.perform(() -> {
            chooseNewGame();
            startGame();
        });
    }

    public void addGame(ChatGame game) {
        gameLock.perform(() -> games.add(game));
        addGameToRotation(game);
    }

    public void addGameToRotation(ChatGame game) {
        gameLock.perform(() -> rotation.add(game));
    }

    public void removeGameFromRotation(ChatGame game) {
        gameLock.perform(() -> rotation.remove(game));
    }

    public void removeGame(ChatGame game) {
        gameLock.perform(() -> rotation.remove(game));
    }

    public List<ChatGame> getRotation() {
        return gameLock.perform(() -> new ArrayList<>(rotation));
    }

    public List<ChatGame> getGames() {
        return gameLock.perform(() -> new ArrayList<>(games));
    }

    public void endGame() {
        gameLock.perform(() -> {
            try {
                if (currentGame != null) {

                    StringBuilder builder = new StringBuilder();

                    Map<UUID, Long> end = currentGame.end();

                    ChatGameEndEvent event = new ChatGameEndEvent(currentGame, end);
                    Bukkit.getPluginManager().callEvent(event);

                    List<UUID> endV = new ArrayList<>(end.keySet());
                    int back = 0;
                    for (int i = 0, endSize = endV.size(); i < endSize; i++) {
                        UUID playerId = endV.get(i);
                        Player player = Bukkit.getPlayer(playerId);
                        if (player == null) {
                            back++;
                            continue;
                        }
                        try {
                            currentGame.reward(player, i - back);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        builder.append(player.getName()).append(" (&7").append(Math.round(end.get(playerId) / 10D) / 100D).append("s&e)").append(", ");
                    }

                    if (builder.length() != 0) {
                        for (int i = 0; i < 2; i++)
                            builder.deleteCharAt(builder.length() - 1);
                    } else {
                        builder.append("No one");
                    }
                    broadcast("&e" + builder.toString() + "&6 won!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ChatGame chooseNewGame() {
        return gameLock.perform(() -> {
            if (currentGame != null && currentGame.isRunning())
                endGame();
            currentGame = ListUtil.randomElement(rotation);
            return currentGame;
        });
    }

    public void setCurrentGame(ChatGame game) {
        gameLock.perform(() -> currentGame = game);
    }

    public void startGame() {
        if (currentGame != null && !currentGame.isRunning()) {

            ChatGameStartEvent event = new ChatGameStartEvent(currentGame);
            Bukkit.getPluginManager().callEvent(event);

            broadcast("A game of &e" + currentGame.getName() + " &6has started!");
            broadcast("");
            broadcast("Want to know how to play?");
            broadcast(currentGame.getDescription());
            currentGame.start();
        }
    }

    public boolean isRunning() {
        return currentGame != null && currentGame.isRunning();
    }

    public ChatGame getCurrentGame() {
        return currentGame;
    }

    @EventSubscription
    private void onChat(AsyncPlayerChatEvent event) {
        if (currentGameCheck(event.getPlayer(), event.getMessage(), true)) {
            event.setCancelled(true);
        }
    }

    public boolean currentGameCheck(Player player, String message, boolean messagePlayer) {
        return gameLock.perform(() -> {

            if (currentGame != null && currentGame.isRunning()) {
                if (currentGame.check(player, message)) {

                    if (messagePlayer) {
                        getBaseBuilder().addText(ChatColor.GREEN + "That was correct!").send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                    }

                    if (!currentGame.isRunning())
                        endGame();
                    return true;
                }
            }
            return false;
        });
    }


    public ChatBuilder getBaseBuilder() {
        return new ChatBuilder("&9Chat &8&l▶ &6");
    }

    public void broadcast(ChatBuilder builder) {
        getBaseBuilder().addBuilder(builder).sendAll(Bukkit.getOnlinePlayers());
    }

    public void broadcast(String text) {
        broadcast(new ChatBuilder(text));
    }

    @Override
    public String getName() {
        return "Chat Games";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return new MenuElement(new ItemBuilder(Material.NAME_TAG).setName("&f&lChat Games").build()).clickBuilder().openMenu(new MenuModuleChatGames(backButton)).build();
    }
}
