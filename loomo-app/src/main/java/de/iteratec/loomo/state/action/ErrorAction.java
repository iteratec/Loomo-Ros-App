package de.iteratec.loomo.state.action;

public class ErrorAction extends Action {

    public ErrorAction() {
        super("WARNING!!! Wrong configuration. This state should never be set!!!!");
    }

    @Override
    public void work() {
        // noting to do
    }
}
