package br.alu.ufc.robertcabral.consultorio.entity;

import android.app.ProgressDialog;
import android.content.Context;

public class Progress extends ProgressDialog {

    Context context;
    public ProgressDialog progressDialog;
    public Progress(Context context) {
        super(context);
        this.context = context;
    }

    public void start(String title, String message){
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setTitle(title);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        progressDialog.setCancelable(false);
    }

    public void stop(){
        progressDialog.dismiss();
    }

}
