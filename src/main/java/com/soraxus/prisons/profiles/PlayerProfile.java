package com.soraxus.prisons.profiles;

import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.Meta;

import java.util.UUID;

public class PlayerProfile implements GravSerializable {
    @Getter
    @Setter
    private Meta meta = new Meta();

    @Getter
    @Setter
    private String basePath = "";

    @Getter
    private UUID playerId;

    public PlayerProfile(UUID playerId) {
        this.playerId = playerId;
    }

    public PlayerProfile(GravSerializer serializer) {
        this.playerId = serializer.readUUID();
        meta = new Meta(serializer);
    }

    public <T> T get(String key, Object... constructionAgs) {
        return meta.get(getBasePath() + key, constructionAgs);
    }

    public <T> T getOrSet(String key, T defaultValue, Object... constructionArgs) {
        return meta.getOrSet(getBasePath() + key, defaultValue, constructionArgs);
    }

    public <T> void set(String key, T object) {
        meta.set(getBasePath() + key, object);
    }

    public <T> T getIgnoreBasePath(String key, Object... constructionAgs) {
        return meta.get(key, constructionAgs);
    }

    public <T> T getOrSetIgnoreBasePath(String key, T defaultValue, Object... constructionArgs) {
        return meta.getOrSet(key, defaultValue, constructionArgs);
    }

    public <T> void setIgnoreBasePath(String key, T object) {
        meta.set(key, object);
    }


    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeUUID(this.playerId);
        meta.serialize(serializer);
    }
}
