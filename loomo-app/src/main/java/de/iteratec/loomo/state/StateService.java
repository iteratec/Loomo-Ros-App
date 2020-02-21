package de.iteratec.loomo.state;

import android.util.Log;

import java.util.Observable;

public class StateService extends Observable {

    private static final String LOG_TAG = "StateService";

    private static StateService instance;

    private StateMachine<UseCaseContext, Event> sm;

    public static synchronized StateService getInstance() {
        if (instance == null) {
            instance = new StateService();
        }

        return instance;
    }

    public StateMachine<UseCaseContext, Event> getSm() {
        return sm;
    }

    public void setSm(
        StateMachine<UseCaseContext, Event> sm) {
        this.sm = sm;
    }


    public void updateState(){
        setChanged();
        notifyObservers();
    }

    private StateService() {
    }

    public void init() {
        this.sm = new StateMachine<UseCaseContext, Event>(new UseCaseContext(), UseCaseStateMachine.INIT, UseCaseStateMachine.GLOBAL);
        sm.connect().subscribe();
        Log.i(LOG_TAG, "State machine initialized. Current state: " + sm.getState());
    }

    public void triggerEvent(Event event) {
        Log.d(LOG_TAG, "Trigger event: " + event);

        sm.accept(event);
    }
    

}
