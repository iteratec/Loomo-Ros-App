package de.iteratec.loomo.state.action;

import de.iteratec.loomo.conversation.DroidSpeechConversationService;
import de.iteratec.loomo.interaction.HeadLightService;
import de.iteratec.loomo.navigation.NavigationService;
import de.iteratec.loomo.state.StateService;


public class InitAction extends Action {

    public InitAction() {
        super("<<<<< Enter INIT State");
    }

    @Override
    public void work() {
        NavigationService.getInstance().stop();
        DroidSpeechConversationService.getInstance().startConversation();
        HeadLightService.getInstance().setOn();
        StateService state = StateService.getInstance();
    }
}
