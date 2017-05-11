/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hanulhan.jms.spring.util;

import java.util.Random;

/**
 *
 * @author uhansen
 */
public class RandomUtils {

    public static long getRandomLong(long min, long max) {
        long maxMinSubtraction = max - min + 1;
        return (long) (Math.random() * maxMinSubtraction) + min;
    }

    public static int getRandomInt(int min, int max) {
        int maxMinSubtraction = max - min + 1;
        return (int) (Math.random() * maxMinSubtraction) + min;
    }

    private String createRandomString() {
        Random random;
        random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }
}
