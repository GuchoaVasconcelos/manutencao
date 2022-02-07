package br.alu.ufc.robertcabral.consultorio.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import br.alu.ufc.robertcabral.consultorio.R;
import br.alu.ufc.robertcabral.consultorio.activitys.QueueActivity;
import br.alu.ufc.robertcabral.consultorio.entity.App;
import br.alu.ufc.robertcabral.consultorio.entity.Fila;
import br.alu.ufc.robertcabral.consultorio.entity.User;

import static br.alu.ufc.robertcabral.consultorio.entity.App.lastPosition;

public class NotificationService extends Service {
    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Worker w = new Worker();
        w.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    class Worker extends Thread {
        int position = 0;
        DatabaseReference mDatabase;
        FirebaseUser user;

        Worker(){
            user = FirebaseAuth.getInstance().getCurrentUser();

            mDatabase = FirebaseDatabase.getInstance().getReference();

            Query query = mDatabase.child("Queue");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        if(data.getValue(Fila.class).getUid().equals(user.getUid()))
                            position = data.getValue(Fila.class).getPosition();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Query queryLast = mDatabase.child("lastPosition");

            queryLast.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    lastPosition = dataSnapshot.getValue(Integer.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        @Override
        public void run() {
            Query q = mDatabase.child("Queue");

            q.addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    Log.e("Notificacao", "Mudou o banco");
                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        Fila fila = data.getValue(Fila.class);
                        if(fila.getUid().equals(user.getUid()) && fila.getPosition() != position){
                            position = fila.getPosition();

                            if ((!App.isRunning(getApplicationContext())) || App.paused) {
                                Log.e("Notificacao", "Aplicação não tá rodando!, notifica!!!");
                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                                        .setSmallIcon(R.drawable.notification_icon)
                                        .setContentText("Sua posição na fila mudou! Você é o " + position + " da fila.")
                                        .setContentTitle("Consultorio")
                                        .setAutoCancel(true)
                                        .setPriority(NotificationCompat.PRIORITY_MAX);

                                Intent resultIntent = new Intent(getApplicationContext(), QueueActivity.class);
                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                                stackBuilder.addParentStack(QueueActivity.class);

                                stackBuilder.addNextIntent(resultIntent);
                                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                                mBuilder.setContentIntent(resultPendingIntent);

                                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                manager.notify(1, mBuilder.build());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

}
