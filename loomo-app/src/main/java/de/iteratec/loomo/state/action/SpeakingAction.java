package de.iteratec.loomo.state.action;

import de.iteratec.loomo.interaction.SpeakService;

public class SpeakingAction extends Action {
    private String text;

    public SpeakingAction(String text) {
        super("<<<<< Speaking Action");
        this.text = text;
    }

    @Override
    public void work() {
        SpeakService.getInstance().say(text);
    }
}
