package de.iteratec.loomo.ros.map;

/**
 * Created by maximilian on 17.10.18.
 */

public class PGM {

  private byte[] data;
  private int height;
  private int width;

  public PGM(int height, int width) {
    this.height = height;
    this.width = width;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }
}
