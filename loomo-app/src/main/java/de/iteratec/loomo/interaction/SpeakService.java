package de.iteratec.loomo.interaction;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;


public class SpeakService implements TextToSpeech.OnInitListener {

    private static final String TAG = "SpeakService";

    private static final String TEXT_WELCOME = "Hallo, ich bin Lohmo. Wie kann ich dir helfen?";
    private static final String ERROR_MESSAGE = "Es ist ein Problem aufgetaucht";
    private static final String TEXT_NAV_INIT = "Ich bereite gerade die heutige Tour vor!";

    private static SpeakService instance;

    private TextToSpeech tts;

    public static synchronized SpeakService getInstance() {
        if (instance == null) {
            instance = new SpeakService();
        }
        return instance;
    }

    public void init(Context context) {
        tts = new TextToSpeech(context, instance);
    }

    public void say(String text) {
        say(text, null);
    }

    public void say(String text, UtteranceProgressListener listener) {
        say(text, listener, "loomo");
    }

    public void say(String text, UtteranceProgressListener listener, String utteranceId) {
        say(text, listener, utteranceId, Locale.GERMAN);
    }

    public void sayHello() {
        this.say(TEXT_WELCOME);
    }

    public void say(String text, UtteranceProgressListener listener, String utteranceId, Locale locale) {
        if (listener != null) {
            tts.setOnUtteranceProgressListener(listener);
        }
        if (text != null) {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "loomo");
        } else {
            tts.speak(ERROR_MESSAGE, TextToSpeech.QUEUE_ADD, null, "loomo");
        }
    }

    @Override
    public void onInit(int status) {
        Log.i(TAG, "Initialized TTS with status: " + status);
        sayHello();
    }

    public void stop() {
        if (tts != null) {
            tts.stop();
        }
        tts = null;
    }
}
