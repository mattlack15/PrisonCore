package com.soraxus.prisons.util;

import com.google.common.collect.Lists;
import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.enchants.ModuleEnchants;
import com.soraxus.prisons.util.list.LockingList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.RegisteredListener;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class EventSubscriptions implements Listener {

    @Getter
    private class RegisteredSubscription2 {
        private WeakReference<Object> weakObject;
        private List<MethodCaller> callers = new ArrayList<>();
        private List<Class<?>> classes = new ArrayList<>();

        RegisteredSubscription2(Object object) {
            this.weakObject = new WeakReference<>(object);
        }

        private boolean isValid() {
            return weakObject.get() != null;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof RegisteredSubscription2) {
                Object other = ((RegisteredSubscription2) o).weakObject.get();
                Object our = this.weakObject.get();
                return Objects.equals(other, our) && ((RegisteredSubscription2) o).getClasses().containsAll(this.getClasses());
            }
            return false;
        }
    }

    @AllArgsConstructor
    @Getter
    private class MethodCaller {
        private BiConsumer<Object, Object> action;
        private EventPriority priority;
    }

    public static EventSubscriptions instance;

    private final LockingList<RegisteredSubscription2> subscriptions = new LockingList<>();

    private final RegisteredListener registeredListenerLowest = new RegisteredListener(EventSubscriptions.this, (listener, event) -> callMethods(event, EventPriority.LOWEST), EventPriority.LOWEST, SpigotPrisonCore.instance, false);
    private final RegisteredListener registeredListenerLow = new RegisteredListener(EventSubscriptions.this, (listener, event) -> callMethods(event, EventPriority.LOW), EventPriority.LOW, SpigotPrisonCore.instance, false);
    private final RegisteredListener registeredListenerNormal = new RegisteredListener(EventSubscriptions.this, (listener, event) -> callMethods(event, EventPriority.NORMAL), EventPriority.NORMAL, SpigotPrisonCore.instance, false);
    private final RegisteredListener registeredListenerHigh = new RegisteredListener(EventSubscriptions.this, (listener, event) -> callMethods(event, EventPriority.HIGH), EventPriority.HIGH, SpigotPrisonCore.instance, false);
    private final RegisteredListener registeredListenerHighest = new RegisteredListener(EventSubscriptions.this, (listener, event) -> callMethods(event, EventPriority.HIGHEST), EventPriority.HIGHEST, SpigotPrisonCore.instance, false);
    private final RegisteredListener registeredListenerMonitor = new RegisteredListener(EventSubscriptions.this, (listener, event) -> callMethods(event, EventPriority.MONITOR), EventPriority.MONITOR, SpigotPrisonCore.instance, false);


    public EventSubscriptions() {
        instance = this;

        //Register for all events
        //this.registerEventsInPackage(Event.class.getPackage().getDescription(), Event.class.getClassLoader());
    }

    public synchronized void call(Object e) {
        this.callMethods(e, EventPriority.LOWEST);
        this.callMethods(e, EventPriority.LOW);
        this.callMethods(e, EventPriority.NORMAL);
        this.callMethods(e, EventPriority.HIGH);
        this.callMethods(e, EventPriority.HIGHEST);
        this.callMethods(e, EventPriority.MONITOR);
    }

    public void callMethods(Object e, EventPriority priority) {

        if (e instanceof PluginDisableEvent) {
            if (((PluginDisableEvent) e).getPlugin().equals(ModuleEnchants.instance.getParent())) {
                this.onDisable();
            }
        }


        this.subscriptions.getLock().perform(() -> this.subscriptions.removeIf(s -> !s.isValid()));
        this.subscriptions.copy().forEach(s -> {
            Object object = s.getWeakObject().get();
            if (object == null)
                return;
            s.getCallers().forEach(c -> {
                if (c.getPriority().equals(priority))
                    c.getAction().accept(object, e);
            });
        });


    }

    public synchronized void onDisable() {
    }

    public synchronized boolean isSubscribed(Object object) {
        return this.isSubscribed(object, object.getClass());
    }

    public <T> boolean isSubscribed(T object, Class<? extends T> clazz) {
        RegisteredSubscription2 subscription = new RegisteredSubscription2(object);
        subscription.getClasses().add(clazz);
        return this.subscriptions.contains(subscription);
    }

    /**
     * Register normal object
     *
     * @param o Object
     */
    public void subscribe(Object o) {
        this.subscribe(o, o.getClass());
    }

    /**
     * Register abstract object
     *
     * @param o     Object
     * @param clazz Class of the abstract object
     */
    public <T> void subscribe(T o, Class<? extends T> clazz) {
//        DuelObjectClass wrapper = new DuelObjectClass<>(o, clazz);
//        if (!this.abstractObjects.contains(wrapper)) {
//            this.abstractObjects.add(wrapper);
//        }
        RegisteredSubscription2 subscription = new RegisteredSubscription2(o);
        for (RegisteredSubscription2 subs : subscriptions) {
            if (subscription.equals(subs)) {
                if (subs.getClasses().contains(clazz))
                    return;
                subs.getClasses().add(clazz);
                subs.getCallers().addAll(getCallers(clazz));
                return;
            }
        }
        subscription.getClasses().add(clazz);
        subscription.getCallers().addAll(getCallers(clazz));
        subscriptions.add(subscription);
    }

    private List<MethodCaller> getCallers(Class<?> c) {
        List<MethodCaller> callers = new ArrayList<>();
        ArrayList<Method> methodsToCheck = Lists.newArrayList(c.getDeclaredMethods());
        for (Method meths : c.getMethods()) {
            if (!methodsToCheck.contains(meths)) {
                methodsToCheck.add(meths);
            }
        }
        for (Method methods : methodsToCheck) {
            if (methods.isAnnotationPresent(EventSubscription.class)) {
                if (methods.getParameterCount() > 0) {
                    Class<?> inType = methods.getParameterTypes()[0];

                    if (Event.class.isAssignableFrom(inType)) {
                        try {
                            Method method = inType.getMethod("getHandlerList");
                            synchronized (this) {
                                HandlerList list = (HandlerList) method.invoke(null);
                                List<RegisteredListener> listeners = Arrays.asList(list.getRegisteredListeners());
                                if (!listeners.contains(registeredListenerLowest)) {
                                    list.register(registeredListenerLowest);
                                    list.register(registeredListenerLow);
                                    list.register(registeredListenerNormal);
                                    list.register(registeredListenerHigh);
                                    list.register(registeredListenerHighest);
                                    list.register(registeredListenerMonitor);
                                }
                            }
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        EventSubscription annotation = methods.getAnnotation(EventSubscription.class);
                        MethodCaller caller = new MethodCaller((o, e) -> {
                            if (inType.isInstance(e) || inType.isAssignableFrom(e.getClass())) {
                                methods.setAccessible(true);
                                try {
                                    methods.invoke(o, e);
                                } catch (IllegalAccessException | InvocationTargetException e1) {
                                    e1.printStackTrace();
                                }
                                methods.setAccessible(false);
                            }
                        }, annotation.priority());
                        callers.add(caller);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return callers;
    }

    public synchronized void unSubscribe(Object o) {
        this.subscriptions.remove(new RegisteredSubscription2(o));
    }


    private void unInjectPlayerPacketListener(Player p) {
//        Channel channel = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
//        channel.eventLoop().submit(() -> {
//            if (channel.pipeline().get(HANDLER_NAME) != null) {
//                channel.pipeline().remove(HANDLER_NAME);
//            }
//        });
    }
}