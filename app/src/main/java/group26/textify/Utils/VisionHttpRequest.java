package group26.textify.Utils;

import android.os.AsyncTask;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import group26.textify.Activities.ScanActivity;

public class VisionHttpRequest extends AsyncTask<String, Void, String> {

    private static final String TARGET_URL = "https://vision.googleapis.com/v1/images:annotate?";
    private static final String API_KEY = "key=AIzaSyBAmxUB45CM2vlbnu0PU4s5SUKggxQypis";

    private ScanActivity listener;

    public VisionHttpRequest(ScanActivity listener){
        this.listener=listener;
    }

    protected String doInBackground(String... params) {
        String urlParameters = params[0];
        String resp = "";
        try{
            // Creating the connection for sending the request
            URL serverUrl = new URL(TARGET_URL + API_KEY);
            URLConnection urlConnection = serverUrl.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection)urlConnection;

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setDoOutput(true);

            BufferedWriter httpRequestBodyWriter = new BufferedWriter(new
                    OutputStreamWriter(httpURLConnection.getOutputStream()));
            httpRequestBodyWriter.write(urlParameters);
            httpRequestBodyWriter.close();

            // Getting the response from the API
            if (httpURLConnection.getInputStream() == null) {
                System.out.println("No stream");
                return "No stream";
            }

            Scanner httpResponseScanner = new Scanner (httpURLConnection.getInputStream());
            while (httpResponseScanner.hasNext()) {
                String line = httpResponseScanner.nextLine();
                System.out.println(line);
                if (line.contains("description")) {
                    resp = line.substring(line.indexOf("description")+"description".length()+4,line.length()-2);
                    System.out.println(resp);
                    break;
                }
            }
            httpResponseScanner.close();

            if (resp.equals("")) {
                resp = "(No text was recognised)";
            }

            resp = resp.replace("\\n", "\n");

            System.out.println("onPostExecute : " + resp);

        }catch(Exception e){
            System.err.println("onClickSendRequestButton : " + e.toString());
            e.printStackTrace();
            resp = resp + e.toString();
        }
        return resp;
    }

    protected void onPostExecute(String response){
        listener.onCallbackVision(response);
    }
}