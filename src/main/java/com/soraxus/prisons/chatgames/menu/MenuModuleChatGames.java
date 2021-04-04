package com.soraxus.prisons.chatgames.menu;

import com.soraxus.prisons.chatgames.ModuleChatGames;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;

import java.util.UUID;

public class MenuModuleChatGames extends Menu {

    private final MenuElement backElement;
    public MenuModuleChatGames(MenuElement backElement) {
        this.backElement = backElement;
        this.setSize(3);
        this.setTitle("Chat Games");
    }

    @Override
    public void build(UUID player) {
        this.setElement(4, backElement);
        this.setElement(13, new MenuElement(new ItemBuilder(Material.EGG).setName("&cStart/Stop Game").addLore("Click to start/stop the current chat game")
                .build()).clickBuilder().onLeftClick((e) -> {
            if(ModuleChatGames.getInstance().isRunning()) {
                ModuleChatGames.getInstance().endGame();
            } else {
                ModuleChatGames.getInstance().chooseNewGame();
                ModuleChatGames.getInstance().startGame();
            }
        }).build());
    }
}
