package de.iteratec.loomo.state.action;

import de.iteratec.loomo.interaction.HeadLightService;
import de.iteratec.loomo.interaction.SpeakService;
import de.iteratec.loomo.ros.RosService;

public class EnterDistanceCheckAction extends Action {
    private String text;


    public EnterDistanceCheckAction(String text) {
        super("<<<<< Enter EG_ELEVATOR_WAITING_HALL");
        this.text = text;
    }

    @Override
    public void work() {
        HeadLightService.getInstance().setWaiting();
        RosService.getInstance().checkDistances();
        SpeakService.getInstance().say(text);
    }
}
