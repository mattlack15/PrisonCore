package com.soraxus.prisons.chatgames;

import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.platform.SpigotCommand;
import net.ultragrav.command.provider.impl.StringProvider;

import java.util.List;

public class CmdChatGame extends SpigotCommand {
    public CmdChatGame() {
        this.addAlias("chatgame");
        this.addParameter(StringProvider.getInstance(), "game");
    }

    @Override
    protected void perform() {
        List<ChatGame> games = ModuleChatGames.getInstance().getGames();
        for (ChatGame game : games) {
            if(game.getName().equalsIgnoreCase(this.<String>getArgument(0).replace("_", " "))) {
                ModuleChatGames.getInstance().endGame();
                ModuleChatGames.getInstance().setCurrentGame(game);
                ModuleChatGames.getInstance().startGame();
                return;
            }
        }
        tell("&cCould not find that game");
    }
}
