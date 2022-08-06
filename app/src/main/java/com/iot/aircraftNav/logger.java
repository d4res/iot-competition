package com.iot.aircraftNav;

import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class logger {
    private final TextView tv;
    public logger(TextView tv) {
        this.tv = tv;
    }
    private final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    public void add(String tag, String msg) {
        Date date = new Date();
        tv.append(String.format("%s %s:%s\n", formatter.format(date), tag, msg));
        Log.d(tag, msg);
    }
}
