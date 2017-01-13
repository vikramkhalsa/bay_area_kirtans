package com.vikramkhalsa.isangat;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by vikra on 1/11/2017.
 */

class JSONGetter  extends AsyncTask<String, Void, String> {

    // Required initialization

    protected String doInBackground(String... temp) {
        String ret = "";
        try {
            URL url = null;
            url = new URL(temp[0]);
            //url = new URL("http://www.vikramkhalsa.com/kirtanapp/getprograms.php");
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


    protected void onPostExecute(String result) {

        if (result.contains("ERROR")) {
           // Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
        }
        else {
            result = result.substring(1,result.length()-1);
            result = result.replace("\"", "");
            for (String res:result.split(",")) {
                //MainActivity.locationAdapter.add(res);
                MainActivity.list.add(res);
            }

        }
    }

}