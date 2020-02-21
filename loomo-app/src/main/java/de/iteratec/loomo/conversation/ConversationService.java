package de.iteratec.loomo.conversation;

import android.content.Context;
import android.util.Log;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.grammar.GrammarConstraint;
import com.segway.robot.sdk.voice.grammar.Slot;
import com.segway.robot.sdk.voice.recognition.RecognitionListener;
import com.segway.robot.sdk.voice.recognition.RecognitionResult;
import com.segway.robot.sdk.voice.recognition.WakeupListener;
import com.segway.robot.sdk.voice.recognition.WakeupResult;
import de.iteratec.loomo.state.Event;
import de.iteratec.loomo.state.StateService;

public class ConversationService {

    private static final String LOG_TAG = "ConversationService";

    private static final String VOICE_COMMAND_FOLLOW_ME = "follow me";
    private static final String VOICE_COMMAND_WAIT = "wait";
    private static final String VOICE_COMMAND_START = "start";
    private static final String VOICE_COMMAND_STOP = "stop";
    private static final String VOICE_COMMAND_RESET = "reset";
    private static final String VOICE_COMMAND_CONTINUE = "continue";
    private static final String VOICE_COMMAND_ELEVATOR = "elevator";

    private boolean shouldStart = false;
    private boolean started = false;

    private Recognizer mRecognizer;
    private GrammarConstraint basicGrammar;
    private ServiceBinder.BindStateListener mRecognitionBindStateListener;

    private WakeupListener mWakeupListener;
    private RecognitionListener mRecognitionListener;

    private static ConversationService instance;

    public static synchronized ConversationService getInstance() {
        if (instance == null) {
            instance = new ConversationService();
        }
        return instance;
    }

    public void init(Context context) {
        initControlGrammar();
        mRecognizer = Recognizer.getInstance();
        initListeners();

        mRecognizer.bindService(context, mRecognitionBindStateListener);
    }

    public void tearDown() {
        mRecognizer.unbindService();
    }

    public void startConversation() {
        if (started) {
            try {
                mRecognizer.startWakeupAndRecognition(mWakeupListener, mRecognitionListener);
                Log.i(LOG_TAG, "Starting voice recognistion");
            } catch (VoiceException e) {
                Log.w(LOG_TAG, "Problem starting voice recognition: " + e.getMessage());
            }
        } else {
            shouldStart = true;
        }
    }

    public void stopConversation() {
        try {
            mRecognizer.stopRecognition();
        } catch (VoiceException e) {
            Log.w(LOG_TAG, "Problem stopping voice recognition: " + e.getMessage());
        }
    }

    private void initControlGrammar() {
        basicGrammar = new GrammarConstraint();
        basicGrammar.setName("bases orders");

        Slot commandSlot = new Slot("commands");
        commandSlot.setOptional(false);
        commandSlot.addWord(VOICE_COMMAND_FOLLOW_ME);
        commandSlot.addWord(VOICE_COMMAND_START);
        commandSlot.addWord(VOICE_COMMAND_STOP);
        commandSlot.addWord(VOICE_COMMAND_WAIT);
        commandSlot.addWord(VOICE_COMMAND_RESET);
        commandSlot.addWord(VOICE_COMMAND_CONTINUE);
        commandSlot.addWord(VOICE_COMMAND_ELEVATOR);
        basicGrammar.addSlot(commandSlot);
    }

    private void initListeners() {
        mRecognitionBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(LOG_TAG, "Recognition onBind");
                try {
                    int mRecognitionLanguage = mRecognizer.getLanguage();
                    Log.i(LOG_TAG, "Starting with language: " + ((mRecognitionLanguage == 0) ? "English" : "Chinese"));
                    mRecognizer.addGrammarConstraint(basicGrammar);

                    started = true;
                    if (shouldStart) {
                        startConversation();
                    }
                } catch (VoiceException e) {
                    Log.w(LOG_TAG, "Problem adding grammar: " + e.getMessage());
                }
            }

            @Override
            public void onUnbind(String s) {
                Log.i(LOG_TAG, "unbind");
                started = false;
            }
        };

        mWakeupListener = new WakeupListener() {
            @Override
            public void onStandby() {
                Log.d(LOG_TAG, "onStandby");
//                Message msg = mHandler.obtainMessage(ACTION_SHOW_MSG, "You can say \"Ok Loomo\" \n or touch the screen to wake up Loomo");
//                mHandler.sendMessage(msg);
            }

            @Override
            public void onWakeupResult(WakeupResult wakeupResult) {
                Log.d(LOG_TAG, "wakeup word:" + wakeupResult.getResult() + ", angle: " + wakeupResult.getAngle());
            }

            @Override
            public void onWakeupError(String s) {
                Log.d(LOG_TAG, "onWakeupError:" + s);
//                Message msg = mHandler.obtainMessage(ACTION_SHOW_MSG, "wakeup error:" + s);
//                mHandler.sendMessage(msg);
            }
        };

        mRecognitionListener = new RecognitionListener() {
            @Override
            public void onRecognitionStart() {
                Log.d(LOG_TAG, "onRecognitionStart");
                // TODO: "Loomo begin to recognize, say:\n follow, start, stop, wait")

            }

            @Override
            public boolean onRecognitionResult(RecognitionResult recognitionResult) {
                //show the recognition result and recognition result confidence.
                String result = recognitionResult.getRecognitionResult();
                Log.i(LOG_TAG, "recognition result: " + result + ", confidence:" + recognitionResult.getConfidence());
                StateService state = StateService.getInstance();
                if (result.contains(VOICE_COMMAND_FOLLOW_ME)) {
                    state.triggerEvent(Event.VOICE_COMMAND_FOLLOW_ME);
                } else if (result.contains(VOICE_COMMAND_START)) {
                    state.triggerEvent(Event.VOICE_COMMAND_START);
                } else if (result.contains(VOICE_COMMAND_WAIT)) {
                    state.triggerEvent(Event.VOICE_COMMAND_WAIT);
                } else if (result.contains(VOICE_COMMAND_STOP)) {
                    state.triggerEvent(Event.VOICE_COMMAND_STOP);
                } else if (result.contains(VOICE_COMMAND_RESET)) {
                    state.triggerEvent(Event.VOICE_COMMAND_RESET);
                } else if (result.contains(VOICE_COMMAND_CONTINUE)) {
                    state.triggerEvent(Event.VOICE_COMMAND_CONTINUE);
                } else if(result.contains(VOICE_COMMAND_ELEVATOR)){
                    state.triggerEvent(Event.VOICE_COMMAND_ELEVATOR);
                }
                return false;
            }

            @Override
            public boolean onRecognitionError(String s) {
                Log.w(LOG_TAG, "onRecognitionError: " + s);
                return false;
            }
        };
    }
}
