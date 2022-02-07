package br.alu.ufc.robertcabral.consultorio.activitys;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONException;

import java.util.Arrays;

import br.alu.ufc.robertcabral.consultorio.entity.Progress;
import br.alu.ufc.robertcabral.consultorio.R;
import br.alu.ufc.robertcabral.consultorio.entity.User;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final private static int GOOGLE_SIGN = 0;
    public static final String AGUARDE = "Aguarde";
    public static final String FAZENDO_O_LOGIN = "Fazendo o login";
    public static final String LOGIN = "Login";
    public static final String USERS = "Users";
    EditText etEmailLogin, etPasswordLogin;
    TextView btCadastreLogin;
    Button btLogin, btForgotPasswordLogin;
    ImageView btGoogleLogin;
    FirebaseAuth mAuth;
    FirebaseUser user;
    GoogleSignInClient mGoogleSignInClient;
    CallbackManager callbackManager;
    LoginButton btFacebookLogin;
    TwitterLoginButton btTwitterLogin;
    Progress progress;
    DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        progress = new Progress(MainActivity.this);

        Twitter.initialize(getApplicationContext());

        FacebookSdk.setApplicationId("418219605632172");
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        btCadastreLogin = findViewById(R.id.tvCadastreLogin);
        btLogin = findViewById(R.id.buttonLogin);
        etEmailLogin = findViewById(R.id.etEmailLogin);
        etPasswordLogin = findViewById(R.id.etPasswordLogin);
        btForgotPasswordLogin = findViewById(R.id.btForgotPasswordLogin);
        btGoogleLogin = findViewById(R.id.btGoogleLogin);
        btFacebookLogin = findViewById(R.id.btFacebookLogin);
        btTwitterLogin = findViewById(R.id.btTwitterLogin);

        btForgotPasswordLogin.setOnClickListener(this);
        btLogin.setOnClickListener(this);
        btCadastreLogin.setOnClickListener(this);
        btGoogleLogin.setOnClickListener(this);
        btFacebookLogin.setOnClickListener(this);
        btTwitterLogin.setOnClickListener(this);


        btTwitterLogin.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                final String[] email = {null};

                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();

                TwitterAuthClient authClient = new TwitterAuthClient();
                authClient.requestEmail(session, new Callback<String>() {
                    @Override
                    public void success(Result<String> result) {
                        email[0] = result.data;
                    }

                    @Override
                    public void failure(TwitterException exception) {

                    }
                });

                AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token,session.getAuthToken().secret);


                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {


                                user = mAuth.getCurrentUser();

                                Query query = mDatabase.child(USERS).child(user.getUid());

                                ValueEventListener valueEventListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        User verify = dataSnapshot.getValue(User.class);

                                        if(verify == null){
                                            User newUser = new User(
                                                    mAuth.getCurrentUser().getUid(),
                                                    mAuth.getCurrentUser().getDisplayName(),
                                                    email[0],
                                                    ""
                                            );

                                            FirebaseDatabase.getInstance().getReference(USERS)
                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .setValue(newUser);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                };

                                query.addValueEventListener(valueEventListener);

                                Intent i = new Intent(MainActivity.this, DashboardActivity.class);
                                startActivity(i);
                                finish();

                            }
                            else{
                                Log.e("Login Erro: ", "Erro login Twitter: " + task.getException().getMessage());
                                if (task.getException() instanceof FirebaseAuthUserCollisionException){
                                    if (progress.progressDialog != null && progress.progressDialog.isShowing())
                                        progress.stop();
                                    Snackbar.make(getWindow().getCurrentFocus(), "Email já cadastrado usando outra rede social.", Snackbar.LENGTH_LONG).show();

                                }else {
                                    if (progress.progressDialog != null && progress.progressDialog.isShowing())
                                        progress.stop();
                                    Snackbar.make(getWindow().getCurrentFocus(), "Tente novamente mais tarde.", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
            }

            @Override
            public void failure(TwitterException exception) {
                Snackbar.make(getWindow().getCurrentFocus(), "Erro, tente novamente mais tarde", Snackbar.LENGTH_LONG).show();
                Log.e("Erro Twitter Login: ", exception.getMessage());
            }
        });
        //progressDialog.dismiss();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestScopes(new Scope("profile"))
                .requestProfile()
                .build();


        mDatabase = FirebaseDatabase.getInstance().getReference();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        callbackManager = CallbackManager.Factory.create();

    }



    void signInFacebook(){
        fazendoLogin();
        btFacebookLogin.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday"));

        btFacebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                GraphRequest request = GraphRequest.newMeRequest(
                        token,
                        (object, response) -> {
                            String email = null;
                            String birthDay = null;
                            try {
                                email = object.getString("email");
                                birthDay = object.getString("birthday");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            handleFacebookToken(token, email, birthDay);
                        }
                );

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                if (progress.progressDialog != null && progress.progressDialog.isShowing())
                    progress.stop();
                //Snackbar.make(getWindow().getCurrentFocus(), "Usuário cancelou", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                if (progress.progressDialog != null && progress.progressDialog.isShowing())
                    progress.stop();
                Snackbar.make(getWindow().getCurrentFocus(), "Erro, tente novamente mais tarde.", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void fazendoLogin() {
        progress.start(AGUARDE, FAZENDO_O_LOGIN);
    }

    private void handleFacebookToken(AccessToken accessToken, String email, String birthDay) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        user = mAuth.getCurrentUser();

                        Query query = mDatabase.child(USERS).child(user.getUid());

                        ValueEventListener valueEventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User verify = dataSnapshot.getValue(User.class);

                                if(verify == null){
                                    User newUser = new User(
                                            user.getUid(),
                                            user.getDisplayName(),
                                            email,
                                            birthDay
                                    );

                                    FirebaseDatabase.getInstance().getReference(USERS)
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(newUser);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        query.addValueEventListener(valueEventListener);

                        Intent i = new Intent(MainActivity.this, DashboardActivity.class);
                        startActivity(i);
                        finish();
                    }
                });
    }

    void signInGoogle(){
        Intent signIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signIntent, GOOGLE_SIGN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        btTwitterLogin.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case GOOGLE_SIGN:
                Task<GoogleSignInAccount> task = GoogleSignIn
                        .getSignedInAccountFromIntent(data);

                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if(account != null) firebaseAuthWithGoogle(account);
                }catch (ApiException e){
                    Log.e(LOGIN, e.getMessage());
                }

                break;
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(LOGIN, "Login Google: " + account.getId());

        AuthCredential credential = GoogleAuthProvider
                .getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                        if(task.isSuccessful()) {
                            Log.d(LOGIN, "Login Google success");
                            user = mAuth.getCurrentUser();


                            Query query = mDatabase.child(USERS).child(user.getUid());

                            ValueEventListener valueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User verify = dataSnapshot.getValue(User.class);

                                    if(verify == null){
                                        User newUser = new User(
                                                user.getUid(),
                                                user.getDisplayName(),
                                                user.getEmail(),
                                                ""
                                        );

                                        FirebaseDatabase.getInstance().getReference(USERS)
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(newUser);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            };

                            query.addValueEventListener(valueEventListener);

                            Intent i = new Intent(MainActivity.this, DashboardActivity.class);
                            startActivity(i);
                            finish();

                        }else{
                            Log.e(LOGIN, "Login Google unsuccess", task.getException());
                        }

                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser() != null){
            user = mAuth.getCurrentUser();
            if(user.getEmail().equals("admin@consultorio.br")){
                Intent i = new Intent(MainActivity.this, AdminActivity.class);
                startActivity(i);
            } else {
                Intent i = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(i);
            }
            finish();
        }else{
            if(AccessToken.getCurrentAccessToken() != null)
                LoginManager.getInstance().logOut();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progress.progressDialog != null && progress.progressDialog.isShowing())
            progress.stop();
    }

    void makeLogin(){
        String email, password;

        email = etEmailLogin.getText().toString().trim();
        password = etPasswordLogin.getText().toString().trim();

        if(email.isEmpty()){
            etEmailLogin.setError("Digite o email");
            etEmailLogin.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmailLogin.setError("Email invalido");
            etEmailLogin.requestFocus();
            return;
        }

        if(password.isEmpty()){
            etPasswordLogin.setError("Digite a senha");
            etPasswordLogin.requestFocus();
            return;
        }

        fazendoLogin();


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            if(email.equals("admin@consultorio.br")){
                                Intent i = new Intent(MainActivity.this, AdminActivity.class);
                                startActivity(i);
                            } else {
                                Intent i = new Intent(MainActivity.this, DashboardActivity.class);
                                startActivity(i);
                            }
                        }else{
                            if(task.getException() instanceof FirebaseAuthInvalidUserException){
                                if (progress.progressDialog != null && progress.progressDialog.isShowing())
                                    progress.stop();
                                Snackbar.make(getWindow().getCurrentFocus(), "Usuário não encontrado", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonLogin:
                makeLogin();
                break;
            case R.id.tvCadastreLogin:
                Intent i = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(i);
                break;
            case R.id.btForgotPasswordLogin:
                String email = etEmailLogin.getText().toString().trim();
                if(email.isEmpty()){
                    etEmailLogin.setError("Digite o email");
                    etEmailLogin.requestFocus();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    etEmailLogin.setError("Email invalido");
                    etEmailLogin.requestFocus();
                    return;
                }

                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Snackbar.make(getWindow().getCurrentFocus(), "Email enviado", Snackbar.LENGTH_LONG).show();
                                    Log.d("SignIn", "Email enviado.");
                                }else{
                                    Log.e("SignIn", "Erro ao enviar o email.");
                                }
                            }
                        });
                break;

            case R.id.btGoogleLogin:
                fazendoLogin();
                signInGoogle();
                break;

            case R.id.btFacebookLogin:
                signInFacebook();
                break;

            case R.id.btTwitterLogin:
                fazendoLogin();
                break;
        }
    }
}
