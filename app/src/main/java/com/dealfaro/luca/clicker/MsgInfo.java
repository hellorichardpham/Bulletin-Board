package com.dealfaro.luca.clicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Richard W Pham on 4/26/2015.
 */

public class MsgInfo {
    String msg;
    String longitude;
    String latitude;
    String msgid;
    String app_id;
    String userid;
    String ts;
    public MsgInfo() {}

    private String getRelevantTimeDiff(String ts){
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        targetFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date timestampDate;

        //parse date.
        //this can easily throw a ParseException so you should probably catch that
        try {
            timestampDate = targetFormat.parse("2015-04-28T03:53:45.426040");
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        //Get current UTC time
        Date now = new Date();

	/*
	//Then just calculate the difference between the dates (remember dates are in
	//milliseconds, so divide by 1000 at some point) and do some math and stuff
	//to figure out what to display as the human readable time difference
	*/
    return "Error";
    }
    public String getTimedMessage() {
        //ts = getRelevantTimeDiff(ts);
        return ts + " " + msg;
    }
}