package com.soraxus.prisons.chatgames.games;

import com.soraxus.prisons.chatgames.ChatGame;
import com.soraxus.prisons.chatgames.ModuleChatGames;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ChatGameTrivia extends ChatGame {

    private final List<Map.Entry<String, String>> questions = new ArrayList<>();

    public ChatGameTrivia() {
        super("Trivia");

        //Load questions
        File file = new File(ModuleChatGames.getInstance().getDataFolder(), "trivia.txt");
        if(!file.exists()) {
            return;
        }
        Map<String, String> qs = new HashMap<>();
        try(Scanner scanner = new Scanner(file)) {

            while(scanner.hasNextLine()) {
                String s = scanner.nextLine();
                String[] qP = s.split("\", \"");
                qs.put(qP[0].replace("\"", "").replace("[", "").replace("]", ""),
                        qP[1].replace("\"", "").replace("[", "").replace("]", ""));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        questions.addAll(qs.entrySet());
    }

    private String answer = "";

    @Override
    protected boolean check0(Player player, String message) {
        return message.equalsIgnoreCase(answer);
    }

    private final Random random = new Random(System.currentTimeMillis());

    @Override
    protected void start0() {
        int index = random.nextInt(questions.size());
        Map.Entry<String, String> q = questions.get(index);
        this.answer = q.getValue();
        ModuleChatGames.getInstance().broadcast(q.getKey());
    }

    @Override
    public void reward(Player player, int position) {

    }
}
