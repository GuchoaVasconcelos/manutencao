package br.alu.ufc.robertcabral.consultorio.activitys;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import br.alu.ufc.robertcabral.consultorio.entity.App;
import br.alu.ufc.robertcabral.consultorio.entity.Fila;
import br.alu.ufc.robertcabral.consultorio.R;
import br.alu.ufc.robertcabral.consultorio.service.NotificationService;

import static br.alu.ufc.robertcabral.consultorio.entity.App.lastPosition;
import static br.alu.ufc.robertcabral.consultorio.entity.App.isSchedule;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener{

    FirebaseUser user;
    CardView btViewQueue, btSettings, btSchedule, btChegueAteNos;
    //public int lastPosition = 0;
    //boolean isSchedule = false;
    App app;
    DatabaseReference mDatabase;
    DatabaseReference databaseRef;
    int pos;
    static String START_SERVICE_NOTIFICATION = "START_SERVICE_NOTIFICATION";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btViewQueue = findViewById(R.id.btViewQueue);
        btSettings = findViewById(R.id.btSettings);
        btSchedule = findViewById(R.id.btSchedule);
        btChegueAteNos = findViewById(R.id.btChegueAteNos);

        btViewQueue.setOnClickListener(this);
        btSettings.setOnClickListener(this);
        btSchedule.setOnClickListener(this);
        btChegueAteNos.setOnClickListener(this);

        user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        databaseRef = mDatabase.child("lastPosition");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lastPosition = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseRef = mDatabase.child("Queue");

        //Query query = databaseRef;
        //query.addListenerForSingleValueEvent(listener);
        databaseRef.addValueEventListener(listener);
        databaseRef.removeEventListener(listener2);

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
    protected void onStart() {
        super.onStart();

        if(user == null){
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();

        Intent intent = new Intent(this, NotificationService.class);
        startService(intent);

    }

    ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Fila fila = null;
            boolean aux = false;
            for(DataSnapshot ds : dataSnapshot.getChildren()) {

                fila = ds.getValue(Fila.class);

                if(fila != null) {
                    if (fila.getUid().equals(user.getUid())) {
                        isSchedule = true;
                        aux = true;
                    }

                    if(!(fila.getUid().equals(user.getUid())) && !aux) {
                        isSchedule = false;
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ValueEventListener listener2 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Fila fila = null;
            boolean aux = false;
            for(DataSnapshot ds : dataSnapshot.getChildren()) {

                fila = ds.getValue(Fila.class);

                if(fila != null) {
                    if (fila.getUid().equals(user.getUid())) {
                        isSchedule = true;
                        aux = true;
                    }

                    if(!(fila.getUid().equals(user.getUid())) && !aux) {
                        isSchedule = false;
                    }
                }
            }
            if (fila == null){
                lastPosition = 0;
                isSchedule = false;
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        App.paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.paused = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btViewQueue:
                Intent i = new Intent(DashboardActivity.this, QueueActivity.class);
                i.putExtra("lasPosition", lastPosition);
                startActivity(i);
                break;
            case R.id.btSettings:
                startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
                break;
            case R.id.btSchedule:

                pos = 0;

//                int tes = 1;
//
//                FirebaseDatabase.getInstance().getReference("lastPosition")
//                        .setValue(tes);

                if(!isSchedule) {

                    databaseRef = mDatabase.child("lastPosition");

                    databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Toast.makeText(getApplicationContext(), "Position: " + dataSnapshot.getValue(Integer.class), Toast.LENGTH_SHORT).show();

                            Fila fila = new Fila(user.getUid(), user.getDisplayName() ,dataSnapshot.getValue(Integer.class) + 1);

                            FirebaseDatabase.getInstance().getReference("Queue")
                                    .child(user.getUid())
                                    .setValue(fila).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                        isSchedule = true;

                                    FirebaseDatabase.getInstance().getReference("lastPosition")
                                            .setValue(fila.getPosition());
                                }

                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    isSchedule = true;
                    Snackbar.make(v, "Consulta marcada", Snackbar.LENGTH_LONG).show();
                }else
                    Snackbar.make(v, "Você já tem uma consulta marcada", Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btChegueAteNos:
                startActivity(new Intent(DashboardActivity.this, MapsActivity.class));
                break;
        }
    }
}
