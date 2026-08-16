package com.example.unyieldingmight;

public class Conversion {
    public static float rounded(float f) {
        return Math.round(f * 100.0f) / 100.0f;
    }
    public static float INtoCM(float val) {
        return rounded(val / 2.54f);
    }
    public static float FTtoCM(float val) {
        return rounded(val * 30.48f);
    }
    public static float LBtoKG(float val) { return rounded(val / 2.205f); }
}
