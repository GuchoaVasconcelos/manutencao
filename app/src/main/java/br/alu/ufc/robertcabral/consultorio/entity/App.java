package br.alu.ufc.robertcabral.consultorio.entity;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.List;

public class App extends Application {

    public static NetworkInfo[] networkInfos;
    public static ConnectivityManager connectivityManager;
    public static ActivityManager manager;
    public static int lastPosition;
    public static boolean isSchedule = false;
    public static boolean paused = false;

    @Override
    public void onCreate() {
        super.onCreate();

        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

    }

    public static boolean haveNetwork(){
        boolean haveWifi = false;
        boolean have3g = false;

        networkInfos = connectivityManager.getAllNetworkInfo();

        for (NetworkInfo info : networkInfos){
            if(info.getTypeName().equalsIgnoreCase("WIFI"))
                if(info.isConnected())
                    haveWifi = true;
            if(info.getTypeName().equalsIgnoreCase("MOBILE"))
                if(info.isConnected())
                    have3g = true;
        }
        return haveWifi || have3g;
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }

        return false;
    }

}
