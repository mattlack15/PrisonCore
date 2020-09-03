package com.soraxus.prisons.bunkers.npc;

import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;

public class CitizensControllerBehaviorAdapter extends BehaviorGoalAdapter {
    private AbstractBunkerNPCController controller;

    public CitizensControllerBehaviorAdapter(AbstractBunkerNPCController controller) {
        this.controller = controller;
    }

    @Override
    public void reset() {

    }

    @Override
    public BehaviorStatus run() {
        return null;
    }

    @Override
    public boolean shouldExecute() {
        return false;
    }
}
