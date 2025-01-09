/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javacard.utils;

import com.sun.javafx.image.BytePixelGetter;

/**
 *
 * @author Spark_Mac
 */
public class ConvertData {
    public  static byte[] stringToByteArray(String string){
        char[] chars = string.toCharArray();
        return charArrayToByteArray(chars);
    }
    
    public static byte[] charArrayToByteArray(char[] chars){
        int len = chars.length;
        byte[] bytes = new byte[len];
        for(int ipIndx = 0;ipIndx<chars.length;ipIndx++){
            bytes[ipIndx] = (byte) (chars[ipIndx] & 0x0F);
        }
        return  bytes;
    }
    public static boolean isByteArrayAllZero(byte[] array) {
       for(byte b : array){
           if(b != 0){
               return false;
           }
       }
       return true;
    }
}
