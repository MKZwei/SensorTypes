package com.example.sensortypes;

public class Values {
    float ax = 0f;
    float ay = 0f;
    float az = 0f;
    float gx = 0f;
    float gy = 0f;
    float gz = 0f;
    float mx = 0f;
    float my = 0f;
    float mz = 0f;
    float lx = 0f;
    float ly = 0f;
    float lz = 0f;
    float grx = 0f;
    float gry = 0f;
    float grz = 0f;
    float press = 0f;
    String prox = "";
    int stepper = 0;
    int azimuth = 0;
    int pitch = 0;
    int roll = 0;
    long time = 0;

    Values(float ax, float ay, float az, float gx, float gy, float gz,
           float mx, float my, float mz, float lx, float ly, float lz,
           float grx, float gry, float grz, float press, String prox,
           int stepper, int azimuth, int pitch, int roll,long time) {
        this.ax = ax;
        this.ay = ay;
        this.az = az;
        this.gx = gx;
        this.gy = gy;
        this.gz = gz;
        this.mx = mx;
        this.my = my;
        this.mz = mz;
        this.lx = lx;
        this.ly = ly;
        this.lz = lz;
        this.grx = grx;
        this.gry = gry;
        this.grz = grz;
        this.press = press;
        this.prox = prox;
        this.stepper = stepper;
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
        this.time = time;
    }
}


