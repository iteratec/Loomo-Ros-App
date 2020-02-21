package de.iteratec.loomo.state.action;

import android.util.Log;
import de.iteratec.loomo.state.Event;
import de.iteratec.loomo.state.StateMachine;
import de.iteratec.loomo.state.StateService;
import de.iteratec.loomo.state.UseCaseContext;
import io.reactivex.functions.BiConsumer;

/**
 * Created by dst on 05.09.2018.
 */

public abstract class Action implements BiConsumer<UseCaseContext, StateMachine.State<UseCaseContext, Event>> {

    private static final String LOG_TAG = "Action";

    private String text;

    public Action(String text) {
        this.text = text;
    }

    @Override
    public void accept(UseCaseContext context, StateMachine.State<UseCaseContext, Event> state) {
        StateService.getInstance().updateState();
        Log.d(LOG_TAG, "notifying Observer");
        Log.i(LOG_TAG, "" + text + ":" + context + ":" + state);
        work();
    }

    public abstract void work();
}
