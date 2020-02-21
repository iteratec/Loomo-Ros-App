package de.iteratec.loomo.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.emoji.Emoji;
import com.segway.robot.sdk.emoji.EmojiPlayListener;
import com.segway.robot.sdk.emoji.EmojiView;
import com.segway.robot.sdk.emoji.HeadControlHandler;
import com.segway.robot.sdk.emoji.configure.BehaviorList;
import com.segway.robot.sdk.emoji.exception.EmojiException;
import com.segway.robot.sdk.emoji.player.RobotAnimator;
import com.segway.robot.sdk.emoji.player.RobotAnimatorFactory;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;

import de.iteratec.loomo.LoomoApplication;
import de.iteratec.loomo.R;
import de.iteratec.loomo.conversation.DroidSpeechConversationService;
import de.iteratec.loomo.interaction.HeadLightService;
import de.iteratec.loomo.interaction.SpeakService;
import de.iteratec.loomo.location.Floor;
import de.iteratec.loomo.location.Location;
import de.iteratec.loomo.location.LocationService;
import de.iteratec.loomo.navigation.Destination;
import de.iteratec.loomo.navigation.NavigationService;
import de.iteratec.loomo.ros.RosService;
import de.iteratec.loomo.state.Event;
import de.iteratec.loomo.state.StateService;

import de.iteratec.loomo.util.StateObserver;

import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.net.URISyntaxException;


public class RosDeveloperActivity extends org.ros.android.RosActivity implements View.OnClickListener, View.OnLongClickListener{

    private static final String TAG = "RosDeveloperActivity";

    private static final int ACTION_BEHAVE = 4;

    private EmojiView mEmojiView;
    private Emoji mEmoji;
    private HeadControlManager mHandcontrolManager;
    private TextView activeState;
    private String item;
    private LocationService locationService;

    private StateObserver stateObserver;

    private Head mHead;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ACTION_BEHAVE:

                    emojiAnimation((Integer) msg.obj);

