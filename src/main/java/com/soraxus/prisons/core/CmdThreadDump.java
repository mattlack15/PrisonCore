package com.soraxus.prisons.core;

import com.soraxus.prisons.SpigotPrisonCore;
import net.ultragrav.command.UltraCommand;
import net.ultragrav.command.provider.impl.StringProvider;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class CmdThreadDump extends UltraCommand {
    public CmdThreadDump() {
        this.addAlias("threaddump");
        this.addParameter("", StringProvider.getInstance());
    }

    @Override
    protected void perform() {
        try {
            String relevant = getArgument(0);
            getPlayer().sendMessage(ChatColor.GREEN + "Dumping threads into console" + (!relevant.equals("") ? " and printing threads relevant to " + relevant : ""));
            File file = new File(SpigotPrisonCore.instance.getDataFolder(), "ThreadDump-" + UUID.randomUUID().toString() + new Date().toString() + ".txt");
            try (FileWriter writer = new FileWriter(file)) {
                Map<?, ?> liveThreads = Thread.getAllStackTraces();
                for (Object o : liveThreads.keySet()) {
                    Thread key = (Thread) o;
                    writer.append("Thread Name: ").append(key.getName()).append("\n");
                    System.out.println("Thread Name: " + key.getName());
                    writer.append("Status: ").append(key.getState().toString()).append("\n");
                    System.out.println("Status: " + key.getState().toString());
                    StackTraceElement[] trace = (StackTraceElement[]) liveThreads.get(key);
                    List<String> toSend = new ArrayList<>();
                    boolean send = false;
                    for (StackTraceElement stackTraceElement : trace) {
                        writer.append("\tat ").append(String.valueOf(stackTraceElement)).append("\n");
                        System.out.println("\tat " + stackTraceElement);
                        if (!relevant.equals("")) {
                            toSend.add(stackTraceElement.toString());
                            if (stackTraceElement.toString().contains(relevant)) {
                                send = true;
                            }
                        }
                    }
                    if (send) {
                        getPlayer().sendMessage("");
                        getPlayer().sendMessage("Thread Name: " + key.getName());
                        getPlayer().sendMessage("Status: " + key.getState().toString());
                        toSend.forEach(s -> getPlayer().sendMessage(s));
                    }
                }
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}