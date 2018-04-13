package com.event.leo.event.notify;

import android.app.Activity;
import android.app.Application;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.event.leo.BuildConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by 宋鹏飞 on 2018/4/13.
 */

public final class ActivityEventManager {

    private static final String TAG = ActivityEventManager.class.getCanonicalName();
    private LinkedBlockingQueue<Pair<String, Object>> notifyQueue;
    private static List<Object> objects = new ArrayList<>();
    private Handler handler;
    private static ActivityEventManager INSTANCE;

    public ActivityEventManager() {
        notifyQueue = new LinkedBlockingQueue<>();
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 单例
     *
     * @return
     */
    public static ActivityEventManager newInstance() {
        if (INSTANCE == null) {
            synchronized (ActivityEventManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ActivityEventManager();
                }
            }
        }
        return INSTANCE;
    }

    public void post(final String key, Object o, final long delay) {
        if (TextUtils.isEmpty(key) || null == o) {
            return;
        }
        Pair<String, Object> pair = new Pair<>(key, o);
        notifyQueue.add(pair);

        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG){
                    Log.e(TAG, Thread.currentThread().getName());
                }
                Pair<String, Object> event;
                try {
                    while ((event = notifyQueue.poll(delay, TimeUnit.MILLISECONDS)) != null) {
                        dispatch(event);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void dispatch(final Pair<String, Object> event) {
        for (final Object object : objects) {
            Method[] methods = object.getClass().getDeclaredMethods();
            for (final Method method : methods) {
                Event annotation = method.getAnnotation(Event.class);
                if (annotation == null) {
                    continue;
                }
                if (annotation.type() != EventType.Event_ACTIVITY) {
                    continue;
                }
                if (!event.first.equals(annotation.key())) {
                    continue;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            method.invoke(object, event.second);
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

    public static void init(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                objects.add(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                objects.remove(activity);
            }
        });
    }

}
