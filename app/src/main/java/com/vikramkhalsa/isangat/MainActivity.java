package com.vikramkhalsa.isangat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    public String site = "http://www.ekhalsa.com/m";
    private ArrayList<program> Programs =new ArrayList<program>();
    ArrayList<String> temp = new ArrayList<String>();
    ArrayAdapter<String> adapter = null;
    CustomArrayAdapter cadapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Just for testing, allow network access in the main thread
        //NEVER use this is productive code
        StrictMode.ThreadPolicy policy = new StrictMode.
                ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);

        // display the GUI defined in the activity_first.xml file
        setContentView(R.layout.activity_main);

        final Context vtc = this;

        Switch sw = (Switch) findViewById(R.id.switch1);
        final WebView ekhalsa = (WebView)findViewById(R.id.webView1);
        final ListView mainList = (ListView)findViewById(R.id.listView1);


        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String val = "";
                if (isChecked) {
                    val = "True";
                   // site = "http://ekhalsa.com/m/";
                    ekhalsa.setVisibility(View.VISIBLE);
                    mainList.setVisibility(View.INVISIBLE);

                } else {
                    //site = "http://www.isangat.org";
                    val = "false";
                    ekhalsa.setVisibility(View.INVISIBLE);
                    mainList.setVisibility(View.VISIBLE);

                    //new webTask(vtc).execute(site);
                    //call other method here

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
            try {
                Calendar cal = Calendar.getInstance();

                Date now = new Date();
                CharSequence timeString;
                timeString = DateUtils.getRelativeDateTimeString(this, prefs.getLong("date", now.getTime()), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);

                textview.setText("Last Updated " + timeString);

            } catch (Exception ex) {

            }
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
        ListView listview = (ListView) findViewById(R.id.listView1);

        //TextView header = new TextView(this);
        //header.setHeight(100);
        //header.setText("Keertan Programs from isangat.org");
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.listheader, listview, false);
        listview.addHeaderView(header);


       // adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,temp);
        cadapter = new CustomArrayAdapter(this, R.layout.simplelistitem, Programs);
        listview.setAdapter(cadapter);



        //If there is internet, update/download the page
       if (wifi.isConnected()) {
            Toast.makeText(MainActivity.this, "Loading data from Web", Toast.LENGTH_SHORT).show();
            new webTask(this).execute(site);
            new jsonTask(this).execute();

       }
        //otherwise just load the page from file
        else {
            Toast.makeText(MainActivity.this, "Loading Page from File", Toast.LENGTH_SHORT).show();
            loadPage();

            if (prefs.contains("json")) {
                PutJSON(prefs.getString("json", new JSONObject().toString()));
            }


        }
    }

    public void PutJSON(String jsonStr){
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");

        try{
            //JSONObject obj = new JSONObject(jsonStr);
            //JSONArray programs  = obj.getJSONArray("programs");

            JSONArray programs  = new JSONArray(jsonStr);
            cadapter.clear();
            for(int i = 0; i < programs.length();i++) {
                JSONObject program1 = programs.getJSONObject(i);
                program temp_prog = new program();
                try {
                    temp_prog.id = program1.getInt("id");
                    temp_prog.title = program1.getString("title");
                    temp_prog.subtitle = program1.getString("subtitle");
                    temp_prog.address = program1.getString("address");
                    temp_prog.phone = program1.getString("phone");
                    temp_prog.startDate = sdf.parse(program1.getString("sd"));
                    temp_prog.endDate = sdf.parse(program1.getString("ed"));
                    temp_prog.source = program1.getString("source");
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
                cadapter.add(temp_prog);
            }
            cadapter.filter("akj");
            //cadapter.getFilter().filter("vsk");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * called when the update page button is clicked
     * @param v the View which triggered the method call: should refer to the button "enter"
     */
    public void enter(View v) {
       Toast.makeText(MainActivity.this, "Loading Data from Web", Toast.LENGTH_SHORT).show();
        new jsonTask(this).execute();
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


            webview1.loadDataWithBaseURL("file:///isangatTemp.html", temp, "text/html", "UTF-8", null);
        } catch (FileNotFoundException ex) {
            Toast.makeText(getBaseContext(), "File not found, attempting to load from Web.", Toast.LENGTH_LONG).show();
            new webTask(this).execute("");
        } catch (Exception ex) {
            Toast.makeText(getBaseContext(), "Could not load file" + ex.getMessage(), Toast.LENGTH_LONG).show();

        }
    }

    static class  ViewHolder{
        TextView title;
        TextView subtitle;
    }
    public class program {

        program(){
        }
        public int id = 0;
        public Date startDate = new Date();
        public Date endDate = new Date();
        public String title = "";
        public String subtitle = "";
        public String address = "";
        public String phone = "";
        public String source = "";
        public String description = "";
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
                ret = "ERROR:" + exception.getMessage();
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
                editor.putLong("date", now.getTime());
                editor.commit();

                textview.setText("Last Updated " + timeString);
                loadPage();
            } else {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class jsonTask extends AsyncTask<String, Integer, String> {

        private Context mContext;
        public jsonTask (Context context) {
            mContext = context;
        }

        protected String doInBackground(String... temp) {
            String ret = "";
            try {
                URL url = null;
                 url = new URL("http://www.vikramkhalsa.com/kirtanapp/getprograms.php");
                //url = new URL("http://www.isangat.org/json.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //set up some things on the connection
                //urlConnection.setRequestProperty("User-Agent", USERAGENT);  //if you are not sure of user agent just set choice=0
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                StringBuilder builder = new StringBuilder();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                SharedPreferences prefs = getSharedPreferences("DATE_PREF", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("json", builder.toString());
                editor.commit();

                ret= builder.toString();

            }  catch (MalformedURLException e) {
                ret = "ERROR:" + e.getMessage();
                e.printStackTrace();
            } catch (ProtocolException e) {
                ret = "ERROR:" + e.getMessage();
                e.printStackTrace();
            } catch (IOException e){
            ret = "ERROR:" + e.getMessage();
                e.printStackTrace();
            } finally {
                return ret;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {

            if (result.contains("ERROR")) {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
            else {
                PutJSON(result);
            }
        }
    }

    private class CustomArrayAdapter extends ArrayAdapter<program>
    {


        private ArrayList<program> list;
        Context context;
        int resource;

        public void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            ArrayList<program> tempList = (ArrayList<program>)Programs.clone();
            Programs.clear();
            if (charText.length() == 0) {
                Programs.addAll(tempList);
            }
            else
            {
                for (program p : tempList)
                {
                    if (p.source.contains(charText))
                    {
                        Programs.add(p);
                    }
                }
            }
            notifyDataSetChanged();
        }
        public CustomArrayAdapter(Context context, int textViewResourceId, ArrayList<program> programs){
            super(context, textViewResourceId, programs);
            this.list = new ArrayList<program>();
            this.list.addAll(programs);
            this.resource = textViewResourceId;
            this.context = context;

        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            View programView = convertView;

            if(convertView == null) {

                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                programView = vi.inflate(resource, null);
            }

            program pg = getItem(position);
            if (pg!= null) {

                //VewHolder holder = new ViewHolder();i
                //LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //convertView = inflater.inflate(R.layout.simplelistitem, null);
                TextView title = (TextView) programView.findViewById((R.id.title));
                TextView subtitle = (TextView) programView.findViewById(((R.id.subtitle)));
                TextView date = (TextView) programView.findViewById((R.id.date));
                TextView time = (TextView) programView.findViewById((R.id.time));
                TextView address = (TextView) programView.findViewById(R.id.address);
                TextView phone = (TextView) programView.findViewById(R.id.phone);
                TextView source = (TextView) programView.findViewById(R.id.source);

                address.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new
                                Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/?q=" + ((TextView)v).getText()));
                        startActivity(i);
                    }
                });

                phone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:"+ ((TextView)v).getText()));
                        startActivity(callIntent);
                    }
                });

                title.setText(pg.title);
                subtitle.setText((pg.subtitle));
                address.setText((pg.address));
                phone.setText(pg.phone);

                DateFormat df = new DateFormat();
                date.setText(df.format("EEE, MMM dd", pg.startDate));
               time.setText(df.format("hh:mma", pg.startDate) + " to " + df.format("hh:mma", pg.endDate));
                source.setText(pg.source);
            }
            return programView;

        }
    }


}


