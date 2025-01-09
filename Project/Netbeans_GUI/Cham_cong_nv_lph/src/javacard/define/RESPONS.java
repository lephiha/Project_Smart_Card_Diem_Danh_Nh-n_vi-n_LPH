/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javacard.define;

/**
 *
 * @author Spark_Mac
 */
public class RESPONS {
    
    public final static String  SW_NO_ERROR = "9000";
    public final static String  SW_UNKNOWN = "6F00";
    
    public final static String  SW_INVALID_PARAMETER = "9C0F";
    //card block
    public final static String  SW_IDENTITY_BLOCKED = "9C0C";
    //error pin
    public final static String  SW_AUTH_FAILED = "9C02";
    //
    public final static String  SW_OPERATION_NOT_ALLOWED = "9C03";
    // Kiểm soát lỗi
    public final static String SW_INTERNAL_ERROR = "9CFF";
    // tr li 9c04 khi th cha c setup */
	 public final static String SW_SETUP_NOT_DONE = "9C04";
	// Li P1*/
	 public final static String SW_INCORRECT_P1 = "9C10";
	//** Li P2*/
	 public final static String SW_INCORRECT_P2 = "9C11";
	//** Required operation was not authorized because of a lack of privileges */
	 public final static String SW_UNAUTHORIZED = "9C06";
	//** Algorithm specified is not correct */
	 public final static String SW_INCORRECT_ALG = "9C09";
         //Loi do dai
         public final static String SW_WRONG_LENGTH = "6700";
}
