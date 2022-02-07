package br.alu.ufc.robertcabral.consultorio.activitys;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import br.alu.ufc.robertcabral.consultorio.R;
import br.alu.ufc.robertcabral.consultorio.entity.Fila;
import br.alu.ufc.robertcabral.consultorio.entity.User;

import static br.alu.ufc.robertcabral.consultorio.entity.App.lastPosition;

public class AddAdminActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String QUEUE = "Queue";
    public static final String LAST_POSITION = "lastPosition";
    ArrayList<User> users = new ArrayList<>();
    int selected;
    ListView listAdmin;
    BottomAppBar bottomAppBar;
    FloatingActionButton floatingActionButton;
    DatabaseReference mDatabase;
    DatabaseReference databaseRef;
    FirebaseAuth auth;
    ArrayList<String> adapter = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_admin);


        selected = -1;

        listAdmin = findViewById(R.id.listAddAdmin);
        bottomAppBar = findViewById(R.id.bottomAppBarAddAdmin);
        floatingActionButton = findViewById(R.id.btRefreshAddAdmin);

        floatingActionButton.setOnClickListener(this);

        bottomAppBar.replaceMenu(R.menu.main_menu_add_admin);
        bottomAppBar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        auth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();


        Query list = mDatabase.child(QUEUE);
        list.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<String> uids = new ArrayList<>();

                for(DataSnapshot data : dataSnapshot.getChildren()){
                    uids.add(data.getValue(Fila.class).getUid());
                }

                Query listUsersBase = mDatabase.child("Users");
                listUsersBase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        adapter.clear();
                        users.clear();

                        for (DataSnapshot data1 : dataSnapshot.getChildren()) {
                            if (!uids.contains(data1.getKey())) {
                                users.add(data1.getValue(User.class));
                            }
                        }

                        if(users.size() == 0){
                            adapter.clear();
                            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(AddAdminActivity.this, android.R.layout.simple_list_item_1, adapter);
                            listAdmin.setAdapter(adapter1);
                        }

                        for (int i = 0; i < users.size(); i++) {
                            adapter.add((i + 1) + " - " + users.get(i).getName());
                            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(AddAdminActivity.this, android.R.layout.simple_list_item_1, adapter);
                            listAdmin.setAdapter(adapter1);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        throw new UnsupportedOperationException();

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                throw new UnsupportedOperationException();

            }
        });

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
            users.clear();
            for (DataSnapshot data : dataSnapshot.getChildren()){
                users.add(data.getValue(User.class));
            }

            ArrayList<String> adapter = new ArrayList<>();

            for (int i = 0; i < users.size(); i++) {
                adapter.add((i + 1) + " - " + users.get(i).getName());
                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(AddAdminActivity.this, android.R.layout.simple_list_item_1, adapter);
                listAdmin.setAdapter(adapter1);
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            throw new UnsupportedOperationException();

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_add_admin, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cancelAddAdmin:

                if( users.size() > 0 && selected != -1 ) {
                    User user = users.get(selected);

                    databaseRef = mDatabase.child(LAST_POSITION);

                    databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Fila fila = new Fila(user.getUid(), user.getName(), dataSnapshot.getValue(Integer.class) + 1);

                            FirebaseDatabase.getInstance().getReference(QUEUE)
                                    .child(user.getUid())
                                    .setValue(fila).addOnCompleteListener(task -> FirebaseDatabase.getInstance().getReference(LAST_POSITION)
                                            .setValue(fila.getPosition()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            throw new UnsupportedOperationException();

                        }
                    });
                }
                break;
            case R.id.addNewAddAdmin:
                AlertDialog.Builder alertbox = new AlertDialog.Builder(this);

                LinearLayout ll_alert_layout = new LinearLayout(this);
                ll_alert_layout.setOrientation(LinearLayout.VERTICAL);
                final EditText ed_input = new EditText(this);
                ll_alert_layout.addView(ed_input);

                alertbox.setTitle("Consultorio");
                alertbox.setIcon(R.drawable.ic_launcher);
                alertbox.setMessage("Digite o nome do paciente: ");

                alertbox.setView(ll_alert_layout);

                alertbox.setPositiveButton("OK",
                        (arg0, arg1) -> {

                            String input_text = ed_input.getText().toString();

                            Toast.makeText(getApplicationContext(), input_text, Toast.LENGTH_LONG).show();

                            String uid = mDatabase.push().getKey();

                            Fila fila = new Fila(uid, input_text, lastPosition + 1);

                            FirebaseDatabase.getInstance().getReference(QUEUE)
                                    .child(uid)
                                    .setValue(fila).addOnCompleteListener(task -> FirebaseDatabase.getInstance().getReference(LAST_POSITION)
                                    .setValue(fila.getPosition()));

                        });
                alertbox.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        throw new UnsupportedOperationException();

    }
}
