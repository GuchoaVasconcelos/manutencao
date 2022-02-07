package br.alu.ufc.robertcabral.consultorio.activitys;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import br.alu.ufc.robertcabral.consultorio.entity.Progress;
import br.alu.ufc.robertcabral.consultorio.R;
import br.alu.ufc.robertcabral.consultorio.entity.User;

public class SignupActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    TextView btCadastradoSignup;
    EditText etNameSignup, etEmailSignup, etPasswordSignup, etRePasswordSignup ;
    Calendar dateBirth;
    Button btSignup, dateBirthSignup;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    Progress progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progress = new Progress(SignupActivity.this);
        setContentView(R.layout.activity_signup);

        // EditText
        etNameSignup = findViewById(R.id.etNameSignup);
        etEmailSignup = findViewById(R.id.etEmailSignup);
        etPasswordSignup = findViewById(R.id.etPasswordSignup);
        etRePasswordSignup = findViewById(R.id.etRePasswordSignup);
        btCadastradoSignup = findViewById(R.id.btCadastradoSignup);
        dateBirthSignup = findViewById(R.id.dateBirthSignup);

        // Button
        btSignup = findViewById(R.id.btSignup);

        // Rotines
        btSignup.setOnClickListener(btSignupRotine);
        btCadastradoSignup.setOnClickListener(btCadastradoSignupRotine);
        dateBirthSignup.setOnClickListener(dateBirthRotine);

        mAuth = FirebaseAuth.getInstance();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onStart() {
        super.onStart();

        if(user != null && user.getEmail().equals("admin@consultorio.br")){
            Intent i = new Intent(SignupActivity.this, AdminActivity.class);
            startActivity(i);
        } else if(user != null) {
            Intent i = new Intent(SignupActivity.this, DashboardActivity.class);
            startActivity(i);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progress.progressDialog != null && progress.progressDialog.isShowing())
            progress.stop();
    }

    private void registerUser(){
        final String name, email, password, repassword;

        name = etNameSignup.getText().toString().trim();
        email = etEmailSignup.getText().toString().trim();
        password = etPasswordSignup.getText().toString().trim();
        repassword = etRePasswordSignup.getText().toString().trim();

        if(name.isEmpty()){
            etNameSignup.setError("Digite o nome");
            etNameSignup.requestFocus();
            return;
        }

        if (dateBirth == null){
            dateBirthSignup.setError("Selecione a data de nascimento");
            return;
        }

        final SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");

        if(email.isEmpty()){
            etEmailSignup.setError("Digite o email");
            etEmailSignup.requestFocus();
            return;
        }

        if(password.isEmpty()){
            etPasswordSignup.setError("Digite a senha");
            etPasswordSignup.requestFocus();
            return;
        }

        if(repassword.isEmpty()){
            etRePasswordSignup.setError("Digite a senha");
            etRePasswordSignup.requestFocus();
            return;
        }

        if(!password.equals(repassword)){
            etRePasswordSignup.setError("Senhas não correspondem");
            etPasswordSignup.setError("Senhas não correspondem");
            etPasswordSignup.requestFocus();
            return;
        }

        progress.start("Aguarde", "Realizando o cadastro");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            User newUser = new User(
                                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    name,
                                    email,
                                    dt.format(dateBirth.getTime())
                            );

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(newUser);

                            user = mAuth.getCurrentUser();

                            UserProfileChangeRequest a = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(newUser.getName())
                                    .build();

                            user.updateProfile(a);

                            if(user != null) {
                                startActivity(new Intent(SignupActivity.this, DashboardActivity.class));
                                finishAffinity();
                            }

                        }else{

                            if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                                if (progress.progressDialog != null && progress.progressDialog.isShowing())
                                    progress.stop();
                                Snackbar.make(getWindow().getCurrentFocus(), "Email já cadastrado.", Snackbar.LENGTH_LONG).show();
                            }
                            else {
                                if (progress.progressDialog != null && progress.progressDialog.isShowing())
                                    progress.stop();
                                Snackbar.make(getWindow().getCurrentFocus(), "Erro, tente novamente mais tarde.", Snackbar.LENGTH_LONG).show();
                                Log.e("SignUp", task.getException().getMessage());
                            }

                        }
                    }
                });

    }

    View.OnClickListener btSignupRotine = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            registerUser();
        }
    };

    View.OnClickListener btCadastradoSignupRotine = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    };

    View.OnClickListener dateBirthRotine = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DialogFragment datePicker = new DatePickerFeagment();
            datePicker.show(getSupportFragmentManager(), "date picker");
        }
    };


    public static class DatePickerFeagment extends DialogFragment {

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
        dateBirthSignup.setText(dt.format(date.getTime()));

        if(date.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
            dateBirthSignup.setError("Selecione uma data válida");
        else {
            dateBirth = date;
            dateBirthSignup.setError(null);
        }



    }
}
