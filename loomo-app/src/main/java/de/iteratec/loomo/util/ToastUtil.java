package de.iteratec.loomo.util;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.Toast;
import de.iteratec.loomo.R;

public class ToastUtil {

    private static String oldMsg;
    private static Toast toast;
    private static long firstTime;
    private static long secondTime;
    private static Button bt_toast;

    /**
     * @param message
     * @Title showToast
     * @Description only one Toast used
     */
    public static void showToast(Context context, String message) {
        if (toast == null) {
            oldMsg = message;
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
            bt_toast = new Button(context);
            //style
            bt_toast.setBackgroundResource(R.drawable.shape_toast);
            bt_toast.setTextColor(Color.parseColor("#00FF00"));
            bt_toast.setText(message);
            bt_toast.setAllCaps(false);
            bt_toast.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            toast.setView(bt_toast);
            toast.show();
            firstTime = System.currentTimeMillis();
        } else {
            secondTime = System.currentTimeMillis();
            if (message.equals(oldMsg)) {
                if (secondTime - firstTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = message;
                bt_toast.setText(message);
                toast.show();
            }
        }
        firstTime = secondTime;
    }
}
