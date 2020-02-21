package de.iteratec.loomo.location;


import org.apache.commons.math3.complex.Quaternion;

public enum MapPosition {


    MUC_HALL_INITPOSE("DoorEG",4.10125112534 , -0.250560522079, new Quaternion(0.0315174199348,0,0,-0.999503202717 )),
    MUC_EG_DOOR("DoorEG", 7.81113004684, 11.7679824829, new Quaternion(0.190221427391, 0, 0, 0.981741212622)),
    MUC_EG_DOOR_INITPOSE("ELEVATOR",-10.989572525, -0.88929438591, new Quaternion(0.968738594243, 0, 0, -0.248083727852)),
    MUC_EG_ELEVATOR("ELEVATOR", 4.77823352814, 11.578584671, new Quaternion(0.574384012571, 0, 0, -0.818585979664)),
    MUC_EG_INSIDE_ELEVATOR("ELEVATOR", 3.72321605682, 8.68597221375, new Quaternion(0.554237461874, 0, 0, -0.832358598114)),
    MUC_INNOLAB("DoorEG",4.10125112534 , -0.250560522079, new Quaternion(0.0315174199348,0,0,-0.999503202717 )),
    MUC_HALL_INIT_DEPTH("DoorEG",1.90279555321,-1.08458018303, new Quaternion(0.983754760894,0,0,-0.179517604761)),
    MUC_EG_DOOR_DEPTH("DoorEG", -4.70145750046, -11.1219816208, new Quaternion(0.956114272511,0,0,0.956114272511)),
    MUC_OG_DOOR("DOOROG",5.33542823792, 12.4720191956, new Quaternion(0.978170324055,0,0, -0.207804757257)),
    MUC_OG_PARKING_POSITION("PARKING",0.737954378128, -1.59381079674, new Quaternion(0.145937514629,0,0, 0.989293809656)),
    MUC_OG_PEPPER("PEPPER",0.830009937286, -5.30675983429, new Quaternion(0.558097166965,0,0, -0.829775603537)),
    MUC_OG_ELEVATOR_INITPOSE("OG_ELEVATOR",4.68395614624,11.1073694229, new Quaternion(0.772880568608,0,0, 0.634551516166)),

    MUC_OG_SHOWROOM("SHOWROOM",2.34026002884, -1.07849836349, new Quaternion(0.989281093877,0,0, -0.14602368745)),
    MUC_OG_ELEVATOR("OG_ELEVATOR",-3.6852478981,7.84068918228, new Quaternion(0.901749405093,0,0,-0.432259193557)),
    MUC_OG_HALL_INITPOSE("INITPOSEOG",-1.43370378017, 0.346371173859, new Quaternion(0.462857468833,0,0, 0.886432718003)),

    MUC_OG_SLAB_INITPOSE("INITPOSEOG",0.281323194504, -0.410026311874, new Quaternion(0.409392184575,0.0,0.0, 0.912358503665)),
    MUC_OG_GAUSS("GAUSS",3.72308301926, 8.60091495514, new Quaternion(0.922119574287, 0,0, 0.38690501511));
    //MUC_EG_DOOR_DEPTH("DoorEG", -4.70145750046, -11.1219816208, new Quaternion(0.956114272511,0,0,0.956114272511)),

    private double x;
    private double y;
    private org.apache.commons.math3.complex.Quaternion quaternion;
    private String goalName;

     MapPosition(String goalName, double x, double y, org.apache.commons.math3.complex.Quaternion quaternion) {
        this.x = x;
        this.y = y;
        this.quaternion = quaternion;
    }

    MapPosition(String goalName, double x, double y) {
        this.x = x;
        this.y = y;
        this.quaternion = org.apache.commons.math3.complex.Quaternion.IDENTITY;
    }

    MapPosition(String goalName) {
        this.x = 0;
        this.y = 0;
        this.quaternion = org.apache.commons.math3.complex.Quaternion.IDENTITY;
    }


    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public org.apache.commons.math3.complex.Quaternion getQuaternion() {
        return quaternion;
    }

    public void setQuaternion(org.apache.commons.math3.complex.Quaternion quaternion) {
        this.quaternion = quaternion;
    }
}
