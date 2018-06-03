package group26.textify.Utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.common.collect.ImmutableList;

import java.util.List;

import group26.textify.Activities.ScanActivity;

public class AsyncLangDetectTask extends AsyncTask<String, Void, String> {
    private ScanActivity listener;
    private final String APIkey = "AIzaSyBAmxUB45CM2vlbnu0PU4s5SUKggxQypis";

    public AsyncLangDetectTask (ScanActivity listener){
        this.listener=listener;
    }

    protected String doInBackground(String... params) {
        try {
            String textToTranslate = params[0];
            String langDetected = "";
            Translate translate;
            // String defaultAPIKey = TranslateOptions.getDefaultApiKey();
            // translate = TranslateOptions.getDefaultInstance().getService();
            translate = TranslateOptions.newBuilder().setProjectId("textify-197415").setApiKey(APIkey).build().getService();

            List<Detection> detections = translate.detect(ImmutableList.of(textToTranslate));
            for (Detection detection : detections) {
                langDetected = detection.getLanguage();
            }
            return langDetected;
        } catch (Exception e) {
            Log.d("AsyncLangDetectClass: ", "ERROR: ", e);
            return null;
        }

    }

    protected void onPostExecute(String response){
        listener.onCallbackLangDetect(response);
    }
}