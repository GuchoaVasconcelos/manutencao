package br.alu.ufc.robertcabral.consultorio.activitys;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import br.alu.ufc.robertcabral.consultorio.R;
import br.alu.ufc.robertcabral.consultorio.entity.Fila;

import static br.alu.ufc.robertcabral.consultorio.entity.App.lastPosition;

public class AdminActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String QUEUE = "Queue";
    public static final String LAST_POSITION = "lastPosition";
    ArrayList<Fila> values = new ArrayList<>();
    int selected;
    ListView listAdmin;
    FirebaseUser user;
    BottomAppBar bottomAppBar;
    FloatingActionButton floatingActionButton;
    DatabaseReference mDatabase;
    DatabaseReference databaseRef;
    FirebaseAuth auth;
    Fila fila;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        selected = -1;

        listAdmin = findViewById(R.id.listAdmin);
        bottomAppBar = findViewById(R.id.bottomAppBarAdmin);
        floatingActionButton = findViewById(R.id.btRefreshAdmin);

        floatingActionButton.setOnClickListener(this);

        bottomAppBar.replaceMenu(R.menu.main_menu_admin);
        bottomAppBar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        auth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Query query = mDatabase.child(QUEUE);

        query.addValueEventListener(listener);


        listAdmin.setOnItemClickListener((parent, view, position, id) -> {
            selected = position;

        });

        listAdmin.setSelector( android.R.color.holo_blue_light );


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

    ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            values.clear();
            for (DataSnapshot data : dataSnapshot.getChildren()){
                values.add(data.getValue(Fila.class));
            }

            Collections.sort(values, (o1, o2) -> Integer.compare(o1.getPosition(), o2.getPosition()));

            ArrayList<String> adapter = new ArrayList<>();

            if(values.size() == 0){
                adapter.clear();
                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(AdminActivity.this, android.R.layout.simple_list_item_1, adapter);
                listAdmin.setAdapter(adapter1);
            }

            for (int i = 0; i < values.size(); i++) {
                adapter.add(values.get(i).position + " : " + values.get(i).getNome());
                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(AdminActivity.this, android.R.layout.simple_list_item_1, adapter);
                listAdmin.setAdapter(adapter1);
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            throw new UnsupportedOperationException();

        }
    };

    @Override
    public void onClick(View v) {
        if (v == findViewById(R.id.btRefreshAdmin)){
            auth.signOut();
            startActivity(new Intent(AdminActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.cancelAdmin:
                if( values.size() > 0 && selected != -1 ){

                    Fila filaSelected = values.get( selected );

                    databaseRef = mDatabase.child(QUEUE);

                    Query query = databaseRef;

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                if (data.getKey().equals(filaSelected.getUid())) {
                                    fila = data.getValue(Fila.class);
                                    data.getRef().removeValue();

                                    databaseRef = mDatabase.child(LAST_POSITION);

                                    databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            lastPosition = dataSnapshot.getValue(Integer.class);
                                            if (fila.getPosition() < lastPosition) {
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
                    selected = -1;
                }
                break;
            case R.id.addNew:
                startActivity(new Intent(AdminActivity.this, AddAdminActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
