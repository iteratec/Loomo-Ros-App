package de.iteratec.loomo.state.action;

import de.iteratec.loomo.interaction.PepperConnectionService;
import de.iteratec.loomo.interaction.SpeakService;

public class CommunicateWithPepperAction extends Action {
    private String text;

    public CommunicateWithPepperAction(String message) {
        super("<<<<< ENTER COMMUNICATE_WITH_PEPPER");
        this.text = message;
    }

    @Override
    public void work() {
        SpeakService.getInstance().say(text);
        PepperConnectionService.getInstance().testMessageToPepper();
    }
}
