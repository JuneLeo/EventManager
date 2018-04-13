package com.event.leo.event.notify;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by spf on 2017/9/6.
 * 自定义 事件总线
 * <p>
 * 线程池-注解-反射-handler
 * <p>
 * handler发送完成要记得移除Runnable
 */

public class EventManager {

    private static final String TAG = EventManager.class.getSimpleName();
    private static EventManager INSTANCE;
    private List<Object> objects;
    private static ScheduledExecutorService executor;
    private final Handler handler;

    public EventManager() {
        objects = new ArrayList<>();
        executor = Executors.newSingleThreadScheduledExecutor();
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 单例
     *
     * @return
     */
    public static EventManager getDefult() {
        if (INSTANCE == null) {
            synchronized (EventManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new EventManager();
                }
            }
        }
        return INSTANCE;
    }


    /**
     * 注册监听
     *
     * @param o
     */
    public void register(Object o) {
        if (o == null) {
            return;
        }
        if (objects.contains(o)) {
            return;
        }
        objects.add(o);
    }

    /**
     * 反注册
     *
     * @param o
     */
    public void unregister(Object o) {
        if (o == null) {
            return;
        }
        if (objects.contains(o)) {
            objects.remove(o);
        }
    }


    public void post(final String key, final Object o, long delayMillis) {
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(key) || o == null) {
                    return;
                }
                for (final Object object : objects) {
                    Method[] methods = object.getClass().getDeclaredMethods();
                    for (final Method method : methods) {
                        Event annotation = method.getAnnotation(Event.class);
                        if (annotation == null) {
                            continue;
                        }
                        if (!key.equals(annotation.key())) {
                            continue;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    method.invoke(object, o);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, object.getClass().getName() + " " + method.getName() + " at least one object");
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }catch (IllegalArgumentException e){
                                    Log.e(TAG, object.getClass().getName() + " " + method.getName() + " parameter type error , must have a parameter");
                                }
                            }
                        });
                    }
                }
            }


        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    public void post(String key, Object o) {
        post(key, o, 0);
    }


}
