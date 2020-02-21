package de.iteratec.loomo.state.action;

import de.iteratec.loomo.followme.FollowMePresenter;
import de.iteratec.loomo.interaction.HeadLightService;
import de.iteratec.loomo.interaction.SpeakService;

public class EnterFollowMeAction extends Action {
    private FollowMePresenter mFollowMePresenter;


    public EnterFollowMeAction(){
        super("<<<<< Enter FOLLOW_ME");
        //mFollowMePresenter = new FollowMePresenter();
        //
        //mFollowMePresenter = FollowMePresenter.getInstance();
        //Log.i("followme1", "" + mFollowMePresenter);
        //mFollowMePresenter.startPresenter();
//        FollowMePresenter.getInstance().stopPresenter();
    }


    @Override
    public void work() {
        SpeakService.getInstance().say("Ok, ich folge Dir.");
        HeadLightService.getInstance().setFollowMe();
        // TODO: Implement follow me mode
        mFollowMePresenter = FollowMePresenter.getInstance();
        mFollowMePresenter.startPresenter();
        mFollowMePresenter.setObstacleAvoidanceOpen(true);
        mFollowMePresenter.actionInitiateTrack();
    }
}
