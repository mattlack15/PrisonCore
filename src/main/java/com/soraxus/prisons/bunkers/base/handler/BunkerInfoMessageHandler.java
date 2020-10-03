package com.soraxus.prisons.bunkers.base.handler;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.generator.GeneratorElement;
import com.soraxus.prisons.util.EventSubscriptions;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class BunkerInfoMessageHandler {

    @Getter
    private final Bunker parent;
    
    public BunkerInfoMessageHandler(Bunker parent) {
        this.parent = parent;
        EventSubscriptions.instance.subscribe(this);
    }

    private final Random random = new Random(System.currentTimeMillis());

    private final Map<String, Long> lastMessages = new ConcurrentHashMap<>();

    private boolean tryMessage(String key, long delayMillis) {

        if(!lastMessages.containsKey(key)) {
            lastMessages.put(key, System.currentTimeMillis());
            return true;
        }

        long l = lastMessages.get(key);
        boolean allow = !(System.currentTimeMillis() - l < delayMillis);
        if(allow)
            lastMessages.put(key, System.currentTimeMillis());
        return allow;
    }

    public void tick() {

        //First messages
        if(random.nextInt(60) == 0 && getParent().isNewlyCreated() && getParent().getContainingPlayers().size() != 0) {
            if(tryMessage("FIRST_MSG", 1234523543654L)) {
                parent.sendExplanationMessages();
            }
        }

        FULL_GENERATORS: if(random.nextInt(200) == 0) {
            //Check for all generators being full
            List<GeneratorElement> generators = parent.getTileMap().byClass(GeneratorElement.class);
            if(generators.isEmpty())
                break FULL_GENERATORS;
            for (GeneratorElement generator : generators) {
                if(!generator.getCurrentStorage().isFull()) {
                    //Generator is not full
                    break FULL_GENERATORS; //Break out of the if statement
                }
            }

            //All generators are full
            if(tryMessage("FG", 180000)) {
                parent.messageMembersInWorld("&eAll of your generators are full, collect your resources by &7right clicking&e them!");
            }
        }

        //Skill messages
        if(random.nextInt(200) == 0 && getParent().isNewlyCreated() && getParent().getContainingPlayers().size() != 0) {
            if(tryMessage("SKILL_MSG", 5000000)) {
                parent.messageMembersInWorld("&eYou can build things for cheaper if you level up your skillz by destroying trees, rocks, etc!");
                parent.messageMembersInWorld("&eOpen your core and navigate to skills for more information");
            }
        }
    }

}
