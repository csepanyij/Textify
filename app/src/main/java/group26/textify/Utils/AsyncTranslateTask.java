package group26.textify.Utils;

import android.os.AsyncTask;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import group26.textify.Activities.ScanActivity;

public class AsyncTranslateTask extends AsyncTask<String, Void, String> {
    private ScanActivity listener;
    private final String APIkey = "AIzaSyBAmxUB45CM2vlbnu0PU4s5SUKggxQypis";

    public AsyncTranslateTask (ScanActivity listener){
        this.listener=listener;
    }

    protected String doInBackground(String... params) {
        String textToTranslate = params[0];
        String inputLang = params[1];
        String outputLang = params[2];

        Translate translate = TranslateOptions.newBuilder().setProjectId("textify-197415").setApiKey(APIkey).build().getService();
        Translate.TranslateOption srcLang = Translate.TranslateOption.sourceLanguage(inputLang);
        Translate.TranslateOption tgtLang = Translate.TranslateOption.targetLanguage(outputLang);

        Translate.TranslateOption model = Translate.TranslateOption.model("nmt");
        Translation translation = translate.translate(textToTranslate, srcLang, tgtLang, model);
        return translation.getTranslatedText();
    }

    protected void onPostExecute(String response){
        listener.onCallbackTranslate(response);
    }
}