package de.iteratec.loomo.state;

import android.util.Log;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;


public class StateMachine<T, E> implements Consumer<E> {

    private static final String LOG_TAG = "StateMachine";

    public static class State<T, E> {
        private String name;
        private BiConsumer<T, State<T, E>> enter;
        private BiConsumer<T, State<T, E>> exit;
        private Map<E, State<T, E>> transitions = new HashMap<>();

        public State(String name) {
            this.name = name;
        }

        public State<T, E> onEnter(BiConsumer<T, State<T, E>> func) {
            this.enter = func;
            return this;
        }

        public State<T, E> onExit(BiConsumer<T, State<T, E>> func) {
            this.exit = func;
            return this;
        }

        public void enter(T context) {
            try {
                enter.accept(context, this);
            } catch (Exception e) {
                Log.w(LOG_TAG, e.getMessage());
            }
        }

        public void exit(T context) {
            try {
                exit.accept(context, this);
            } catch (Exception e) {
                Log.w(LOG_TAG, e.getMessage());
            }
        }

        public State<T, E> transition(E event, State<T, E> state) {
            transitions.put(event, state);
            return this;
        }

        public State<T, E> next(E event) {
            return transitions.get(event);
        }

        public String toString() {
            return name;
        }
    }

    private volatile State<T, E> state;
    private State<T, E> fallbackState;
    private final T context;
    private final PublishSubject<E> events = PublishSubject.create();

    public StateMachine(T context, State<T, E> initial, State<T, E> fallback) {
        this.state = initial;
        this.context = context;
        this.fallbackState = fallback;
    }

    public Observable<Void> connect() {

        return Observable.create(new ObservableOnSubscribe<Void>() {
            @Override
            public void subscribe(ObservableEmitter<Void> sub) throws Exception {
                state.enter(context);

                sub.setDisposable(events.collect((Callable) context, new BiConsumer<T, E>() {
                    @Override
                    public void accept(T context, E event) throws Exception {
                        State<T, E> next = state.next(event);
                        if (next == null) {
                            Log.i(LOG_TAG, "No event ('" + event + "') defined for state : " + state + " check global.");
                            next = fallbackState.next(event);
                        }
                        if (next != null) {
                            state.exit(context);
                            state = next;
                            Log.i(LOG_TAG, "New state : " + state + " event: " + event);
                            next.enter(context);
                        } else {
                            Log.w(LOG_TAG, "Invalid event : " + event + " for state " + state);
                        }
                    }

                }).subscribe());
            }
        });

    }

    @Override
    public void accept(E event) {
        events.onNext(event);
    }

    public State<T, E> getState() {
        return state;
    }

}
