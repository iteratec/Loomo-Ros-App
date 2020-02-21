package de.iteratec.loomo.ros.map;

import java.util.Arrays;

/**
 * Created by maximilian on 05.09.18.
 */

class NavigationMap {

    private String image;
    private float resolution;
    private double[] origin;
    private boolean negate;
    private double occupied_thresh;
    private double free_thresh;

  /*public NavigationMap(String image, double resolution, double[] origin, boolean negate,
      double occupied_thresh, double free_thresh) {
    this.image = image;
    this.resolution = resolution;
    this.origin = origin;
    this.negate = negate;
    this.occupied_thresh = occupied_thresh;
    this.free_thresh = free_thresh;
  }*/

    @Override
    public String toString() {
        return "NavigationMap{" +
                "image='" + image + '\'' +
                ", resolution=" + resolution +
                ", origin=" + Arrays.toString(origin) +
                ", negate=" + negate +
                ", occupied_thresh=" + occupied_thresh +
                ", free_thresh=" + free_thresh +
                '}';
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public float getResolution() {
        return resolution;
    }

    public void setResolution(float resolution) {
        this.resolution = resolution;
    }

    public double[] getOrigin() {
        return origin;
    }

    public void setOrigin(double[] origin) {
        this.origin = origin;
    }

    public boolean isNegate() {
        return negate;
    }

    public void setNegate(boolean negate) {
        this.negate = negate;
    }

    public double getOccupied_thresh() {
        return occupied_thresh;
    }

    public void setOccupied_thresh(double occupied_thresh) {
        this.occupied_thresh = occupied_thresh;
    }

    public double getFree_thresh() {
        return free_thresh;
    }

    public void setFree_thresh(double free_thresh) {
        this.free_thresh = free_thresh;
    }
}
