package com.pham.richard.bulletinboard;

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

    private String getRelevantTimeDiff(String timeStamp){
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        targetFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date timestampDate;
        //parse date.
        //this can easily throw a ParseException so you should probably catch that
        try {
            timestampDate = targetFormat.parse(timeStamp);
            timeStamp = output.format(timestampDate);
            return timeStamp;
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

        return "Error";
	/*
	//Then just calculate the difference between the dates (remember dates are in
	//milliseconds, so divide by 1000 at some point) and do some math and stuff
	//to figure out what to display as the human readable time difference
	*/
    }
    public String getTimedMessage() {
        ts = getRelevantTimeDiff(ts);
        return ts + " " + msg;
    }
}