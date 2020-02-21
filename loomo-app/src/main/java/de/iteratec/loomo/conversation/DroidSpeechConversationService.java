package de.iteratec.loomo.conversation;

import android.content.Context;
import android.util.Log;
import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;
import de.iteratec.loomo.interaction.SpeakService;
import de.iteratec.loomo.state.Event;
import de.iteratec.loomo.state.StateService;

import java.util.List;

public class DroidSpeechConversationService implements OnDSListener {

    private static final String LOG_TAG = "DroidSpeechConversation";

    private static final String VOICE_COMMAND_FOLLOW_ME = "follow me";
    private static final String VOICE_COMMAND_WAIT = "wait";
    private static final String VOICE_COMMAND_START = "start";
    private static final String VOICE_COMMAND_STOP = "stop";
    private static final String VOICE_COMMAND_RESET = "reset";
    private static final String VOICE_COMMAND_CONTINUE = "continue";

    private DroidSpeech droidSpeech;

    private static DroidSpeechConversationService instance;

    public static synchronized DroidSpeechConversationService getInstance() {
        if (instance == null) {
            instance = new DroidSpeechConversationService();
        }
        return instance;
    }

    public void init(Context context) {
        droidSpeech = new DroidSpeech(context, null);
        droidSpeech.setOfflineSpeechRecognition(true);
        droidSpeech.setOnDroidSpeechListener(this);
    }

    public void tearDown(){
    }

    public void startConversation() {
        droidSpeech.startDroidSpeechRecognition();
    }

    public void stopConversation() {
        droidSpeech.closeDroidSpeechOperations();
    }


    @Override
    public void onDroidSpeechSupportedLanguages(String currentSpeechLanguage, List<String> supportedSpeechLanguages) {
        Log.i(LOG_TAG, "onDroidSpeechSupportedLanguages: " + currentSpeechLanguage);
    }

    @Override
    public void onDroidSpeechRmsChanged(float rmsChangedValue) {
        // Log.i(LOG_TAG, "onDroidSpeechRmsChanged: " + rmsChangedValue);
    }

    @Override
    public void onDroidSpeechLiveResult(String liveSpeechResult) {
         Log.i(LOG_TAG, "onDroidSpeechLiveResult: " + liveSpeechResult);
    }

    @Override
    public void onDroidSpeechFinalResult(String finalSpeechResult) {
        Log.i(LOG_TAG, "onDroidSpeechFinalResult: " + finalSpeechResult);
        StateService state = StateService.getInstance();
        final String cleanedSpeechResult = finalSpeechResult.toLowerCase().trim();
        if(contains(cleanedSpeechResult, "lohnmost","loomo", "lomo", "lumo", "domo", "logo", "lovoo", "Shlomo", "yomo", "Lohmar", "Norma", "blume", "blue" ,"nummer","Lovato")){
            if (contains(cleanedSpeechResult,"start","dart") || (contains(cleanedSpeechResult,"bring") && (contains(cleanedSpeechResult, "pepper", "peppa", "papa")))){
                Log.i(LOG_TAG, "YYYYYEEAAAAHHHHHHHHHHHHH");
                state.triggerEvent(Event.VOICE_COMMAND_START);
            }
            if(contains(cleanedSpeechResult, "stop", "halt")){
                state.triggerEvent(Event.VOICE_COMMAND_STOP);
            }
            if (contains(cleanedSpeechResult,"warte","water")){
                Log.i(LOG_TAG, "YYYYYEEAAAAHHHHHHHHHHHHH");
                state.triggerEvent(Event.VOICE_COMMAND_WAIT);
            }
            if (cleanedSpeechResult.contains("weiter")){
                Log.i(LOG_TAG, "YYYYYEEAAAAHHHHHHHHHHHHH");
                state.triggerEvent(Event.VOICE_COMMAND_CONTINUE);
            }
            if (cleanedSpeechResult.contains("reset")){
                Log.i(LOG_TAG, "YYYYYEEAAAAHHHHHHHHHHHHH");
                state.triggerEvent(Event.VOICE_COMMAND_RESET);
            }
            if (cleanedSpeechResult.contains("folge")){
                Log.i(LOG_TAG, "YYYYYEEAAAAHHHHHHHHHHHHH");
                state.triggerEvent(Event.VOICE_COMMAND_FOLLOW_ME);
            }
            if (cleanedSpeechResult.contains("reset")){
                Log.i(LOG_TAG, "YYYYYEEAAAAHHHHHHHHHHHHH");
                state.triggerEvent(Event.VOICE_COMMAND_RESET);
            }
            if (cleanedSpeechResult.contains("Fahrstuhl")){
                Log.i(LOG_TAG, "YYYYYEEAAAAHHHHHHHHHHHHH");
                state.triggerEvent(Event.VOICE_COMMAND_ELEVATOR);
            }
            if(containsAnd(cleanedSpeechResult, "wie", "läuft")){
                SpeakService.getInstance().say("Alles super hier unten");
            }
            if (contains(cleanedSpeechResult, "Köln", "Geissböcke", "FC") &&  contains(cleanedSpeechResult, "gespielt", "ausgegangen")){
                SpeakService.getInstance().say("Der FC hat mal wieder gewonnen, natürlich.");
            }
        }
    }

    private boolean containsAnd(String cleanedSpeechResult, String ... containStrings) {
        for(String contain : containStrings){
            if(!cleanedSpeechResult.contains(contain)){
                return false;
            }
        }
        return true;
    }

    private boolean contains(String cleanedSpeechResult, String ... containStrings) {
        for(String contain : containStrings){
            if(cleanedSpeechResult.contains(contain)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDroidSpeechClosedByUser() {
        Log.i(LOG_TAG, "onDroidSpeechClosedByUser");
    }

    @Override
    public void onDroidSpeechError(String errorMsg) {
        Log.i(LOG_TAG, "onDroidSpeechError: " + errorMsg);
    }
}
