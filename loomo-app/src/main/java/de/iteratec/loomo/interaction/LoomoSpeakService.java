package de.iteratec.loomo.interaction;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.tts.TtsListener;

import java.util.Locale;


public class LoomoSpeakService implements TextToSpeech.OnInitListener {

    private static final String TAG = "SpeakService";

    private static final String TEXT_WELCOME = "Hallo, ich bin Lohmo. Wie kann ich dir helfen?";
    private static final String ERROR_MESSAGE = "Es ist ein Problem aufgetaucht";

    private static LoomoSpeakService instance;

    private ServiceBinder.BindStateListener mSpeakerBindStateListener;

    private Speaker speaker;
    private TtsListener mTtsListener;

    public static synchronized LoomoSpeakService getInstance() {
        if (instance == null) {
            instance = new LoomoSpeakService();
        }
        return instance;
    }

    public void init(Context context) {
        speaker = Speaker.getInstance();
        mTtsListener = new TtsListener() {
            @Override
            public void onSpeechStarted(String s) {
                //s is speech content, callback this method when speech is starting.
                Log.d(TAG, "onSpeechStarted() called with: s = [" + s + "]");
            }

            @Override
            public void onSpeechFinished(String s) {
                //s is speech content, callback this method when speech is finish.
                Log.d(TAG, "onSpeechFinished() called with: s = [" + s + "]");
            }

            @Override
            public void onSpeechError(String s, String s1) {
                //s is speech content, callback this method when speech occurs error.
                Log.d(TAG, "onSpeechError() called with: s = [" + s + "], s1 = [" + s1 + "]");
            }
        };

        mSpeakerBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "speaker service onBind");
                // bindSpeakerService = true;

                // set the volume of TTS
                try {
                    speaker.setVolume(50);
                } catch (VoiceException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onUnbind(String s) {
                Log.d(TAG, "speaker service onUnbind");
                //speaker service or recognition service unbind, disable function buttons.
            }
        };

        //bind the speaker service.
        speaker.bindService(context, mSpeakerBindStateListener);

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
        // say("hello world, I am a segway robot.");
        say(TEXT_WELCOME);
    }

    public void say(String text, UtteranceProgressListener listener, String utteranceId, Locale locale) {
        if (text != null) {
            try {
                speaker.speak(text, mTtsListener);
            } catch (VoiceException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onInit(int status) {
        Log.i(TAG, "Initialized TTS with status: " + status);
    }

    public void stop() {
        if (speaker != null) {
            speaker.unbindService();
            speaker = null;
        }
    }
}
