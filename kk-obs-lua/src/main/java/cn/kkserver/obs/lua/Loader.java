package cn.kkserver.obs.lua;

import cn.kkserver.lua.IObjectReflect;
import cn.kkserver.lua.LuaState;
import cn.kkserver.observer.IObserver;

/**
 * Created by zhanghailong on 2016/11/8.
 */

public class Loader {

    public final static String Tag = "kk";

    static {

        LuaState.addObjectReflect(new IObjectReflect() {

            @Override
            public boolean canReflectToJavaObject(Object object) {
                return object != null && object instanceof LuaObserver;
            }

            @Override
            public boolean canReflectToLuaObject(Object object) {
                return object != null && object instanceof IObserver;
            }

            @Override
            public Object reflectToJavaObject(Object object) {
                if(object != null && object instanceof LuaObserver) {
                    return ((LuaObserver) object).observer;
                }
                return object;
            }

            @Override
            public Object reflectToLuaObject(Object object) {
                if(object != null && object instanceof IObserver) {
                    return new LuaObserver((IObserver) object);
                }
                return object;
            }
        });
    }
}
