package com.example.sensortypes;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;

import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class MainActivity extends AppCompatActivity implements SensorEventListener, ActivityCompat.OnRequestPermissionsResultCallback {
    // Konstante fuer das Log
    private static final String TAG = "MainActivity";
    /**
     * Sensoren
     */
    private Sensor acceleromter = null;
    private Sensor gyroscope = null;
    private Sensor magnet = null;
    private Sensor stepCounter = null;
    private Sensor linAcceleromter = null;
    private Sensor gravity = null;
    private Sensor pressure = null;
    private Sensor proximity = null;
    private Sensor orientation = null;     // test für TYPE_GAME_ROTATION_VECTOR


    // SenorManager, der den Zugriff auf die Sensoren garantiert
    private SensorManager sensorManager = null;

    private float maxDistance;
    // Speicherung der entsprechenden Sensorwerte
    private float[] werteGrav = new float[3];
    private float[] werteMag = new float[3];
    private float[] werteBes = new float[3];
    private float[] werteGyro = new float[3];
    private float[] werteLin = new float[3];
    private String distance = "";
    private float wertPress = 0;
    private int stepcounter = 0;
    private int az = 0;
    private int pi = 0;
    private int ro = 0;
    // Daten für den virtuellen Lagesensor
    float[] accData = new float[3];
    float[] magData = new float[3];

    // Prueft, Welche Sensorwerte aufgezeichnet werden sollen
    private boolean isRunningAcc;
    private boolean isRunningGyr;
    private boolean isRunningMag;
    private boolean isRunningStepcounter;
    private boolean isRunningLin;
    private boolean isRunningGrav;
    private boolean isRunningPress;
    private boolean isRunningProximity;
    private boolean isRunningOrient;
    // Start der Sensoranzeige
    private boolean buttonStart;

    private static int counter = 0;
    private static int countSensor = 0;
    private int counterSteps = 0;
    // Attribute fuer Dateien, Verzeichnisse und I/O Elemente
    BufferedWriter writer;
    File dir = null;
    File file = null;
    File sdKarte = null;
    // Sensorwerte werden gestartet und gestoppt
    private Button start = null;
    private Button stop = null;
    // zwecks Rotationsanpassung bei Drehung des Endgeräts
    private Display display;
    /**
     * Anzeige der Sensorwerte
     */
    private TextView showAcc = null;
    private TextView showGyro = null;
    private TextView showMag = null;
    private TextView showStepCount = null;
    private TextView showLinAcc = null;
    private TextView showGrav = null;
    private TextView showPress = null;
    private TextView showProx = null;
    private TextView showOrient = null;
    /**
     * Anzeige Checkbox
     */
    private CheckBox checkAcc = null;
    private CheckBox checkGyr = null;
    private CheckBox checkStep = null;
    private CheckBox checkLin = null;
    private CheckBox checkGrav = null;
    private CheckBox checkProx = null;
    private CheckBox checkMag = null;
    private CheckBox checkOrient = null;
    private CheckBox checkPress = null;
    // Liste, die Sensorwerte speichern
    private List<Values> all = new ArrayList<Values>();
    private long fixedTime = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /**
         * Verweis auf die UI-Elemente
         */
        //(TextView)
        showAcc = (TextView) findViewById(R.id.accwerte);
        showGyro = (TextView) findViewById(R.id.gyrwerte);
        showMag = (TextView) findViewById(R.id.magwerte);
        showStepCount = (TextView) findViewById(R.id.stepwerte);
        showLinAcc = (TextView) findViewById(R.id.linwerte);
        showGrav = (TextView) findViewById(R.id.gravwerte);
        showOrient = (TextView) findViewById(R.id.orientwert);
        showPress = (TextView) findViewById(R.id.luftwerte);
        showProx = (TextView) findViewById(R.id.nahwerte);
        showOrient = (TextView) findViewById(R.id.orientwert);
        // (Checkbox)
        checkAcc = (CheckBox) findViewById(R.id.checkAcc);
        checkGyr = (CheckBox) findViewById(R.id.checkGyr);
        checkStep = (CheckBox) findViewById(R.id.checkStep);
        checkLin = (CheckBox) findViewById(R.id.checkLin);
        checkGrav = (CheckBox) findViewById(R.id.checkGrav);
        checkProx = (CheckBox) findViewById(R.id.checkProx);
        checkMag = (CheckBox) findViewById(R.id.checkMag);
        checkOrient = (CheckBox) findViewById(R.id.checkOrient);
        checkPress = (CheckBox) findViewById(R.id.checkPress);
        // (Button)
        start = (Button)findViewById(R.id.startButton);
        stop = (Button)findViewById(R.id.stopButton);
        /**
         * Button start und stop mit mit Aktionen verknuepfen
         */
        start.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                start.setEnabled(false);
                stop.setEnabled(true);

                if (checkAcc.isChecked()) isRunningAcc = true;
                if (checkGyr.isChecked()) isRunningGyr = true;
                if (checkLin.isChecked()) isRunningLin = true;
                if (checkGrav.isChecked())isRunningGrav = true;
                if (checkMag.isChecked()) isRunningMag = true;
                if (checkPress.isChecked())isRunningPress = true;
                if (checkOrient.isChecked())isRunningOrient = true;
                if (checkStep.isChecked()) isRunningStepcounter = true;
                if (checkProx.isChecked()) isRunningProximity = true;

                try {
                    if (Build.VERSION.SDK_INT >= 23){// Prueft, ob die App Zugriffsrechte auf die Hardware hat (seit API 23)
                        int checkPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    }
                    /* if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) { ***Seit Android 5.1 muss eine manuelle API geschrieben werden,
                        sdKarte = Environment.getExternalStorageDirectory();                          um Schreibrechte der SD-Karte zu verleihen. Das erfolgt beim
                        if (sdKarte.exists() && sdKarte.canWrite()) {                                 update.****
                            file = new File(sdKarte, "SensordataSD" + System.currentTimeMillis() + ".tsv");
                            writer = new BufferedWriter(new FileWriter(file, true));
                        }
                    }*/
                    dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    file = new File(dir, "Sensordata" + System.currentTimeMillis() + ".tsv");
                    writer = new BufferedWriter(new FileWriter(file, true));

                } catch (IOException e) {
                    e.printStackTrace();
                }

                fixedTime = System.currentTimeMillis();
                //buttonStart = true;
                return true;
            }
        });

        stop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                start.setEnabled(true);
                stop.setEnabled(false);
                //buttonStart = false;

                isRunningAcc = false;
                isRunningGyr = false;
                isRunningLin = false;
                isRunningGrav = false;
                isRunningMag = false;
                isRunningPress = false;
                isRunningOrient = false;
                isRunningStepcounter = false;
                isRunningProximity = false;
                sensorValueOutput();

                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        // sensorManager initialisieren
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // Verweis auf die Sensorik im System
        // Liste aller Sensoren im System
        List<Sensor> sensor = sensorManager.getSensorList(Sensor.TYPE_ALL);

        // Die gefragten Sensoren aus der Liste den Attributen zuweisen
        for (Sensor s : sensor) {
            switch (s.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    acceleromter = s;
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    gyroscope = s;
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magnet = s;
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    stepCounter = s;
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    linAcceleromter = s;
                    break;
                case Sensor.TYPE_GRAVITY:
                    gravity = s;
                    break;
                case Sensor.TYPE_PRESSURE:
                    pressure = s;
                    break;
                case Sensor.TYPE_PROXIMITY:
                    proximity = s;
                    break;
                case Sensor.TYPE_GAME_ROTATION_VECTOR:
                    orientation = s;
                default:
            }
        }
        // Referenz zum Window Manager
        WindowManager windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        //Default Display Einstellung
        display = windowManager.getDefaultDisplay();
    }

    /**
     * Listet die Sensorwerte dem Sensortyp nach in einer
     * Liste auf, welche anschließend in eine Datei geschrieben werden
     */
    public void sensorValueOutput() {
            if (writer != null) {
                // Überschrift
                try {
                    writer.write(String.format("Time\t\taccel_x\taccel_y\taccel_z\tgyro_x\tgyro_y\tgyro_z\tmag_x\tmag_y\tmag_z" +
                                               "\tlin_x\tlin_y\tlin_z\tgrav_x\tgrav_y\tgrav_z\tpress\tprox\tsteps" +
                                               "\tazi\tpit\trol"));
                    writer.write("\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Sensorwerte werden in die Datei geschrieben
                for (int i = 0; i < all.size(); ++i) {
                    try {
                        writer.write(String.format("%d\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%s\t%d\t%d\t%d\t%d", all.get(i).time, all.get(i).ax, all.get(i).ay, all.get(i).az
                                     , all.get(i).gx, all.get(i).gy, all.get(i).gz, all.get(i).mx, all.get(i).my, all.get(i).mz, all.get(i).lx, all.get(i).ly, all.get(i).lz
                                     , all.get(i).grx, all.get(i).gry, all.get(i).grz, all.get(i).press, all.get(i).prox, all.get(i).stepper, all.get(i).azimuth, all.get(i).pitch, all.get(i).roll));
                        writer.write("\r\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // Ausgabe aller 0 Sensorwerte
                /*try {
                    writer.write(String.format("Anzahl der Nullwerte: \t%d\n", counter));
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                // Ausgabe aller beschriebenen Sensorwerte
                try {
                    writer.write(String.format("Anzahl Sensorwerte: \t%d", countSensor));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    /**
     * Nur bei Aenderung von Messergebnissen
     *
     * @param sensor
     * @param accuracy
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     *
     * @param a Wert 1
     * @param b Wert 2
     * @param eps Toleranzbereich
     * @return True, wenn die Differenz unterhalb des Toleranzbereichs liegt
     */
    public boolean compare(float a, float b , float eps) {
        return Math.abs(a - b) < eps;
    }


    /**
     * SensorManager stellt Sensorwerte bereit
     *
     * @param event aktuelle Sensorwerte
     */
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public void onSensorChanged(SensorEvent event) {
            long time = System.currentTimeMillis();
            long timeInSensor = System.currentTimeMillis();
            if (event.sensor == acceleromter) {
                if (isRunningAcc) {
                    // Kopie vom Werte Array, um das Vermischen alter und neuer Daten zu vermeiden
                    werteBes = event.values.clone();
                    // Noch zu implementieren: Merkliste.add(werteBes);
                    showAcc.setText(String.format("x: %s\ty: %s\tz: %s", Math.round(100.0 * werteBes[0]) / 100.0, Math.round(100.0 * werteBes[1]) / 100.0, Math.round(100.0 * werteBes[2]) / 100.0));
                    // Die Beschleunigungswerte werden zur Auswertung im Logcat aufgezeichnet
                    Log.i(TAG, "bx: " + Math.round(100.0 * werteBes[0]) / 100.0 + " \tby: " + Math.round(100.0 * werteBes[1]) / 100.0 + " \tbz: " + Math.round(100.0 * werteBes[2]) / 100.0);
                } else {
                    showAcc.setText(String.format(""));
                }
            }

            if (event.sensor == gyroscope) {
                if (isRunningGyr == true) {
                    werteGyro = event.values.clone();
                    // Anzeige der Sensorwerte im Display
                    showGyro.setText(String.format("x: %s\ty: %s\tz: %s", Math.round(100.0 * werteGyro[0]) / 100.0, Math.round(100.0 * werteGyro[1]) / 100.0, Math.round(100.0 * werteGyro[2]) / 100.0));
                    // Die Gyroskopswerte werden zur Auswertung im Logcat aufgezeichnet
                    Log.i(TAG, "gx: " + Math.round(100.0 * werteGyro[0]) / 100.0 + " \tgy: " + Math.round(100.0 * werteGyro[1]) / 100.0 + " \tgz: " + Math.round(100.0 * werteGyro[2]) / 100.0);
                } else {
                    showGyro.setText(String.format(""));
                }
            }

            if (event.sensor == magnet) {
                if (isRunningMag) {
                    werteMag = event.values.clone();
                    showMag.setText(String.format("x: %s\ty: %s\tz: %s", Math.round(100.0 * werteMag[0]) / 100.0, Math.round(100.0 * werteMag[1]) / 100.0, Math.round(100.0 * werteMag[2]) / 100.0));
                    // Die Magnetwerte werden zur Auswertung im Logcat aufgezeichnet
                    Log.i(TAG, "mx: " + Math.round(100.0 * werteMag[0]) / 100.0 + " \tmy: " + Math.round(100.0 * werteMag[1]) / 100.0 + " \tmz: " + Math.round(100.0 * werteMag[2]) / 100.0);
                } else {
                    showMag.setText(String.format(""));
                }
            }

            if (event.sensor == linAcceleromter) {
                if (isRunningLin) {
                    werteLin = event.values.clone();
                    showLinAcc.setText(String.format("x: %s\ty: %s\tz: %s", Math.round(100.0 * werteLin[0]) / 100.0, Math.round(100.0 * werteLin[1]) / 100.0, Math.round(100.0 * werteLin[2]) / 100.0));
                    // Die lineare Beschleunigungswerte werden zur Auswertung im Logcat aufgezeichnet
                    Log.i(TAG, "lx: " + Math.round(100.0 * werteLin[0]) / 100.0 + " \tly: " + Math.round(100.0 * werteLin[1]) / 100.0 + " \tlz: " + Math.round(100.0 * werteLin[2]) / 100.0);

                } else {
                    showLinAcc.setText(String.format(""));
                }
            }

            if (event.sensor == gravity) {
                if (isRunningGrav) {
                    werteGrav = event.values.clone();
                    showGrav.setText(String.format("x: %s\ty: %s\tz: %s", Math.round(100.0 * werteGrav[0]) / 100.0, Math.round(100.0 * werteGrav[1]) / 100.0, Math.round(100.0 * werteGrav[2]) / 100.0));
                    // Die Gravitationswerte werden zur Auswertung im Logcat aufgezeichnet
                    Log.i(TAG, "grx: " + Math.round(100.0 * werteGrav[0]) / 100.0 + " \tgry: " + Math.round(100.0 * werteGrav[1]) / 100.0 + " \tgrz: " + Math.round(100.0 * werteGrav[2]) / 100.0);
                } else {
                    showGrav.setText(String.format(""));
                }
            }

            if (event.sensor == pressure) {
                if (isRunningPress) {
                    wertPress = event.values[0];
                    showPress.setText(String.format("%s", Math.round(100.0 * wertPress) / 100.0));
                    // Die Luftdruckwerte werden zur Auswertung im Logcat aufgezeichnet
                    Log.i(TAG, "Luftdruck: " + Math.round(100.0 * wertPress) / 100.0 );
                } else {
                    showPress.setText(String.format(""));
                }
            }

            if (event.sensor == proximity) {
               if (isRunningProximity) {
                   maxDistance = event.sensor.getMaximumRange();
                   float wertProx = event.values[0];
                   if (wertProx < maxDistance) { // Nah
                       distance = "Nah";
                       showProx.setText(R.string.Nah);
                       Log.i(TAG, "Nah");
                   } else { // Fern
                       distance = "Fern";
                       showProx.setText(R.string.Fern);
                       Log.i(TAG, "Fern");
                   }
               } else {
                   showProx.setText("");
                }
            }

            if (event.sensor == stepCounter) {
               if (isRunningStepcounter ) {
                   if (counterSteps < 1) {
                       counterSteps = (int) event.values[0];
                   } else {
                       stepcounter = (int) event.values[0] - counterSteps;
                   }
                   //showStepCount.setText(String.format("Anzahl Schritte: %d", stepcounter));
                   showStepCount.setText(String.format("%d", stepcounter));
                   // Die Schritte werden zur Auswertung im Logcat aufgezeichnet
                   Log.i(TAG, "Schritte: " + stepcounter);
               }else {
                   showStepCount.setText(String.format(""));
               }

            }

            /**
             * Lagesensor (Orientierungssensor)
             * berechnen, da TYPE_ORIENTATION veraltet ist
             */
        if (event.sensor == orientation) {
            if (isRunningOrient) {
                // Konstante vom Sensortyp ermitteln
                /*int sensorType = event.sensor.getType();
                switch (sensorType) { // Ermittlung der Beschleunigungs- und Magnetwerte,
                                      // die zur Berechnung der Rotationsmatrix genutzt dienen
                    case Sensor.TYPE_ACCELEROMETER:     accData = event.values.clone();
                                                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:    magData = event.values.clone();
                                                        break;
                    default:                            return;
                }*/

               // if (accData == null || magData == null) return;
                // Rotationsmatrix, die berechnet werden muss
                float[] rotationMatrix = new float[9];
                // Ermittlung der Orientierung des Geraetes
                // Rotationsmatrix, die die Sensordaten vom Geraete Koordinatensystem ins Erd
                // Koordinatensystem uebersetzt
                //boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix, null, accData, magData); /**  1. Variante
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);    // 2. Variante
                //Das neu justierte array bei eventuellen rotationen des Gerätes
                float[] rotationsMatrixAdjusted = new float[9];
                switch (display.getRotation()) { // Ermittelt die Konstante der aktuellen rotation
                    // In der Default Einstellung bleibt die Rotationsmatrix bestehen
                    case Surface.ROTATION_0:    rotationsMatrixAdjusted = rotationMatrix.clone();
                                                break;
                    /**
                     * Rotationsmatrix wird mit den Sensorwerten auf den neuen Achsen Korrekt belegt
                     */
                    case Surface.ROTATION_90:   SensorManager.remapCoordinateSystem(rotationMatrix,
                                                SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
                                                rotationsMatrixAdjusted);
                                                break;
                    case Surface.ROTATION_180:  SensorManager.remapCoordinateSystem(rotationMatrix,
                                                SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y,
                                                rotationsMatrixAdjusted);
                                                break;
                    case Surface.ROTATION_270:  SensorManager.remapCoordinateSystem(rotationMatrix,
                                                SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X,
                                                rotationsMatrixAdjusted);
                                                break;
                }

                // Ermittlung der Lagedaten
                float[] orientation = new float[3];
                /*if (rotationOK) { // falsch, wenn g nahe 0 ist oder das Gerät nahe am Nordpol ist
                                  // Sensordaten sind in dem Fall unzuverlässig
                    // Orientierungswinkel generieren, basoerend auf das Erd-Koordinatensystem
                    SensorManager.getOrientation(rotationsMatrixAdjusted, orientation);
                }*/
                SensorManager.getOrientation(rotationMatrix, orientation);
                // Umrechnen von Radiant in Grad
                az = (int) Math.toDegrees(orientation[0]); //Azimuth
                pi = (int) Math.toDegrees(orientation[1]); //Pitch
                ro = (int) Math.toDegrees(orientation[2]); //Roll

                showOrient.setText(String.format("Azimuth: %d\tPitch: %d\tRoll: %d", az, pi, ro));
                // Die Orientierungswerte werden zur Auswertung im Logcat aufgezeichnet
                Log.i(TAG, "Azimuth: " + az + " \tbPitch: " + pi + " \tbRoll: " + ro);

            } else {
                showOrient.setText(String.format(""));
            }
        }

        /**
         * Sensorwerte werden im 100 Hz Takt aufgelistet
         */
        if (time - fixedTime >= 10) { // Aktuelle Zeit groesser fixer Zeitstempel
                if (!(  compare(werteBes[0], 0.0f, 0.00001f) && compare(werteBes[1], 0.0f, 0.00001f)
                        && compare(werteBes[2], 0.0f, 0.00001f) && compare(werteGyro[0], 0.0f, 0.00001f)
                        && compare(werteGyro[1], 0.0f, 0.00001f) && compare(werteGyro[2], 0.0f, 0.00001f)
                        && compare(werteMag[0], 0.0f, 0.00001f) && compare(werteMag[1], 0.0f, 0.00001f)
                        && compare(werteMag[2], 0.0f, 0.00001f) && compare(werteLin[0], 0.0f, 0.00001f)
                        && compare(werteLin[1], 0.0f, 0.00001f) && compare(werteLin[2], 0.0f, 0.00001f)
                        &&  compare(werteGrav[0], 0.0f, 0.00001f) &&  compare(werteGrav[1], 0.0f, 0.00001f)
                        &&  compare(werteGrav[2], 0.0f, 0.00001f) &&  compare(wertPress, 0.0f, 0.00001f)
                        &&  distance.equals("") &&  compare(stepcounter, 0.0f, 0.00001f)
                        &&  compare(az, 0.0f, 0.00001f) &&  compare(pi, 0.0f, 0.00001f)
                        &&  compare(ro, 0.0f, 0.00001f))) {
            /*if((werteBes[0] == 0.0f) && (werteBes[1] == 0.0f) && (werteBes[2] == 0.0f) && (werteGyro[0]  == 0.0f)
                    && (werteGyro[1] == 0.0f) && (werteGyro[2] == 0.0f)
                    && (werteMag[1] == 0.0f) && (werteMag[2] == 0.0f)) { // ob alle Sensorwerte null sind
                    ++counter;
            }*/

                    Values v = new Values(werteBes[0], werteBes[1], werteBes[2], werteGyro[0], werteGyro[1], werteGyro[2],
                            werteMag[0], werteMag[1], werteMag[2], werteLin[0], werteLin[1], werteLin[2],
                            werteGrav[0], werteGrav[1], werteGrav[2], wertPress, distance, stepcounter,
                            az, pi, ro, fixedTime);
                    all.add(v);
                    fixedTime += 10;
                    ++countSensor;
                }
        }
    }

    @Override
    /**
     * Activity tritt in den Hintergrund.
     * Sensoren abmelden.
     */
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    /**
     * Activity tritt in den Vordergrund.
     * Sensoren anmelden.
     */
    protected void onResume() {
        super.onResume();

        if (acceleromter != null) {
            sensorManager.registerListener(this, acceleromter, SensorManager.SENSOR_DELAY_FASTEST);
        }  else {
            showAcc.setText(R.string.Vorhanden);
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            showGyro.setText(R.string.Vorhanden);
        }
        if (magnet != null) {
            sensorManager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            showMag.setText(R.string.Vorhanden);
        }

        if (stepCounter != null) {
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            showStepCount.setText(R.string.Vorhanden);
        }
        if (linAcceleromter != null) {
            sensorManager.registerListener(this, linAcceleromter, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            showLinAcc.setText(R.string.Vorhanden);
        }
        if (gravity != null) {
                sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_FASTEST);
            } else {
                showGrav.setText(R.string.Vorhanden);
        }
        if (pressure != null) {
            sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            showPress.setText(R.string.Vorhanden);
        }
        if (proximity != null) {
            sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            showProx.setText(R.string.Vorhanden);
        }
        if (orientation != null) {
            sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            showOrient.setText(R.string.Vorhanden);
        }
    }

    /**
     * Überprüft, ob die App Zugriffsrechte auf das Endgerät haben darf
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == 1)
        {
            int grantResultsLength = grantResults.length;
            if(grantResultsLength > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getApplicationContext(), "You grant write external storage permission. Please click original button again to continue.", Toast.LENGTH_LONG).show();
            }else
            {
                Toast.makeText(getApplicationContext(), "You denied write external storage permission.", Toast.LENGTH_LONG).show();
            }
        }
    }
}