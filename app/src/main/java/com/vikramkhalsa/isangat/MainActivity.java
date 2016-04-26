package com.vikramkhalsa.isangat;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Xml;
import android.view.View;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;


public class MainActivity extends AppCompatActivity {

    public String site = "http://www.isangat.org";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // display the GUI defined in the activity_first.xml file
        setContentView(R.layout.activity_main);


        Switch sw = (Switch) findViewById(R.id.switch1);

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String val = "";
                if (isChecked) {
                    val = "True";
                    site = "http://ekhalsa.com/programs.php";
                } else {
                    site = "http://www.isangat.org";
                    val = "false";
                }
                SharedPreferences prefs = getSharedPreferences("DATE_PREF", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("site", val);
                editor.commit();
            }
        });


        //Check if last load date exists in prefs, if so, show it in the text view
        SharedPreferences prefs = getSharedPreferences("DATE_PREF", Context.MODE_PRIVATE);
        TextView textview = (TextView) findViewById(R.id.textView2);
        if (prefs.contains("date")) {
            textview.setText("Last Updated " + prefs.getString("date", "None"));
        }

        //Check if last load date exists in prefs, if so, show it in the text view
        if (prefs.contains("site")) {
            sw.setChecked(Boolean.parseBoolean(prefs.getString("site", "false")));
        }

        //Check if there is wifi
        ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo net = conManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        //Date now = new Date();
        //String time = (String) DateUtils.getRelativeDateTimeString(this, now.getTime(), DateUtils.HOUR_IN_MILLIS, DateUtils.WEEK_IN_MILLIS,0);
        //Toast.makeText(MainActivity.this, time, Toast.LENGTH_LONG).show();

        //If there is internet, update/download the page
        if (wifi.isConnected()) {
            Toast.makeText(MainActivity.this, "Loading Page from Web", Toast.LENGTH_SHORT).show();
            new webTask(this).execute(site);
        }
        //otherwise just load the page from file
        else {
            Toast.makeText(MainActivity.this, "Loading Page from File", Toast.LENGTH_SHORT).show();
            loadPage();
        }
    }

    /*
     * called when the update page button is clicked
     * @param v the View which triggered the method call: should refer to the button "enter"
     */
    public void enter(View v) {
        Toast.makeText(MainActivity.this, "Loading Page from Web", Toast.LENGTH_SHORT).show();
        new webTask(this).execute(site);


    }

    //Reloads the webview with contents from the saved file
    private void loadPage() {
        try {

            WebView webview1 = (WebView) findViewById(R.id.webView1);
            FileInputStream fin = openFileInput("isangatTemp.html");

            int c;
            String temp = "";

            while ((c = fin.read()) != -1) {
                temp = temp + Character.toString((char) c);
            }
            /*

               XPath xpath = XPathFactory.newInstance().newXPath();
            String expression = "//tr";
            InputSource inputSrc = new InputSource(temp);
            NodeList nodes = (NodeList)xpath.evaluate(expression, inputSrc, XPathConstants.NODESET);

            Toast.makeText(this, "count: " + String.valueOf(nodes.getLength()),Toast.LENGTH_SHORT).show();
            // if node found
            if(nodes != null && nodes.getLength() > 0) {
                //mPeople.clear();
                int len = nodes.getLength();
                for(int i = 0; i < len; ++i) {
                    // query value
                    Node node = nodes.item(i);
                    Toast.makeText(this, node.getTextContent(),Toast.LENGTH_SHORT).show();
                    // mPeople.add(node.getTextContent());
                }
            }
*/

            webview1.loadDataWithBaseURL("file:///isangatTemp.html", temp, "text/html", "UTF-8", null);
        } catch (FileNotFoundException ex) {
            Toast.makeText(getBaseContext(), "File not found, attempting to load from Web.", Toast.LENGTH_LONG).show();
            new webTask(this).execute("");
        } catch (Exception ex) {
            Toast.makeText(getBaseContext(), "Could not load file" + ex.getMessage(), Toast.LENGTH_LONG).show();

        }
    }

    public class webTask extends AsyncTask<String, Integer, String> {

        private Context mContext;
        public webTask (Context context) {
            mContext = context;
        }

        protected String doInBackground(String... temp) {
            String ret = "";
            try {

                URL url = new URL(temp[0]);
                //URL url = new URL("http://ekhalsa.com/programs.php");
                //create the new connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //set up some things on the connection
                //urlConnection.setRequestProperty("User-Agent", USERAGENT);  //if you are not sure of user agent just set choice=0
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                //this will be used to write the downloaded data into the file we created
                FileOutputStream fileOutput = openFileOutput("isangatTemp.html", Context.MODE_PRIVATE);
                //this is the total size of the file
                int totalSize = urlConnection.getContentLength();
                //variable to store total downloaded bytes
                int downloadedSize = 0;

                //create a buffer...
                byte[] buffer = new byte[1024];
                int bufferLength = 0; //used to store a temporary size of the buffer

                //write the contents to the file
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bufferLength);
                }
                //close the output stream when done
                fileOutput.close();
                inputStream.close();
                urlConnection.disconnect();
                ret = "success";

            } catch (Exception exception) {
                ret = exception.getMessage();
            } finally {
                return ret;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
            if (result == "success") {
                Toast.makeText(MainActivity.this, "File Downloaded", Toast.LENGTH_SHORT).show();
                TextView textview = (TextView) findViewById(R.id.textView2);
                Calendar cal = Calendar.getInstance();

                Date now = new Date();
                CharSequence timeString;
                timeString = DateUtils.getRelativeDateTimeString(mContext, now.getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS,0);

                //String time = DateUtils.getRelativeDateTimeString(MainActivity.this, cal.getTimeInMillis(), DateUtils.HOUR_IN_MILLIS, DateUtils.WEEK_IN_MILLIS);
                //String time = cal.getTime().toString();
                SharedPreferences prefs = getSharedPreferences("DATE_PREF", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("date", timeString.toString());
                editor.commit();

                textview.setText("Last Updated " + timeString);
                loadPage();
            } else {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        }
    }



}


