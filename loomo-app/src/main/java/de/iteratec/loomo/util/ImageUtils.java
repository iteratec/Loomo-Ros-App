package de.iteratec.loomo.util;

import android.graphics.Bitmap;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.segway.robot.sdk.vision.calibration.Intrinsic;
import sensor_msgs.CameraInfo;

public final class ImageUtils {

    public static Bitmap depth2Grey(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();

        int[] pixels = new int[width * height];

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                //grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);
                grey = (red * 38 + green * 75 + blue * 15) >> 7;
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    public static CameraInfo calibrateCameraInfo(CameraInfo cameraInfo, int type, Intrinsic intrinsic, int width, int height) {
        double[] k = new double[9];
//        # Intrinsic camera matrix for the raw (distorted) images.
//        #     [fx  0 cx]
//        # K = [ 0 fy cy]
//        #     [ 0  0  1]
        k[0] = intrinsic.focalLength.x;
        k[1] = 0;
        k[2] = intrinsic.principal.x;
        k[3] = 0;
        k[4] = intrinsic.focalLength.y;
        k[5] = intrinsic.principal.y;
        k[6] = 0;
        k[7] = 0;
        k[8] = 1;

        double[] d = Doubles.toArray(Floats.asList(intrinsic.distortion.value.clone()));

        double[] p = new double[12];
//        # Projection/camera matrix
//        #     [fx'  0  cx' Tx]
//        # P = [ 0  fy' cy' Ty]
//        #     [ 0   0   1   0]
        p[0] = intrinsic.focalLength.x;
        p[1] = 0;
        p[2] = intrinsic.principal.x;
        p[3] = 0;
        p[4] = 0;
        p[5] = intrinsic.focalLength.y;
        p[6] = intrinsic.principal.y;
        p[7] = 0;
        p[8] = 0;
        p[9] = 0;
        p[10] = 1;
        p[11] = 0;

        cameraInfo.setWidth(width);
        cameraInfo.setHeight(height);
        cameraInfo.setK(k);
        cameraInfo.setD(d);
        cameraInfo.setP(p);
        cameraInfo.setDistortionModel("plumb_bob");
        return cameraInfo;
    }
}
