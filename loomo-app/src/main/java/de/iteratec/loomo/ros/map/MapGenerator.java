/*
 * Copyright 2017 Ekumen, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.iteratec.loomo.ros.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import geometry_msgs.Pose;
import nav_msgs.MapMetaData;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.ros.internal.message.MessageBuffers;
import org.ros.message.Time;
import org.ros.rosjava_geometry.Quaternion;
import std_msgs.Header;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class that generates message for an empty map of 10x10 meters.
 * TODO: This should be replaced by a Map Generator that could generate a map grid from an image with metadata.
 */
public class MapGenerator implements OccupancyGridGenerator {
    private static final String TAG = "MapGenerator";
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final float RESOLUTION = (float) 0.08;
    private int mapWidth;
    private int mapHeight;
    Context context;
    private double free_thresh;
    private double occ_thresh;
    public byte[] map_bytes;
    private float resolution;
    private MapMetaData information;
    private Pose origin;
    private String map_type;
    private int[][] pgm;
    private PGM pgmOneD;
    private Bitmap bitmap;
    private Runnable mapConverter;
    private int mapResource;
    private String filename;

    public MapGenerator(Context context) {
        this.context = context;
        Log.d(TAG, "Constructor called");
    }

    public void fillHeader(Header header) {
        header.setFrameId("map");
        header.setStamp(Time.fromMillis(System.currentTimeMillis()));
    }


    public void fillFromYaml(MapMetaData information) {
        this.information = information;

    }

    public NavigationMap readInMapInfo(int infoResource,int mapResource) {
        this.mapResource=mapResource;
        NavigationMap map = null;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            map = mapper.readValue(getInfoforLocation(infoResource), NavigationMap.class);
            occ_thresh = map.getOccupied_thresh();
            free_thresh = map.getFree_thresh();
            information.getOrigin().getPosition().setX(map.getOrigin()[0]);
            information.getOrigin().getPosition().setY(map.getOrigin()[1]);
            information.getOrigin().getPosition().setZ(map.getOrigin()[2]);
            resolution = map.getResolution();

            this.pgmOneD = read1DPGM(mapResource);
            mapHeight = pgmOneD.getHeight();
            mapWidth = pgmOneD.getWidth();
            pgmOneD.setData(convert(pgmOneD.getData()));
            information.setResolution(resolution);
            information.setWidth(mapWidth);
            information.setHeight(mapHeight);
            Quaternion.identity().toQuaternionMessage(information.getOrigin().getOrientation());

            Log.d(TAG, map.toString());
        } catch (IOException e) {
            Log.e(TAG, "Couldn't read in Map Info from YAML");
        }

        return map;
    }

    public void convertData(Object loaded_info) {
        convert(loaded_info);
    }

    public void convert(Object object) {
        Log.d(TAG, "could not convert because unknown format");
    }



    /**
     * Does some thing in old style.
     *
     * @deprecated use {@link #convert(byte[])} ()} instead.
     */
    @Deprecated
    public void convert(int[][] pgm) {
        // Set the origin to the center of the map


        map_bytes = new byte[mapWidth * mapHeight];
        Log.d(TAG, "Image loaded New ! Height: " + Integer.toString(mapHeight) + " Width: " + Integer.toString(mapWidth));
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                int color = pgm[y][x];
                double value = 1 - (color / 255.0);
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

            }
        }
        information.setMapLoadTime(Time.fromMillis(System.currentTimeMillis()));


    }
    public byte[] convert(byte[] pgm) {
        // Set the origin to the center of the map

        for (int i = 0; i < pgm.length; i++) {
            int color = pgm[i] & 0xFF;

            double value = 1 - (color / 255.0);

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
            pgm[i] = (byte) value;
        }
        Log.d(TAG, "First byte Read:" + (int)pgm[0]);
        return pgm;
    }

    public void readinBitmap(MapMetaData information) {

    }

    public void fillInformation(MapMetaData information, int infoResource ,int mapResource) {
        //TODO implement location chooser
        Log.d(TAG, "fillInformation called...");
        fillFromYaml(information);
        readInMapInfo(infoResource,mapResource);


    }




    public ChannelBuffer generateData() {

        ChannelBufferOutputStream output = new ChannelBufferOutputStream(MessageBuffers.dynamicBuffer());
        try {
            output.write(pgmOneD.getData());
        } catch (Exception e) {
            throw new RuntimeException("Empty map generator generateData error: " + e.getMessage());
        }
        map_bytes=null;
        return output.buffer();
    }

    private InputStream getMapforLocation(int mapResource) {

        return context.getResources().openRawResource(mapResource);
    }

    private InputStream getInfoforLocation(int infResource) {
        return context.getResources().openRawResource(infResource);
    }

    /**
     * Does some thing in old style.
     *
     * @deprecated use {@link #read1DPGM(int)} ()} instead.
     */
    @Deprecated
    public int[][] readPGM(int mapResource) {

        try {
            return PGMIO.read(getMapforLocation(mapResource));
        } catch (IOException e) {
            Log.e(TAG, "Couldn't convert PGM" + e.getMessage());
        }
        return null;
    }

    public PGM read1DPGM(int mapResource) {
        try {
            return PGMIO.read1D(getMapforLocation(mapResource));
        } catch (IOException e) {
            Log.e(TAG, "Couldn't convert PGM" + e.getMessage());
        }
        return null;
    }



    public int getLinearIndex(int sx, int i, int j) {
        return ((sx) * (j) + (i));
    }

}