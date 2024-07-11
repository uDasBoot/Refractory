package dev.ycihasmear.refractory.util;

public class ModMathHelpers {
    public static int getScaledHeight(int height, int x, int maxX) {
        float percent = (float) x / (float) maxX;
        return (int) (height * percent);
    }
}
