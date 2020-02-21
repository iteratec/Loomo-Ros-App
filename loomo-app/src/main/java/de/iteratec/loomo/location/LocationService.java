package de.iteratec.loomo.location;


import de.iteratec.loomo.navigation.Destination;
import org.apache.commons.lang.NotImplementedException;

public class LocationService {

    private Location location = Location.MUC_OG;

    private static LocationService instance;

    public static synchronized LocationService getInstance() {
        if (instance == null) {
            instance = new LocationService();
        }
        return instance;
    }

    private LocationService() {

    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getHelloText() {
        return this.location.getHello();
    }

    public MapPosition getPositionForDestination(Destination destination) {
        if (destination == Destination.EG_DOOR) {
            return location.getPosDoorEG();
        } else if (destination == Destination.INNO_LAB) {
            return location.getInnoLabPos();
        } else if (destination == Destination.OG_DOOR) {
            return location.getDoor5OG();
        } else if (destination == Destination.EG_ELEVATOR) {
            return location.getLiftEg();
        }else if(destination==Destination.INIT_POS){
            return location.getInitialPositionWaitingHall();
        } else if(destination==Destination.INSIDE_ELEVATOR){
        return location.getInsideElevator();
        }
         else if (destination == Destination.SHOWROOM){
            return location.getShowRoom();
        } else if (destination == Destination.PARKING_POSITION){
            return location.getShowRoomParkPosition();
        } else if (destination == Destination.PEPPER){
            return location.getPepper();
        } else if(destination == Destination.ELEVATOR_WAITING_HALL){
            return location.getElevatorOG();
        }
        throw new NotImplementedException("TODO. Destination " + destination + " not implemented.");
    }


}
