package de.iteratec.loomo.ros.map;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maximilian on 09.10.18.
 */

public class MapConverter implements Runnable {
  private int [][]pgm;
  private int mapWidth;
  private int mapHeight;
  private double occ_thresh;
  private double free_thresh;
  MapGenerator mapGenerator;
  public MapConverter(int [][] pgm, int mapWidth, int mapHeight,double occ_thresh, double free_thresh, MapGenerator mapGenerator){
    this.pgm=pgm;
    this.mapHeight=mapHeight;
    this.mapWidth=mapWidth;
    this.occ_thresh=occ_thresh;
    this.free_thresh=free_thresh;
    this.mapGenerator=mapGenerator;
  }


  @Override
  public void run() {


    byte []map_bytes = new byte[mapWidth * mapHeight];

    List<Double> values = new ArrayList<Double>();
    for (int y = 0; y < mapHeight; y++) {
      for (int x = 0; x < mapWidth; x++) {
        int color = pgm[y][x];
        double value = 1 - (color / 255.0);
        if (!values.contains(value)) {
          values.add(value);
        }
        if (value > occ_thresh) {
          value = +100;
          //Log.d(TAG, "occupied");
        } else if (value < free_thresh) {
          value = 0;
          //Log.d(TAG, "free");
        } else {
          double value_fl = -1;
          value = (int) value_fl;
          //Log.d(TAG, "unknown " + Integer.toString(value));
        }
        map_bytes[getLinearIndex(mapWidth, x, mapHeight - y - 1)] = (byte) value;
        mapGenerator.map_bytes=map_bytes;
      }
    }
  }

  public int getLinearIndex(int sx, int i, int j) {
    return ((sx) * (j) + (i));
  }

}
