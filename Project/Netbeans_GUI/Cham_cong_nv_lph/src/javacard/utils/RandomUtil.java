/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javacard.utils;

import java.util.Random;

/**
 *
 * @author Admin
 */
public class RandomUtil {

    public static byte[] randomData(int len) {
        byte[] b = new byte[len];
        new Random().nextBytes(b);
        return b;
    }
}
