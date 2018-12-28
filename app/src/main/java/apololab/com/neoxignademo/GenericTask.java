package apololab.com.neoxignademo;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import java.net.HttpURLConnection;
import java.net.URL;

public abstract class GenericTask extends AsyncTask<Void,Void,String> {

    String url = "";
    Context context;
    protected boolean error = false;

    public GenericTask(Context context, String url){
        this.url = url;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        setLoading(true);
    }

    protected abstract void onSuccess(String content);

    protected void setLoading(boolean loading){

    }

    @Override
    protected String doInBackground(Void... voids) {
        String result = "";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setUseCaches(false);
            connection.setDoInput(true);
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                result = Utils.streamToString( connection.getInputStream() );
            } else if (responseCode == 503){
                this.error = true;
                result = Utils.streamToString( connection.getErrorStream() );
            }
        } catch (Exception ex){
            this.error = true;
            result = context.getResources().getString(R.string.default_error_msg);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        setLoading(false);

        if (error) {
            new AlertDialog.Builder(this.context).setMessage(result).show();
        } else {
            this.onSuccess(result);
        }
    }
}
