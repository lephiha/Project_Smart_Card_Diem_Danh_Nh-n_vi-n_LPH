/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cham_cong_nv_lph;

import java.io.Serializable;

/**
 *
 * @author Spark_Mac
 */
public class StockFile implements Serializable{
    private String dateString;
    private String startTimeString;
    private String endTimeString;
    public StockFile(String dateString,String startTimeString,String endTimeString){
        this.dateString = dateString;
        this.startTimeString = startTimeString;
        this.endTimeString = endTimeString;
    }
    @Override
    public String toString(){
        return dateString + " : " + startTimeString + " | " + endTimeString;
    }
}
