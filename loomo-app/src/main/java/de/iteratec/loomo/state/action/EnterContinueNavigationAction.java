package de.iteratec.loomo.state.action;

import de.iteratec.loomo.interaction.HeadLightService;
import de.iteratec.loomo.interaction.SpeakService;
import de.iteratec.loomo.navigation.Destination;
import de.iteratec.loomo.navigation.NavigationService;

public class EnterContinueNavigationAction extends Action {

    private Destination destination;

    public EnterContinueNavigationAction(Destination destination) {
        super("<<<<< Enter CONTINUE_TOUR");
        this.destination = destination;
    }

    @Override
    public void work() {
        SpeakService.getInstance().say("Weiter gehts");
        NavigationService.getInstance().startNavigation(destination);
        HeadLightService.getInstance().setOn();
    }
}
