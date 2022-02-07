package br.alu.ufc.robertcabral.consultorio.activitys;

import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import br.alu.ufc.robertcabral.consultorio.entity.App;
import br.alu.ufc.robertcabral.consultorio.entity.Fila;
import br.alu.ufc.robertcabral.consultorio.R;

import static br.alu.ufc.robertcabral.consultorio.entity.App.lastPosition;
import static br.alu.ufc.robertcabral.consultorio.entity.App.isSchedule;

public class QueueActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String QUEUE = "Queue";
    public static final String LAST_POSITION = "lastPosition";
    ListView listQueue;
    BottomAppBar bottomAppBar;
    FloatingActionButton floatingActionButton;
    DatabaseReference mDatabase;
    DatabaseReference databaseRef;
    Fila fila;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        listQueue = findViewById(R.id.listQueue);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        floatingActionButton = findViewById(R.id.btRefreshQueue);

        floatingActionButton.setOnClickListener(this);

        bottomAppBar.replaceMenu(R.menu.main_menu);
        bottomAppBar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Query query = mDatabase.child(QUEUE);

        query.addValueEventListener(listener);

        Query queryLast = mDatabase.child(LAST_POSITION);

        queryLast.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lastPosition = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                throw new UnsupportedOperationException();

            }
        });

    }

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

    ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            isSchedule = false;
            for (DataSnapshot data : dataSnapshot.getChildren()){
                if(data.getValue(Fila.class).getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                    isSchedule = true;
            }


            ArrayList<Fila> values = new ArrayList<>();
            for (DataSnapshot data : dataSnapshot.getChildren()){
                values.add(data.getValue(Fila.class));
            }

            Collections.sort(values, (o1, o2) -> Integer.compare(o1.getPosition(), o2.getPosition()));

            ArrayList<String> adapter = new ArrayList<>();

            if(values.size() == 0){
                adapter.clear();
                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(QueueActivity.this, android.R.layout.simple_list_item_1, adapter);
                listQueue.setAdapter(adapter1);
            }

            for (int i = 0; i < values.size(); i++) {
                adapter.add(values.get(i).position + " : " + values.get(i).getNome());
                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(QueueActivity.this, android.R.layout.simple_list_item_1, adapter);
                listQueue.setAdapter(adapter1);
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            throw new UnsupportedOperationException();

        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.red:
                if(isSchedule) {

                    Query q = mDatabase.child(QUEUE);

                    q.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if (ds.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    fila = ds.getValue(Fila.class);

                                    Query q1 = mDatabase.child(LAST_POSITION);

                                    q1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (!(fila.getPosition() == dataSnapshot.getValue(Integer.class))) {
                                                // is not a last
                                                Query q2 = mDatabase.child(QUEUE);

                                                q2.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                                        ArrayList<Fila> vetAux = new ArrayList<Fila>();
                                                        for (DataSnapshot data : dataSnapshot1.getChildren()) {
                                                            Fila fila1 = data.getValue(Fila.class);
                                                            vetAux.add(fila1);

                                                        }

                                                        Collections.sort(vetAux, (o1, o2) -> Integer.compare(o1.getPosition(), o2.getPosition()));

                                                        for (int i = 0; i < vetAux.size(); i++) {
                                                            if (vetAux.get(i).getPosition() == (fila.getPosition() + 1)) {
                                                                int aux = vetAux.get(i).getPosition();
                                                                vetAux.get(i).setPosition(fila.getPosition());
                                                                vetAux.get(i - 1).setPosition(aux);
                                                            }
                                                        }

                                                        for (int i = 0; i < vetAux.size(); i++) {
                                                            FirebaseDatabase.getInstance().getReference(QUEUE)
                                                                    .child(vetAux.get(i).getUid())
                                                                    .setValue(vetAux.get(i));

                                                        }

                                                        Toast.makeText(getApplicationContext(), "Sucesso, sua posição na fila é: " + (fila.getPosition() + 1), Toast.LENGTH_LONG).show();

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        throw new UnsupportedOperationException();

                                                    }
                                                });

                                            }else{
                                                Toast.makeText(getApplicationContext(), "Sua posição na fila é: " + (fila.getPosition()), Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            throw new UnsupportedOperationException();

                                        }
                                    });

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            throw new UnsupportedOperationException();

                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Nenhuma consulta cadastrada por você.", Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.cancelSchedule:
                if(isSchedule) {
                    databaseRef = mDatabase.child(QUEUE);

                    Query query = databaseRef;

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                if (data.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    fila = data.getValue(Fila.class);
                                    data.getRef().removeValue();

                                    databaseRef = mDatabase.child(LAST_POSITION);

                                    databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (fila.getPosition() < dataSnapshot.getValue(Integer.class)) {
                                                databaseRef = mDatabase.child(QUEUE);
                                                Query q = databaseRef;

                                                q.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                                        ArrayList<Fila> posicoesParaAtualizar = new ArrayList<Fila>();
                                                        for (DataSnapshot data : dataSnapshot1.getChildren()) {
                                                            Fila fila1 = data.getValue(Fila.class);
                                                            posicoesParaAtualizar.add(fila1);

                                                        }

                                                        Collections.sort(posicoesParaAtualizar, (o1, o2) -> Integer.compare(o1.getPosition(), o2.getPosition()));

                                                        for (int i = 0; i < posicoesParaAtualizar.size(); i++) {
                                                            if (!(posicoesParaAtualizar.get(i).getPosition() < fila.getPosition())) {
                                                                posicoesParaAtualizar.get(i).setPosition(posicoesParaAtualizar.get(i).getPosition() - 1);
                                                            }
                                                        }

                                                        for (int i = 0; i < posicoesParaAtualizar.size(); i++) {
                                                            if (!(posicoesParaAtualizar.get(i).getPosition() < fila.getPosition())) {
                                                                FirebaseDatabase.getInstance().getReference(QUEUE)
                                                                        .child(posicoesParaAtualizar.get(i).getUid())
                                                                        .setValue(posicoesParaAtualizar.get(i));
                                                            }
                                                        }

                                                        lastPosition--;
                                                        FirebaseDatabase.getInstance().getReference(LAST_POSITION)
                                                                .setValue(dataSnapshot.getValue(Integer.class) - 1);

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        throw new UnsupportedOperationException();

                                                    }
                                                });


                                            } else if (fila.getPosition() == lastPosition) {

                                                databaseRef = mDatabase.child(LAST_POSITION);

                                                Query query1 = databaseRef;

                                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        lastPosition--;
                                                        FirebaseDatabase.getInstance().getReference(LAST_POSITION)
                                                                .setValue(dataSnapshot.getValue(Integer.class) - 1);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        throw new UnsupportedOperationException();

                                                    }
                                                });
                                            }
                                            Toast.makeText(getApplicationContext(), "Sucesso, você não tem consulta marcada", Toast.LENGTH_LONG).show();

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            throw new UnsupportedOperationException();

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            throw new UnsupportedOperationException();

                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(), "Nenhuma consulta cadastrada por você.", Toast.LENGTH_LONG).show();

                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btRefreshQueue) {
            Query query = mDatabase.child(QUEUE);

            query.addValueEventListener(listener);
        }
    }
}
