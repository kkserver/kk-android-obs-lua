package cn.kkserver.obs.lua;

import android.util.Log;

import cn.kkserver.core.IGetter;
import cn.kkserver.lua.IObjectReflect;
import cn.kkserver.lua.LuaFunction;
import cn.kkserver.lua.LuaState;
import cn.kkserver.observer.IObserver;
import cn.kkserver.observer.IWithObserver;
import cn.kkserver.observer.Listener;
import cn.kkserver.observer.Observer;
import cn.kkserver.observer.WithObserver;

/**
 * Created by zhanghailong on 2016/11/8.
 */

public class LuaObserver implements IGetter {

    public final IObserver observer;

    private static final LuaFunction _funcGet = new LuaFunction() {
        @Override
        public int invoke(LuaState luaState) {

            int top = luaState.gettop();

            if(top > 1 && luaState.type( - top ) == LuaState.LUA_TOBJECT && luaState.type( - top + 1) == LuaState.LUA_TSTRING) {

                Object object = luaState.toobject(- top);

                String name = luaState.tostring(- top + 1);

                Object v = null;

                if(object instanceof LuaObserver) {

                    IObserver obs = ((LuaObserver) object).observer;

                    while(obs != null && name.startsWith("^")) {
                        obs = obs.parent();
                        name = name.substring(1);
                    }

                    if(obs != null) {
                        v = obs.get(Observer.keys(name));
                    }

                }

                luaState.pushValue(v);

                return 1;
            }

            return 0;
        }
    };

    private static final LuaFunction _funcRemove = new LuaFunction() {

        @Override
        public int invoke(LuaState luaState) {

            int top = luaState.gettop();

            if(top > 1 && luaState.type( - top ) == LuaState.LUA_TOBJECT && luaState.type( - top + 1) == LuaState.LUA_TSTRING) {

                Object object = luaState.toobject(- top);

                String name = luaState.tostring(- top + 1);

                if(object instanceof LuaObserver) {

                    IObserver obs = ((LuaObserver) object).observer;

                    while(obs != null && name.startsWith("^")) {
                        obs = obs.parent();
                        name = name.substring(1);
                    }

                    if(obs != null) {
                        obs.remove(Observer.keys(name));
                    }

                }

            }

            return 0;
        }

    };

    private static final LuaFunction _funcSet = new LuaFunction() {
        @Override
        public int invoke(LuaState luaState) {

            int top = luaState.gettop();

            if(top > 1 && luaState.type( - top ) == LuaState.LUA_TOBJECT && luaState.type( - top + 1) == LuaState.LUA_TSTRING) {

                Object object = luaState.toobject(- top);

                String name = luaState.tostring(- top + 1);

                Object v = top > 2 ? luaState.toValue(-top + 2) : null;

                if(object instanceof LuaObserver) {

                    IObserver obs = ((LuaObserver) object).observer;

                    while(obs != null && name.startsWith("^")) {
                        obs = obs.parent();
                        name = name.substring(1);
                    }

                    if(obs != null) {
                        obs.set(Observer.keys(name),v);
                    }

                }

            }

            return 0;
        }
    };

    private static class LuaListener implements Listener<Object> {

        private final LuaState _luaState;
        private final int _ref;

        public LuaListener(LuaState luaState,int ref) {
            _luaState = luaState;
            _ref = ref;
        }

        @Override
        protected void finalize() throws Throwable {
            _luaState.unref(_ref);
            super.finalize();
        }

        @Override
        public void onChanged(IObserver iObserver, String[] strings, Object o) {

            int top = _luaState.gettop();

            _luaState.getref(_ref);

            if(_luaState.type( - 1) == LuaState.LUA_TFUNCTION) {

                _luaState.pushValue(Observer.joinString(strings));
                _luaState.pushValue(o);

                if(0 != _luaState.pcall(2,0)) {
                    Log.d(Loader.Tag,_luaState.tostring( -1));
                }

            }

            _luaState.pop(_luaState.gettop() - top);
        }

    }

    private static final LuaFunction _funcOn = new LuaFunction() {

        @Override
        public int invoke(LuaState luaState) {

            int top = luaState.gettop();

            if(top > 2 && luaState.type( - top ) == LuaState.LUA_TOBJECT
                    && luaState.type( - top + 1) == LuaState.LUA_TSTRING
                    && luaState.type( - top + 2) == LuaState.LUA_TFUNCTION) {

                Object object = luaState.toobject(- top);

                String name = luaState.tostring( - top + 1);

                Object weakObject = top > 3 ? luaState.toValue(- top +3) : null;

                if(object instanceof LuaObserver) {

                    IObserver obs = ((LuaObserver) object).observer;

                    while(obs != null && name.startsWith("^")) {
                        obs = obs.parent();
                        name = name.substring(1);
                    }

                    if(obs != null) {
                        luaState.pushvalue( - top + 2);
                        int ref = luaState.ref();
                        obs.on(Observer.keys(name),new LuaListener(luaState,ref),weakObject);
                    }

                }

            }

            return 0;
        }
    };

    private static final LuaFunction _funcOff = new LuaFunction() {

        @Override
        public int invoke(LuaState luaState) {

            int top = luaState.gettop();

            if(top > 0 && luaState.type( - top ) == LuaState.LUA_TOBJECT) {

                Object object = luaState.toobject(- top);

                String name = top > 1 && luaState.type(- top +1) == LuaState.LUA_TSTRING ? luaState.tostring( - top + 1) : null;

                Object weakObject = top > 2 ? luaState.toValue(- top +2) : null;

                if(object instanceof LuaObserver) {

                    IObserver obs = ((LuaObserver) object).observer;

                    while(obs != null && name.startsWith("^")) {
                        obs = obs.parent();
                        name = name.substring(1);
                    }

                    if(obs != null) {
                        obs.off(name == null ? null : Observer.keys(name), null, weakObject);
                    }
                }

            }

            return 0;
        }
    };

    public LuaObserver(IObserver observer) {
        this.observer = observer;
    }


    @Override
    public Object get(String s) {
        if("get".equals(s)) {
            return _funcGet;
        }
        else if("set".equals(s)) {
            return _funcSet;
        }
        else if("remove".equals(s)) {
            return _funcRemove;
        }
        else if("on".equals(s)) {
            return _funcOn;
        }
        else if("off".equals(s)) {
            return _funcOff;
        }
        return null;
    }

}
