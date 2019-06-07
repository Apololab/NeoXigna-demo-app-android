package apololab.com.neoxignademo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    static final String DEMO_WEB_URL = "https://neoxignademo.azurewebsites.net/";
    static final String PATH_PREPARE_XML = "AppApi/XML";
    static final String PATH_CHECK_SIGNED = "AppApi/IsSigned";
    static final String NEOXIGNA_BUNDLE_ID = "com.apololab.neoxigna";

    static final int NEOXIGNA_INVOKE_CODE = 1986;

    static final String SIGNING = "1";
    static final String NOT_SIGNED = "0";
    Button btnStart;
    TextView lblNeoXignaNotInstalled;
    View pnlContent;
    View pgrLoading;

    private static final CookieManager manager = new CookieManager(); // Necesario para preservar la sesión en el proyecto web, ya que

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = findViewById(R.id.btnStart);
        pnlContent = findViewById(R.id.pnlContent);
        pgrLoading = findViewById(R.id.pgrLoading);
        lblNeoXignaNotInstalled = findViewById(R.id.lblNeoXignaNotInstalled);


        // Se revisa que NeoXigna se encuentre instalado, esto es importante ya que al intentar abrir el URI para firmar ocurre una excepción en caso de que NeoXigna no esté instalado
        boolean neoxignaInstalled=  isNeoXignaInstalled();
        lblNeoXignaNotInstalled.setVisibility( neoxignaInstalled ? View.INVISIBLE : View.VISIBLE );
        btnStart.setEnabled( neoxignaInstalled );

        btnStart.setOnClickListener((view)-> startSignature() );
    }

    /**
     * Se invoca el endpoint AppApi/XML, el cual sube un XML de prueba generado en el backend de prueba, y se retorna un URI en la variable neoxignaSchema, el cual debe ponerse como parámetro en un Intent para llamar a NeoXigna
     * Una vez finalizada la firma digital se retorta el control a esta app en el método onActivityResult, en dicho momento ya el backend de prueba debió recibir el DownloadKey , por tanto es posible que únicamente se debe esperar
     * a que éste lo termine de descargar
     */
    private void startSignature(){
        new GenericTask(this,DEMO_WEB_URL + PATH_PREPARE_XML){
            @Override
            protected void onSuccess(String neoxignaSchema) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(neoxignaSchema));
                startActivityForResult(intent,NEOXIGNA_INVOKE_CODE);
            }

            @Override
            protected void setLoading(boolean loading) {
                MainActivity.this.setLoading(loading);
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEOXIGNA_INVOKE_CODE){
            waitSignature();
        }
    }

    private void setLoading(boolean loading){
        pgrLoading.setVisibility( loading ? View.VISIBLE : View.INVISIBLE );
        pnlContent.setVisibility( loading ? View.INVISIBLE : View.VISIBLE);
    }


    /**
     * Se espera a que el backend de pruebas ya tenga el documento descargado, retorna 0 si no llegó el documento, 1 si se está descargando, y cualquier otro valor sería el resultado de la firma
     *  en este caso sería un string con formato [nombre] | [identificacion] | [fecha]
     */
    protected void waitSignature(){
        new GenericTask(this,DEMO_WEB_URL + PATH_CHECK_SIGNED ){

            @Override
            protected String doInBackground(Void... voids) {
                String result = super.doInBackground(voids);
                try {
                    while (result.equals(SIGNING)) {
                        Thread.sleep(2000);
                        result = super.doInBackground(voids);
                    }
                } catch (Exception ex){
                    this.error = true;
                    result = context.getResources().getString(R.string.default_error_msg);
                }
                return result;

            }

            @Override
            protected void onSuccess(String result) {
                String message = result.equals( NOT_SIGNED ) ? MainActivity.this.getString(R.string.document_not_signed) :
                        MainActivity.this.getString(R.string.document_signed) + result;
                new AlertDialog.Builder(this.context).setMessage(message).show();


            }

            @Override
            protected void setLoading(boolean loading) {
                MainActivity.this.setLoading(loading);
            }
        }.execute();
    }

    private boolean isNeoXignaInstalled(){
        PackageManager pm = this.getApplicationContext().getPackageManager();
        try {
            pm.getPackageInfo(NEOXIGNA_BUNDLE_ID, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("Tag",NEOXIGNA_BUNDLE_ID + " not installed");
            return false;
        }
    }

    static {
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);
    }

}
