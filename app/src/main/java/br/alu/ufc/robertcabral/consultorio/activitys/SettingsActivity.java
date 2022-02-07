package br.alu.ufc.robertcabral.consultorio.activitys;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.alu.ufc.robertcabral.consultorio.entity.App;
import br.alu.ufc.robertcabral.consultorio.entity.Progress;
import br.alu.ufc.robertcabral.consultorio.R;
import br.alu.ufc.robertcabral.consultorio.service.NotificationService;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ListView listViewSettings;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        String[] settings = {"Perfil", "Notificações", "Sair"};
        listViewSettings = findViewById(R.id.listViewSettings);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, settings);
        listViewSettings.setAdapter(adapter);
        listViewSettings.setOnItemClickListener(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(user == null){
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            finish();
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
                break;
            case 1:
                Intent intent = new Intent();
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

                intent.putExtra("app_package", getPackageName());
                intent.putExtra("app_uid", getApplicationInfo().uid);

                intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());

                startActivity(intent);
                break;
            case 2:
                Progress progress = new Progress(SettingsActivity.this);
                FirebaseAuth.getInstance().signOut();
                stopService(new Intent(SettingsActivity.this, NotificationService.class));
                Intent i = new Intent(SettingsActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finishAffinity();
                break;
        }
    }
}
