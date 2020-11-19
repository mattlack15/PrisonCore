package com.soraxus.prisons.profiles;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.Meta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrisonProfile extends PlayerProfile {

    @AllArgsConstructor
    @Getter
    public enum CommentSetting {
        ALLOW("Allow new comments"),
        DISALLOW_NEW("Disallow new comments"),
        DISALLOW("Disallow new comments and hide current ones");

        private String desc;
    }

    protected PrisonProfile(UUID playerId) {
        super(playerId);
        this.setBasePath("prison.");
    }

    protected PrisonProfile(UUID playerId, Meta meta) {
        super(playerId);
        this.setBasePath("prison.");
        this.setMeta(meta);
    }

    protected PrisonProfile(GravSerializer serializer) {
        super(serializer);
        this.setBasePath("prison.");
    }

    public synchronized List<ProfileComment> getComments() {
        return new ArrayList<>(getMeta().getOrSet("comments", new ArrayList<>()));
    }

    //Comment functions are synchronized because they are modifying a list that isn't synchronized
    public synchronized void addComment(ProfileComment comment) {
        getMeta().getOrSet("comments", new ArrayList<>()).add(0, comment);
        scheduleUpdate();
    }

    public synchronized void removeComment(ProfileComment comment) {
        getMeta().getOrSet("comments", new ArrayList<>()).remove(comment);
        scheduleUpdate();
    }

    //No need to be synchronized, meta is thread-safe
    public CommentSetting getCommentSetting() {
        return getMeta().getOrSet("commentSetting", CommentSetting.ALLOW);
    }

    public void setCommentSetting(CommentSetting setting) {
        getMeta().set("commentSetting", setting);
        scheduleUpdate();
    }

    public void scheduleUpdate() {
        ProfileManager.instance.queueUpdate(this);
    }
}