                    break;
                default:
                    break;
            }
        }
    };

    private RosService rosService;

    public RosDeveloperActivity() throws URISyntaxException {
        //when the constructor is given a master URI as 3rd argument one will directly connect to the master without having to enter the IP
        //super("RosDeveloperActivity", "RosDeveloperActivity", new URI("http://192.168.1.139:11311"));
        super("RosDeveloperActivity", "RosDeveloperActivity", new URI("http://192.168.106.75:11311"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ros);
        mEmojiView = (EmojiView) findViewById(R.id.face);
        mEmojiView.setOnClickListener(this);
        mEmojiView.setOnLongClickListener(this);
        initEmoji();
        stateObserver= new StateObserver(this);
        StateService.getInstance().addObserver(stateObserver);
        locationService = LocationService.getInstance();
        this.rosService = new RosService(this);
        rosService.setInstance(this.rosService);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called");
        this.rosService.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.rosService.stop();
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        Log.d(TAG, "init: called");
        this.rosService.init(nodeMainExecutor, getMasterUri(), getRosHostname());
    }



    private void setAdminView() {
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(RosDeveloperActivity.this);
        View myView = getLayoutInflater().inflate(R.layout.activity_admin, null);
        myBuilder.setTitle("Admin");
        final Spinner mySpinner = myView.findViewById(R.id.position);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(RosDeveloperActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray((R.array.positions)));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mySpinner.setAdapter(adapter);

        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                item = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setButtons(myBuilder);

        myBuilder.setView(myView);
        AlertDialog dialog = myBuilder.create();
        dialog.show();
    }

    private void setButtons(AlertDialog.Builder Builder) {
        Builder.setPositiveButton("Bestätigen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextView tv = findViewById(R.id.welcome);
                String hello;
                if (item.equals("MUC")) {
                    locationService.setLocation(Location.MUC);
                    hello = locationService.getHelloText();
                    tv.setText(hello);

                } else if (item.equals("MUC_OG")) {
                locationService.setLocation(Location.MUC_OG);
                hello = locationService.getHelloText();
                tv.setText(hello);
                } else if (item.equalsIgnoreCase("Standort auswählen")) {
                    tv.setText("Hallo Loomo");
                } else if (item.equalsIgnoreCase("resetOdom")) {
                    rosService.resetOdom();

                } else if (item.equalsIgnoreCase("raw_mode")) {
                    Base.getInstance().setControlMode(Base.CONTROL_MODE_RAW);
                } else if (item.equalsIgnoreCase("nav_mode")) {
                    Base.getInstance().setControlMode(Base.CONTROL_MODE_NAVIGATION);
                } else if (item.equalsIgnoreCase("init_navigation")) {
                    StateService state = StateService.getInstance();
                    state.triggerEvent(Event.VOICE_COMMAND_RESET);
                }
                else if(item.equalsIgnoreCase("clear_costmap")){
                    rosService.clearCostmap();
                }else if(item.equalsIgnoreCase("start_navigation")){
                    StateService state = StateService.getInstance();
                    state.triggerEvent(Event.VOICE_COMMAND_START);
                }
                else if(item.equalsIgnoreCase("goal_reached")){
                    StateService state = StateService.getInstance();
                    state.triggerEvent(Event.DESTINATION_ARRIVED);
                }
                else if(item.equalsIgnoreCase("continue_navigation")){
                    StateService state = StateService.getInstance();
                    state.triggerEvent(Event.VOICE_COMMAND_CONTINUE);
                }else if(item.equalsIgnoreCase("elevator")){
                    StateService state = StateService.getInstance();
                    state.triggerEvent(Event.VOICE_COMMAND_ELEVATOR);
                }
                else if(item.equalsIgnoreCase("back_to_start")){
                    NavigationService.getInstance().startNavigation(Destination.INIT_POS);
                }
                else if(item.equalsIgnoreCase("drivein2ElevatorROS")){
                    NavigationService.getInstance().startNavigation(Destination.INSIDE_ELEVATOR);
                }
                else if(item.equalsIgnoreCase("drive2DoorROS")){
                    NavigationService.getInstance().startNavigation(Destination.EG_DOOR);
                }
                else if(item.equalsIgnoreCase("drive2ElevatorROS")){
                    NavigationService.getInstance().startNavigation(Destination.EG_ELEVATOR);
                }
                else if(item.equalsIgnoreCase("changeParams")){
                    RosService.getInstance().changeParameters("image_offset", 100);
                }
                else if(item.equalsIgnoreCase("Pepper")){
                StateService state = StateService.getInstance();
                state.triggerEvent(Event.VOICE_COMMAND_START);
                }
                else if(item.equalsIgnoreCase("getoutofelevator")){
                    Log.d(TAG, "attempting to drive forward");
                    RosService.getInstance().drive(1,0);
                }



            }
        });
        Builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    private void initEmoji() {
        mEmoji = Emoji.getInstance();
        mEmoji.init(this);
        mEmoji.setEmojiView((EmojiView) findViewById(R.id.face));

        //emojiAnimation(BehaviorList.LOOK_AROUND);

        //mHandcontrolManager = new HeadControlManager(this);
        //mHandcontrolManager.setMode(HeadControlHandler.MODE_EMOJI);
        //mEmoji.setHeadControlHandler(mHandcontrolManager);
        mHead= Head.getInstance();
        mHead.bindService(LoomoApplication.getContext(), mHeadBindStateListener);

        emojiAnimation(BehaviorList.IDEA_BEHAVIOR_RANDOM);

        SpeakService.getInstance().sayHello();
    }

    private ServiceBinder.BindStateListener mHeadBindStateListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            Log.d(TAG, "onBind() called");
            mHead.setMode(HeadControlHandler.MODE_EMOJI);
            HeadLightService.getInstance().init(mHead);
        }

        @Override
        public void onUnbind(String reason) {
            Log.d(TAG, "onUnbind() called with: reason = [" + reason + "]");
        }
    };

    private void emojiAnimation(int behavior) {
        try {
            mEmoji.startAnimation(RobotAnimatorFactory.getReadyRobotAnimator(behavior), new EmojiPlayListener() {
                @Override
                public void onAnimationStart(RobotAnimator animator) {
                }

                @Override
                public void onAnimationEnd(RobotAnimator animator) {
                    mEmojiView.setClickable(true);
                    //mHead = Head.getInstance();
                    mHead.setWorldYaw(0);
                    mHead.setWorldPitch(0.6f);
                    //mHandcontrolManager.setWorldPitch(0.6f);


                    Message msg = optainBehaviorMessage();
                    mHandler.sendMessageDelayed(msg, (int) (Math.random() * 60 * 1000 + 1000));
                }

                @Override
                public void onAnimationCancel(RobotAnimator animator) {
                    mEmojiView.setClickable(true);
                    mHead = Head.getInstance();
                    mHead.setWorldYaw(0);
                    mHead.setWorldPitch(0.6f);
                    //mHandcontrolManager.setWorldPitch(0.6f);
                }
            });


        } catch (EmojiException e) {
            Log.w(TAG, "Problem with emoji", e);
        }
    }

    @Override
    public void onClick(final View v) {
        v.setClickable(false);
        StateService.getInstance().triggerEvent(Event.VOICE_COMMAND_CONTINUE);

        Message msg = optainBehaviorMessage();
        mHandler.sendMessage(msg);
        v.setClickable(true);

    }

    private Message optainBehaviorMessage() {
        int behavior;
        int randomSeed = (int) (Math.random() * 4);
        switch (randomSeed) {
            case 0:
                /*if (StateService.getInstance().getSm().getState()== UseCaseStateMachine.WAITING_ENTRANCE_HALL) {
                    behavior = BehaviorList.IDEA_BEHAVIOR_RANDOM;
                }else*/
                behavior=BehaviorList.APPLE_WOW_EMOTION;
                break;
            case 1:
                behavior = BehaviorList.AVATAR_BLINK_EMOTION;
                break;
            case 2:
                behavior = BehaviorList.AVATAR_CURIOUS_EMOTION;
                break;
            case 3:
                behavior = BehaviorList.APPLE_LIKE_EMOTION;
                break;
            default:
                behavior = BehaviorList.AVATAR_HELLO_EMOTION;
                break;
        }

        return mHandler.obtainMessage(ACTION_BEHAVE, behavior);
    }

    @Override
    public boolean onLongClick(View view) {
        DroidSpeechConversationService.getInstance().stopConversation();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DroidSpeechConversationService.getInstance().startConversation();
        setAdminView();
        return false;
    }

    public void updateActiveState(){
        Log.d(TAG, "setting new State" + StateService.getInstance().getSm().getState().toString());
        TextView aState= (TextView) findViewById(R.id.state);
        aState.setText(StateService.getInstance().getSm().getState().toString());
    }

}
