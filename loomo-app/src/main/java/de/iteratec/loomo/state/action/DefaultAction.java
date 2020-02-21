package de.iteratec.loomo.state.action;

public class DefaultAction extends Action {

    public DefaultAction(String message) {
        super("DefaultAction" + "::" + message);
    }

    @Override
    public void work() {
        // noting to do

    }
}
