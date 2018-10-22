package com.example.jayam.robot_controller;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.VerticalSeekBar;
import java.util.HashMap;
import java.util.Map;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {
    public static final int MAX_SERVO_VALUE = 700;
    public static final int MAX_MOTOR_VALUE = 750;
    public static final int TURNING_THRESHOLD = 75;
    public static final String WS_TAG = "WebSocket";

    public static Map<String, Integer> parameters = new HashMap<>();
    private String targetURL = "ws://192.168.4.1:80";

    //private OkHttpClient client = new OkHttpClient();

    //private WebSocket ws;

    private RequestThread requestThread = new RequestThread();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        //VerticalSeekBar slider1 = (VerticalSeekBar) findViewById(R.id.slider1);
        VerticalSeekBar slider2 = (VerticalSeekBar) findViewById(R.id.slider2);

        // Setup Listeners //TODO: Re-enable slider for dual servo support
//        slider1.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onStartTrackingTouch(SeekBar arg0) {
//                // Do nothing
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar arg0) {
//                // Do nothing
//            }
//
//            @Override
//            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
//                // Send HTTP request here
//                int power = (int) Math.floor(MAX_SERVO_VALUE * (progress / 100.0));
//                parameters.put("S1", power);
//            }
//        });

        slider2.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // Do nothing

            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // Do nothing
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                // Send HTTP request here
                int power = (int) Math.floor(MAX_SERVO_VALUE * ((progress - 50) / 50.0));
                parameters.put("S2", power);

            }
        });

        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                int[] motorSpeeds = convertToMotorSpeeds(angle, strength);
                parameters.put("M1", motorSpeeds[0]);
                parameters.put("M2", motorSpeeds[1]);
            }
        });


        // Setup parameter map
        parameters.put("M1", 0);
        parameters.put("M2", 0);
        parameters.put("S1", -1 * MAX_SERVO_VALUE);
        parameters.put("S2", -1 * MAX_SERVO_VALUE);

        requestThread.start();

    }   // OnCreate


    public int[] convertToMotorSpeeds(int angle, int strength) {
        double xControl = strength * Math.cos(Math.toRadians(angle));
        double yControl = strength * Math.sin(Math.toRadians(angle));

        if (Math.abs(xControl) <= TURNING_THRESHOLD) {
            xControl = (xControl / 100) * 15;
        } else {
            double temp = Math.abs(xControl);
            double start = ((67.0 / 100) * 15);    //X-Coordinate where piecewise curves meet
            double scaled = (((temp - 67) / 33) * 10); //scaled 67->100 to 0->10
            temp = start + (Math.pow(scaled, 2) / 100) * (100 - start);  //follow y=x^2 curve for the section of the curve from where x-coordinate is start->100
            xControl = temp * ((xControl < 0) ? -1 : 1);   //add sign back to xControl
        }

        //Invert X
        xControl = xControl * -1;

        //Calc R+L = V
        double vTemp = (100 - Math.abs(xControl)) * (yControl / 100) + yControl;

        //Calc R-L = W
        double wTemp = (100 - Math.abs(yControl)) * (xControl / 100) + xControl;

        //Calc R
        double R_Motor = (vTemp + wTemp) / 2;
        double L_Motor = (vTemp - wTemp) / 2;

        //Scale to -1023 to 1023
        R_Motor = (R_Motor * MAX_MOTOR_VALUE) / 100;
        L_Motor = (L_Motor * MAX_MOTOR_VALUE) / 100;

        int MotorSpeeds[] = {(int) L_Motor, (int) R_Motor};
        return MotorSpeeds;
    }
}