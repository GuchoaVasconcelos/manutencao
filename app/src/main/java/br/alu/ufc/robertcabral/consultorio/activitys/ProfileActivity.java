package br.alu.ufc.robertcabral.consultorio.activitys;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import br.alu.ufc.robertcabral.consultorio.entity.App;
import br.alu.ufc.robertcabral.consultorio.entity.Fila;
import br.alu.ufc.robertcabral.consultorio.entity.Progress;
import br.alu.ufc.robertcabral.consultorio.R;
import br.alu.ufc.robertcabral.consultorio.entity.User;

public class ProfileActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, View.OnClickListener {

    Button btUpdateProfile, dateBirthProfile;
    Calendar dateBirth;
    FirebaseUser user;
    DatabaseReference mDatabase;
    EditText etNameProfile, etEmailProfile;
    Progress progress;
    User userServer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        progress = new Progress(ProfileActivity.this);
        userServer = new User();

        dateBirth = Calendar.getInstance();

        progress.start("Aguarde", "Buscando informações");

        btUpdateProfile = findViewById(R.id.btUpdateProfile);
        dateBirthProfile = findViewById(R.id.dateBirthProfile);
        etNameProfile = findViewById(R.id.etNameProfile);
        etEmailProfile = findViewById(R.id.etEmailProfile);

        btUpdateProfile.setOnClickListener(this);
        dateBirthProfile.setOnClickListener(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Query query = mDatabase.child("Users").child(user.getUid());

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userServer = dataSnapshot.getValue(User.class);
                etNameProfile.setText(userServer.getName());
                if(!userServer.getDateBirth().isEmpty())
                    dateBirthProfile.setText(userServer.getDateBirth());
                else
                    userServer.setDateBirth("");
                etEmailProfile.setText(userServer.getEmail());

                if (progress.progressDialog != null && progress.progressDialog.isShowing())
                    progress.stop();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                throw new UnsupportedOperationException();

            }
        };

        query.addValueEventListener(valueEventListener);

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onStart() {
        super.onStart();
        if(user == null){
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finishAffinity();
        }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btUpdateProfile:


                User userUpdated = new User();


                userUpdated.setUid(user.getUid());
                userUpdated.setName(etNameProfile.getText().toString().trim());
                userUpdated.setEmail(etEmailProfile.getText().toString().trim());
                userUpdated.setDateBirth(dateBirthProfile.getText().toString().trim());


                if(!(userUpdated.getName().equals(userServer.getName()) && userUpdated.getDateBirth().equals(userServer.getDateBirth()) && userUpdated.getEmail().equals(userServer.getEmail()))) {

                    if(!(userUpdated.getEmail().equals(userServer.getEmail()))){
                        Progress progress1 = new Progress(ProfileActivity.this);
                        progress1.start("Aguarde", "Atualizando informações");
                        user.updateEmail(userUpdated.getEmail())
                                .addOnCompleteListener(task -> {

                                    if (progress1.progressDialog != null && progress1.progressDialog.isShowing())
                                            progress1.stop();

                                });
                    }

                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(userUpdated);

                    UserProfileChangeRequest a = new UserProfileChangeRequest.Builder()
                            .setDisplayName(userUpdated.getName())
                            .build();

                    user.updateProfile(a);

                    Query query = mDatabase.child("Queue");

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot data : dataSnapshot.getChildren()){
                                Fila fila = data.getValue(Fila.class);

                                if (fila.getUid().equals(user.getUid())){
                                    fila.setNome(userUpdated.getName());

                                    FirebaseDatabase.getInstance().getReference("Queue")
                                            .child(user.getUid())
                                            .setValue(fila);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            throw new UnsupportedOperationException();

                        }
                    });

                    Snackbar.make(getWindow().getCurrentFocus(), "Dados atualizados", Snackbar.LENGTH_LONG).show();
                }

                break;
            case R.id.dateBirthProfile:
                DialogFragment datePicker = new DatePickerFeagment();
                datePicker.show(getSupportFragmentManager(), "date picker");
                break;
        }
    }

    @SuppressLint("ValidFragment")
    private static class DatePickerFeagment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return (new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day));
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar date = Calendar.getInstance();

        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
        dateBirthProfile.setText(dt.format(date.getTime()));

        if(date.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
            dateBirthProfile.setError("Selecione uma data válida");
        else {
            dateBirth = date;
            dateBirthProfile.setError(null);
        }
    }
}
