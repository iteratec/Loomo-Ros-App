package de.iteratec.loomo.location;

import de.iteratec.loomo.navigation.Destination;


public enum Location {

    MUC("SLAB", "SLAB", MapPosition.MUC_EG_DOOR, MapPosition.MUC_INNOLAB, "Hallo SLAB", MapPosition.MUC_HALL_INITPOSE, MapPosition.MUC_EG_DOOR_INITPOSE, MapPosition.MUC_EG_ELEVATOR, MapPosition.MUC_EG_INSIDE_ELEVATOR, MapPosition.MUC_OG_DOOR, MapPosition.MUC_OG_SHOWROOM, MapPosition.MUC_OG_PARKING_POSITION, MapPosition.MUC_OG_PEPPER, MapPosition.MUC_OG_ELEVATOR_INITPOSE),

    //MUC_OG("Hello Daniel", MapPosition.MUC_OG_HALL_INITPOSE,MapPosition.MUC_OG_DOOR,MapPosition.MUC_OG_ELEVATOR);
    MUC_OG("Hello Daniel", MapPosition.MUC_OG_SLAB_INITPOSE,MapPosition.MUC_OG_GAUSS, MapPosition.MUC_OG_ELEVATOR);


    //TODO: fill with correct coordinates
    private final String mapEg;
    private final String map5OG;
    private Floor activeFloor = Floor.EG;

    private final MapPosition initialPositionDoorEG;
    private final MapPosition initialPositionElevatorOG;
    private final MapPosition initialPositionInsideElevator;
    private final MapPosition initialPositionWaitingHall;
    private final MapPosition posDoorEG;
    private final MapPosition innoLabPos;
    private final MapPosition innoLabParkPosition = null;
    private final MapPosition liftEg;
    private final MapPosition door5OG;
    private final MapPosition Lift5OG = null;
    private final MapPosition startPosEG = null;

    private int mapformb;
    private int mapforamcl;
    private int mapinfo;
    private MapPosition insideElevator;

    private MapPosition showRoom;
    private MapPosition showRoomParkPosition;
    private MapPosition pepper;


    private final String hello;

    private Location(String mapEg, String map5OG, MapPosition posDoorEG, MapPosition innoLabPos, String hello,  MapPosition initialPositionWaitingHall, MapPosition initialPositionEGDoor, MapPosition goalElevator, MapPosition initialPositionInsideElevator, MapPosition door5OG, MapPosition showRoom, MapPosition showRoomParkPosition, MapPosition pepper, MapPosition initialPositionElevatorOG) {
        this.map5OG = map5OG;
        this.hello = hello;
        this.mapEg = mapEg;
        this.innoLabPos = innoLabPos;
        this.posDoorEG = posDoorEG;
        this.initialPositionDoorEG = initialPositionEGDoor;
        this.initialPositionWaitingHall=initialPositionWaitingHall;
        this.liftEg=goalElevator;
        this.initialPositionInsideElevator = initialPositionInsideElevator;
        this.initialPositionElevatorOG=initialPositionElevatorOG;
        this.door5OG = door5OG;
        this.showRoom = showRoom;
        this.showRoomParkPosition = showRoomParkPosition;
        this.pepper = pepper;
    }
    private Location(String hello, MapPosition initialPositionWaitingHall, MapPosition doorGoal, MapPosition elevatorGoal){
        this.initialPositionWaitingHall= initialPositionWaitingHall;
        this.posDoorEG = doorGoal;
        this.liftEg = elevatorGoal;
        this.mapEg=null;
        this.map5OG=null;
        this.hello=hello;
        this.initialPositionDoorEG=null;
        this.innoLabPos=null;
        this.door5OG = null;
        this.initialPositionElevatorOG = null;
        this.showRoom = null;
        this.showRoomParkPosition = null;
        this.pepper = null;
        this.initialPositionInsideElevator = null;



    }
    private Location(String hello, MapPosition initialPositionWaitingHall, MapPosition doorGoal, MapPosition elevatorGoal, MapPosition insideElevator){
        this.initialPositionWaitingHall= initialPositionWaitingHall;
        this.posDoorEG = doorGoal;
        this.liftEg = elevatorGoal;
        this.insideElevator=insideElevator;
        this.mapEg=null;
        this.map5OG=null;
        this.hello=hello;
        this.initialPositionDoorEG=null;
        this.innoLabPos=null;
        this.door5OG = null;
        this.initialPositionElevatorOG = null;
        this.initialPositionInsideElevator = null;


    }
    private Location(String hello, MapPosition initialPositionWaitingHall, MapPosition doorGoal, MapPosition elevatorGoal,int mapinfo, int mapforamcl, int mapformb){
        this.initialPositionWaitingHall= initialPositionWaitingHall;
        this.posDoorEG = doorGoal;
        this.liftEg = elevatorGoal;
        this.hello=hello;
        this.mapEg=null;
        this.map5OG=null;
        this.initialPositionDoorEG=null;
        this.innoLabPos=null;
        this.door5OG = null;
        this.initialPositionElevatorOG = null;
        this.mapforamcl=mapforamcl;
        this.mapformb=mapformb;
        this.mapinfo=mapinfo;
        this.initialPositionInsideElevator = null;

    }

    public int getMapformb() {
        return mapformb;
    }

    public void setMapformb(int mapformb) {
        this.mapformb = mapformb;
    }

    public int getMapforamcl() {
        return mapforamcl;
    }

    public void setMapforamcl(int mapforamcl) {
        this.mapforamcl = mapforamcl;
    }

    public int getMapinfo() {
        return mapinfo;
    }

    public void setMapinfo(int mapinfo) {
        this.mapinfo = mapinfo;
    }

    public Floor getActiveFloor() {
        return activeFloor;
    }

    public void setActiveFloor(Floor activeFloor) {
        this.activeFloor = activeFloor;
    }

    public String getMap5OG() {
        return map5OG;
    }

    public MapPosition getPosDoorEG() {
        return posDoorEG;
    }

    public MapPosition getInnoLabPos() {
        return innoLabPos;
    }

    public MapPosition getInnoLabParkPosition() {
        return innoLabParkPosition;
    }

    public MapPosition getLiftEg() {
        return liftEg;
    }

    public MapPosition getDoor5OG() {
        return door5OG;
    }

    public MapPosition getLift5OG() {
        return Lift5OG;
    }

    public MapPosition getStartPosEG() {
        return startPosEG;
    }

    public String getHello() {
        return hello;
    }

    public MapPosition getInsideElevator() {
        return insideElevator;
    }

    public void setInsideElevator(MapPosition insideElevator) {
        this.insideElevator = insideElevator;
    }

    public MapPosition getInitialPositionWaitingHall() {
        return initialPositionWaitingHall;
    }

    public String getMapEg() {
        return mapEg;
    }

    public MapPosition getShowRoom() {
        return showRoom;
    }

    public MapPosition getShowRoomParkPosition() {
        return showRoomParkPosition;
    }

    public MapPosition getPepper() {
        return pepper;
    }

    public MapPosition getElevatorOG() {
        return initialPositionElevatorOG;
    }

    public MapPosition getInitialPosition(Destination destination) {
        switch (destination){
            case EG_DOOR:
                return initialPositionWaitingHall;
            case EG_ELEVATOR:
                return initialPositionDoorEG;
            case ELEVATOR:
                return initialPositionInsideElevator;
        }
        return null;
    }



}
