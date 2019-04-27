package com.anshaysingh.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebUrls = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int chosenCeleb = 0;
    int locationOfCorrectAnswer = 0;
    String[] answer = new String[4];
    ImageView celeb;
    Button button0;
    Button button1;
    Button button2;
    Button button3;
    public static class DownloadTask extends AsyncTask<String,Void,Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {

                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public class DownloadCelebImage extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            HttpURLConnection httpURLConnection = null;
            URL url;
            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void onClick(View view) {

        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {

            Toast.makeText(getApplicationContext(),"Correct!",Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(),"Incorrect! It is " + celebNames.get(chosenCeleb),Toast.LENGTH_SHORT).show();
        }

        createNewQuestion();
    }

    public void createNewQuestion() {

        Random random = new Random();
        chosenCeleb = random.nextInt(celebUrls.size());

        DownloadTask task = new DownloadTask();
        Bitmap celebImage;
        try {
            celebImage = task.execute(celebUrls.get(chosenCeleb)).get();

            celeb.setImageBitmap(celebImage);
            locationOfCorrectAnswer = random.nextInt(4);
            int incorrectAnswerLocation;
            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answer[i] = celebNames.get(chosenCeleb);
                } else {
                    incorrectAnswerLocation = random.nextInt(celebUrls.size());

                    while (incorrectAnswerLocation == chosenCeleb) {
                        incorrectAnswerLocation = random.nextInt(celebUrls.size());
                    }

                    answer[i] = celebNames.get(incorrectAnswerLocation);
                }
            }
            button0.setText(answer[0]);
            button1.setText(answer[1]);
            button2.setText(answer[2]);
            button3.setText(answer[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        celeb = (ImageView)findViewById(R.id.imageView);
        button0 = (Button) findViewById(R.id.button1);
        button1 = (Button) findViewById(R.id.button2);
        button2 = (Button) findViewById(R.id.button3);
        button3 = (Button) findViewById(R.id.button4);
        DownloadCelebImage downloadCelebImage = new DownloadCelebImage();
        String result = null;

        try {

            result = downloadCelebImage.execute("http://www.posh24.se/kandisar").get();

            String[] splitResult = result.split("<div class=\"listedArticles\">");

            Pattern pimg = Pattern.compile("img src=\"(.*?)\"");
            Matcher mimg = pimg.matcher(splitResult[0]);
            while (mimg.find()) {

                celebUrls.add(mimg.group(1));

            }

            Pattern pname = Pattern.compile("alt=\"(.*?)\"");
            Matcher mname = pname.matcher(splitResult[0]);
            while (mname.find()) {

                celebNames.add(mname.group(1));

            }


        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        createNewQuestion();

    }
}
