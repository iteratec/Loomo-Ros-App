package de.iteratec.loomo.state;

import android.util.Log;
import de.iteratec.loomo.navigation.Destination;
import de.iteratec.loomo.state.action.*;
import de.iteratec.loomo.util.StateObserver;
import io.reactivex.functions.BiConsumer;

public class UseCaseStateMachine {

    private StateObserver stateObserver;
    private static final String LOG_TAG = "StateMachine";
    public static StateMachine.State<UseCaseContext, Event> NAVINIT = new StateMachine.State<>("NAVINIT");
    public static StateMachine.State<UseCaseContext, Event> INIT = new StateMachine.State<>("INIT");
    public static StateMachine.State<UseCaseContext, Event> IDLE = new StateMachine.State<>("IDLE");
    public static StateMachine.State<UseCaseContext, Event> FOLLOW_ME = new StateMachine.State<>("FOLLOW_ME");
    public static StateMachine.State<UseCaseContext, Event> WAITING_ENTRANCE_HALL = new StateMachine.State<>("WAITING_ENTRANCE_HALL");
    public static StateMachine.State<UseCaseContext, Event> EG_DOOR = new StateMachine.State<>("EG_DOOR");
    public static StateMachine.State<UseCaseContext, Event> STARTING_TOUR = new StateMachine.State<>("STARTING_TOUR");
    public static StateMachine.State<UseCaseContext, Event> WAY_TO_ELVEVATOR = new StateMachine.State<>("WAY_TO_ELVEVATOR");
    public static StateMachine.State<UseCaseContext, Event> EG_ELEVATOR_WAITING_HALL = new StateMachine.State<>("EG_ELEVATOR_WAITING_HALL");
    public static StateMachine.State<UseCaseContext, Event> ELEVATOR = new StateMachine.State<>("ELEVATOR");
    public static StateMachine.State<UseCaseContext, Event> WAITING_AT_DOOR = new StateMachine.State<>("WAITING_AT_DOOR");
    public static StateMachine.State<UseCaseContext, Event> WAITING_AT_OGDOOR = new StateMachine.State<>("WAITING_AT_OGDOOR");
    public static StateMachine.State<UseCaseContext, Event> WAITING_AT_ELEVATOR = new StateMachine.State<>("WAITING_AT_ELEVATOR");

    public static StateMachine.State<UseCaseContext, Event> WAY_TO_SHOWROOM = new StateMachine.State<>("WAY_TO_SHOWROOM");
    public static StateMachine.State<UseCaseContext, Event> OG_DOOR = new StateMachine.State<>("OG_DOOR");
    public static StateMachine.State<UseCaseContext, Event> WAY_TO_OGDOOR = new StateMachine.State<>("WAY_TO_OGDOOR");
    public static StateMachine.State<UseCaseContext, Event> SHOWROOM = new StateMachine.State<>("SHOWROOM");
    public static StateMachine.State<UseCaseContext, Event> PARKING_POSITION = new StateMachine.State<>("PARKING_POSITION");


    public static StateMachine.State<UseCaseContext, Event> WAY_TO_PEPPER = new StateMachine.State<>("WAY_TO_PEPPER");
    public static StateMachine.State<UseCaseContext, Event> COMMUNICATE_WITH_PEPPER = new StateMachine.State<>("COMMUNICATE_WITH_PEPPER");
    public static StateMachine.State<UseCaseContext, Event> WAY_TO_PARKING_POSITION = new StateMachine.State<>("WAY_TO_PARKING_POSITION");
    public static StateMachine.State<UseCaseContext, Event> CONNECT_TO_WIFI = new StateMachine.State<>("CONNECT_TO_WIFI");
    public static StateMachine.State<UseCaseContext, Event> GET_OUT_ELEVATOR = new StateMachine.State<>("GET_OUT_ELEVATOR");


    // Holder for events valid for all states
    public static StateMachine.State<UseCaseContext, Event> GLOBAL = new StateMachine.State<>("GLOBAL");

