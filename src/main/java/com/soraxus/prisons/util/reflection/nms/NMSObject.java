package com.soraxus.prisons.util.reflection.nms;

import com.soraxus.prisons.util.reflection.NMSUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NMSObject {
    private Object object;

    public NMSObject invoke(String method, Object... args) {
        try {
            return NMSUtil.wrap((object.getClass().getMethod(method, NMSUtil.getClasses(args)).invoke(object, args)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public NMSObject getField(String field) {
        try {
            return NMSUtil.wrap(object.getClass().getField(field).get(object));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public NMSObject cast(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return new NMSObject(clazz.cast(object));
    }

    public NMSObject cast(String clazz) {
        return cast(NMSUtil.getNMSClass(clazz));
    }
}
