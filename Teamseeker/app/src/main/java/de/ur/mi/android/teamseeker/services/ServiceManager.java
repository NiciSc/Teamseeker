package de.ur.mi.android.teamseeker.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

public class ServiceManager {
    private static ServiceManager instance;
    private Context context;
    private Map<Class<? extends Service>, Intent> intents = new HashMap<>();

    public ServiceManager(Context context) {
        instance = ServiceManager.this;
        this.context = context;
    }

    public static ServiceManager getInstance() {
        return instance;
    }

    public void createServiceInstance(Class<? extends Service> serviceClass, Intent intent) {
        if (!isServiceRunning(serviceClass)) {
            context.startService(intent);
            intents.put(serviceClass, intent);
        } else {
            updateServiceInstance(serviceClass, intent);
        }
    }

    private Intent getIntentForService(Class<? extends Service> serviceClass) {
        return intents.get(serviceClass);
    }

    public void updateServiceInstance(Class<? extends Service> serviceClass, Intent intent) {
        context.startService(intent);
        intents.remove(serviceClass);
        intents.put(serviceClass, intent);
    }

    /**
     * stops service, currently not needed since it autoshuts down with the app
     * use later when service is made persistent across app restarts
     *
     * @param serviceClass
     */
    public void stopServiceInstance(Class<? extends Service> serviceClass) {
        if (isServiceRunning(serviceClass)) {
            context.stopService(getIntentForService(serviceClass));
        }
    }

    private boolean isServiceRunning(Class<? extends Service> serviceClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
