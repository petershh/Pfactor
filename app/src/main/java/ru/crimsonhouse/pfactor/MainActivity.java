package ru.crimsonhouse.pfactor;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.AsyncTask;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button button;
    EditText input;
    TextView output;
    ProgressBar pb;

    AsyncTask<Integer, Void, Boolean> primeTask;
    AsyncTask<Integer, Void, TreeMap<Integer, Integer>> digestTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(this);

        input = (EditText)findViewById(R.id.editText);
        output = (TextView)findViewById(R.id.textView);
        pb = (ProgressBar)findViewById(R.id.progressBar);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO
    }

    native TreeMap<Integer, Integer> factorize(int x);

    native boolean isPrime(int x);

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public void onClick(View v){
        Editable numRaw = input.getText();
        if(numRaw.length()!=0) {
            int num = Integer.parseInt(numRaw.toString());
            if (num == 0) output.setText("Нуль делится на себя");
            else if (num == 1) output.setText("Единица - ни простое, ни составное число");
            else if (num < 0) num = (0 - num);
            if(checkSimplicity(num)) output.setText("Это простое число.");
            else{
                digestTask = new DigestTask();
                TreeMap<Integer, Integer> digest = new TreeMap<>();
                try {
                    digest = digestTask.execute().get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                StringBuilder sb = new StringBuilder(Integer.toString(num)+" = ");
                for(Map.Entry<Integer, Integer> entry: digest.entrySet()){
                    sb.append(Integer.toString(entry.getKey()));
                    sb.append("<sup>");
                    sb.append(Integer.toString(entry.getValue()));
                    sb.append("</sup> * ");
                }
                sb.deleteCharAt(sb.length()-1);
                Spanned spanned;
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
                    spanned = Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY);
                }
                else spanned = Html.fromHtml(sb.toString());
                output.setText(spanned);
            }
        }
    }

    private boolean checkSimplicity(int x){
        primeTask=new PrimeTask();
        boolean result=false;
        try {
            result = primeTask.execute(x).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    private class PrimeTask extends AsyncTask<Integer, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
            button.setClickable(false);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            pb.setVisibility(View.INVISIBLE);
            button.setClickable(true);
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            return isPrime(params[0]);
        }
    }

    private class DigestTask extends AsyncTask<Integer, Void, TreeMap<Integer, Integer>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
            button.setClickable(false);
        }

        @Override
        protected void onPostExecute(TreeMap<Integer, Integer> result) {
            super.onPostExecute(result);
            pb.setVisibility(View.INVISIBLE);
            button.setClickable(true);
        }

        @Override
        protected TreeMap<Integer, Integer> doInBackground(Integer... params) {
            return factorize(params[0]);
        }
    }
}
