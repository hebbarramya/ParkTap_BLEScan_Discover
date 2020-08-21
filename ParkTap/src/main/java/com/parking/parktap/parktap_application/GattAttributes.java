package com.parking.parktap.parktap_application;

import java.util.HashMap;

public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
//    public static String PARKTAP_SERVICE_ID="80808000-0000-0000-1000-000000000000";
//    public static  String PARKTAP_CHARACTERISTIC_ID="11111111-0100-0000-0000-000000000001";



    public static String PARKTAP_SERVICE_ID="6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static  String PARKTAP_CHARACTERISTIC_ID="6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static  String  CHARACTERISTIC_READ_UUID="6e400002-b5a3-f393-e0a9-e50e24dcca9e";

    static
    {
        attributes.put(PARKTAP_SERVICE_ID, "ParkTap Service");
    }



    public static String Gattvalues(String uuid,String device_default_name){
        String devicename=attributes.get(uuid);
        return devicename==null?device_default_name:devicename;
    }


}
