package com.example.nicky.simplestretchtimer.aboutactivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nicky.simplestretchtimer.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Nicky on 10/6/17.
 */

public class AboutActivity extends AppCompatActivity {
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.info)
    TextView mInfo;

    private String mTitleText;
    private String mInfoText;
    private AdView mAdView;

    private static final String JSON_URL = "https://api.myjson.com/bins/1045g5";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        ButterKnife.bind(this);
        GetAboutTask getAboutTask = new GetAboutTask();
        URL url = createUrl(JSON_URL);
        getAboutTask.execute(url);
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }


    private class GetAboutTask extends AsyncTask<URL, Integer, String> {

        protected String doInBackground(URL... url) {
            InputStream inputStream = null;
            HttpsURLConnection connection = null;
            String jsonResponseString = null;
            URL myURL = url[0];
            try {
                connection = (HttpsURLConnection) myURL.openConnection();
                connection.setReadTimeout(3000);
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();

                if (connection.getResponseCode() == 200) {
                    inputStream = connection.getInputStream();
                    jsonResponseString = readFromStream(inputStream);
                } else {
                    Toast.makeText(AboutActivity.this, R.string.problem_loading_page, Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(AboutActivity.this, R.string.problem_loading_page, Toast.LENGTH_SHORT).show();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {

                    }
                }
            }
            return jsonResponseString;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(result);
                mTitleText = jsonObject.getString("Title");
                mInfoText = jsonObject.getString("Info");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mTitle.setText(mTitleText);
            mInfo.setText(mInfoText);


        }


        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

    }

    private URL createUrl(String webAddress) {
        URL url = null;
        try {
            url = new URL(webAddress);
        } catch (MalformedURLException e) {

        }
        return url;
    }

}
