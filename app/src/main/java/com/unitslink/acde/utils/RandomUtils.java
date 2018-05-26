package com.unitslink.acde.utils;

import java.util.Random;

/**
 * Created by gugia on 2018/2/7.
 */

public class RandomUtils {

    static Random random = new Random();

    public static int randomInt(int n) {
        return random.nextInt(n);
    }

    public static int randomInt(int min, int max) {
        return min + random.nextInt(max - min);
    }

    public static double randomLat() {
        return randomInt(90) + random.nextDouble();
    }

    public static double randomLon() {
        return randomInt(179) + random.nextDouble();
    }

    public static boolean randomBool() {
        return random.nextInt(100) % 2 == 0;
    }
}
