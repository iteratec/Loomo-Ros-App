package de.iteratec.loomo.state.action;

import de.iteratec.loomo.followme.FollowMePresenter;
import de.iteratec.loomo.interaction.HeadLightService;
import de.iteratec.loomo.interaction.SpeakService;

public class StopFollowMeAction extends Action {


    public StopFollowMeAction() {
        super("<<<<< Exit FOLLOW_ME");
    }

    @Override
    public void work() {
        SpeakService.getInstance().say("Ok.");
        HeadLightService.getInstance().setWaiting();
        FollowMePresenter.getInstance().closeDetectorTrack();
        FollowMePresenter.getInstance().stopPresenter();
    }
}
