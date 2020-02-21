package de.iteratec.loomo.navigation;


import android.util.Log;
import de.iteratec.loomo.location.LocationService;
import de.iteratec.loomo.location.MapPosition;
import de.iteratec.loomo.ros.nodes.NavGoalPublisher;

public class NavigationService {

    private static final String LOG_TAG = "NavigationService";

    private static NavigationService instance;
    private NavGoalPublisher navGoalPublisher;
    private LocationService locationService;

    public static synchronized NavigationService getInstance() {
        if (instance == null) {
            instance = new NavigationService();
            instance.setLocationService(LocationService.getInstance());
        }
        return instance;
    }

    private NavigationService() {
        // TODO Init ROS stuff.
    }

    public void stop() {
        if (navGoalPublisher != null) {
            navGoalPublisher.cancelGoal();
        } else {
            Log.w(LOG_TAG, "Navigation service not initialized correctly. NavGoalPublisher must be set.");
        }
    }

    public void startNavigation(Destination destination) {
        if (navGoalPublisher != null) {
            MapPosition position = locationService.getPositionForDestination(destination);
            navGoalPublisher.publishGoal(position);
        } else {
            Log.w(LOG_TAG, "Navigation service not initialized correctly. NavGoalPublisher must be set.");
        }
    }

    private void setLocationService(LocationService service) {
        this.locationService = service;
    }

    public NavGoalPublisher getNavGoalPublisher() {
        return navGoalPublisher;
    }

    public void setNavGoalPublisher(NavGoalPublisher navGoalPublisher) {
        this.navGoalPublisher = navGoalPublisher;
    }
}