    static {
        INIT
                .onEnter(new InitAction())
                .onExit(new DefaultAction("exit"))
                .transition(Event.VOICE_COMMAND_FOLLOW_ME, FOLLOW_ME)
                .transition(Event.VOICE_COMMAND_WAIT, NAVINIT);

        NAVINIT
                .onEnter(new InitNavigationAction("Ich bereite gerade die heutige tour vor"))
                .onExit(new DefaultAction("exit"))
                .transition(Event.FINISHED_NAV_INIT, WAITING_ENTRANCE_HALL);

        IDLE
                .onEnter(new DefaultAction("idle"))
                .onExit(new DefaultAction("exit"))
                .transition(Event.VOICE_COMMAND_FOLLOW_ME, FOLLOW_ME)
                .transition(Event.VOICE_COMMAND_WAIT, WAITING_ENTRANCE_HALL);

        FOLLOW_ME
                .onEnter(new EnterFollowMeAction())
                .onExit(new StopFollowMeAction())
                .transition(Event.VOICE_COMMAND_WAIT, WAITING_ENTRANCE_HALL);

        WAITING_ENTRANCE_HALL
                .onEnter(new SpeakingAction("Ich bin bereit"))
                .onExit(new DefaultAction("exit"))
                .transition(Event.VOICE_COMMAND_START, STARTING_TOUR)
                .transition(Event.VOICE_COMMAND_CONTINUE, STARTING_TOUR);

        /*STARTING_TOUR
                .onEnter(new EnterStartTourAction(Destination.EG_DOOR, "Ok, kommt. Ich bringe Euch zu Pepper. Er kann Euch mehr erzählen"))
                .onExit(new DefaultAction("exit"))
                .transition(Event.DESTINATION_ARRIVED, EG_DOOR);*/

        STARTING_TOUR
                .onEnter(new EnterStartTourAction(Destination.EG_DOOR, "Ok, kommt. Ich bringe euch zum Gauss"))
                .onExit(new DefaultAction("exit"))
                .transition(Event.DESTINATION_ARRIVED, IDLE);


        EG_DOOR
                .onEnter(new SpeakingAction("Ich habe mein Ziel erreicht, Bitte mach mir die Tür auf"))
                .onExit(new DefaultAction("exit"))
                .transition(Event.VOICE_COMMAND_START, WAY_TO_ELVEVATOR)
                .transition(Event.VOICE_COMMAND_CONTINUE, WAY_TO_ELVEVATOR);


        WAITING_AT_DOOR
            .onEnter(new SpeakingAction("Ich bin bereit"))
            .onExit(new DefaultAction("exit"))
            .transition(Event.VOICE_COMMAND_CONTINUE, WAY_TO_ELVEVATOR)
            .transition(Event.VOICE_COMMAND_START, WAY_TO_ELVEVATOR);


        WAY_TO_ELVEVATOR
                .onEnter(new EnterContinueNavigationAction(Destination.EG_ELEVATOR))
                .onExit(new DefaultAction("exit"))
                .transition(Event.DESTINATION_ARRIVED, EG_ELEVATOR_WAITING_HALL);

        EG_ELEVATOR_WAITING_HALL
                .onEnter(new EnterDistanceCheckAction("Bitte drücken"))
                .onExit(new EnterElevatorAction())
                .transition(Event.VOICE_COMMAND_CONTINUE, ELEVATOR)
                .transition(Event.VOICE_COMMAND_START, ELEVATOR);

        ELEVATOR.onEnter(new InitOGNavigationAction())
                .onExit(new SpeakingAction("Okay ich komme"))
                .transition(Event.VOICE_COMMAND_CONTINUE, GET_OUT_ELEVATOR)
                .transition(Event.VOICE_COMMAND_START, GET_OUT_ELEVATOR);


        GET_OUT_ELEVATOR
                .onEnter(new EnterStartTourAction(Destination.ELEVATOR_WAITING_HALL,"Achtung ich steige aus"))
                .onExit(new DefaultAction("exit"))
                .transition(Event.DESTINATION_ARRIVED, WAITING_AT_ELEVATOR)
                .transition(Event.MANUAL_ELEVATOR_ESC, OG_DOOR);

        WAITING_AT_ELEVATOR
                .onEnter(new SpeakingAction("Ich bin bereit"))
                .onExit(new DefaultAction("exit"))
                .transition(Event.VOICE_COMMAND_START, WAY_TO_OGDOOR)
                .transition(Event.VOICE_COMMAND_CONTINUE, WAY_TO_OGDOOR);


        WAY_TO_OGDOOR
                .onEnter(new EnterStartTourAction(Destination.OG_DOOR,""))
                .onExit(new DefaultAction("exit"))
                .transition(Event.DESTINATION_ARRIVED, OG_DOOR);


        OG_DOOR
                .onEnter(new SpeakingAction("Ich habe mein Ziel erreicht, Bitte mach mir die Tür auf"))
                .onExit(new DefaultAction("exit"))
                .transition(Event.VOICE_COMMAND_START, WAY_TO_SHOWROOM)
                .transition(Event.VOICE_COMMAND_CONTINUE, WAY_TO_SHOWROOM)

            .transition(Event.VOICE_COMMAND_FOLLOW_ME, FOLLOW_ME);

        WAITING_AT_OGDOOR
                .onEnter(new SpeakingAction("Ich bin bereit"))
                .onExit(new DefaultAction("exit"))
                .transition(Event.VOICE_COMMAND_START, WAY_TO_PEPPER)
                .transition(Event.VOICE_COMMAND_CONTINUE, WAY_TO_PEPPER);


        WAY_TO_SHOWROOM
                .onEnter(new EnterStartTourAction(Destination.SHOWROOM, "kommt, gleich sind wir bei pepper!"))
                .onExit(new DefaultAction("exit"))
                .transition(Event.DESTINATION_ARRIVED, SHOWROOM);

        SHOWROOM
                .onEnter(new SpeakingAction("Ich habe mein Ziel erreicht, Bitte mach mir die Tür auf!"))
                .onExit(new DefaultAction("exit"))
                .transition(Event.VOICE_COMMAND_START, WAY_TO_PEPPER)
                .transition(Event.VOICE_COMMAND_CONTINUE, WAY_TO_PEPPER);

        WAY_TO_PEPPER
                .onEnter(new EnterContinueNavigationAction(Destination.SHOWROOM))
                .onExit(new DefaultAction("exit"))
                .transition(Event.DESTINATION_ARRIVED, CONNECT_TO_WIFI);

        CONNECT_TO_WIFI
                .onEnter(new ConnectToWifiAction())
                .onExit(new DefaultAction("exit"))
                .transition(Event.FINISHED_WIFI_CONNECTION, COMMUNICATE_WITH_PEPPER);

        COMMUNICATE_WITH_PEPPER
                .onEnter(new CommunicateWithPepperAction("erkläre uns den Peiltisch"))
                .onExit(new DefaultAction("exit"))
                .transition(Event.VOICE_COMMAND_CONTINUE, WAY_TO_PARKING_POSITION)
                .transition(Event.VOICE_COMMAND_START, WAY_TO_PARKING_POSITION);


        WAY_TO_PARKING_POSITION
                .onEnter(new EnterContinueNavigationAction(Destination.EG_DOOR))
                .onExit(new DefaultAction("exit"))
                .transition(Event.DESTINATION_ARRIVED, PARKING_POSITION);

        PARKING_POSITION
                .onEnter(new DefaultAction("enter"))
                .onExit(new DefaultAction("exit"));

        GLOBAL.onEnter(new ErrorAction()).onExit(new DefaultAction("exit"))
                .transition(Event.VOICE_COMMAND_STOP, IDLE)
                .transition(Event.VOICE_COMMAND_ELEVATOR, EG_ELEVATOR_WAITING_HALL)
                .transition(Event.VOICE_COMMAND_RESET, NAVINIT)
                .transition(Event.VOICE_COMMAND_FOLLOW_ME, FOLLOW_ME);

    }

    public static BiConsumer<UseCaseContext, StateMachine.State<UseCaseContext, Event>> log(final String text) {
        StateService.getInstance().updateState();
        Log.d(LOG_TAG, "notifying observer");
        return new BiConsumer<UseCaseContext, StateMachine.State<UseCaseContext, Event>>() {
            @Override
            public void accept(UseCaseContext t1, StateMachine.State<UseCaseContext, Event> state) {
                Log.i(LOG_TAG, "" + t1 + ":" + state + ":" + text);
            }
        };
    }

}
