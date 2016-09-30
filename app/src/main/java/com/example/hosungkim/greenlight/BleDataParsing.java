package com.example.hosungkim.greenlight;

import android.util.Log;

/**
 * Created by HosungKim on 2016-08-13.
 */

public class BleDataParsing {
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static int parseScanRecord(byte[] scanRecord){
        int startByte = 2;
        boolean patternFound = false;
        int major = 0;
        int minor = 0;
        byte txPower = 0;

        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 &&           //Identifies an iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) {       //Identifies correct data length
                patternFound = true;
                break;
            }
            startByte++;
        }

        if (patternFound) {
            byte[] uuidBytes = new byte[16];
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
            String hexString = bytesToHex(uuidBytes);
            String hexRecord = bytesToHex(scanRecord);
            String uuid =  hexString.substring(0,8) + "-" +
                    hexString.substring(8,12) + "-" +
                    hexString.substring(12,16) + "-" +
                    hexString.substring(16,20) + "-" +
                    hexString.substring(20,32);
            major = (scanRecord[startByte+20] & 0xff) * 0x100 + (scanRecord[startByte+21] & 0xff);
            minor = (scanRecord[startByte+22] & 0xff) * 0x100 + (scanRecord[startByte+23] & 0xff);

            txPower = scanRecord[startByte+24];
            Log.d("ScanRecord", " uuid: " + uuid + "  major: " + major + "  minor: " + minor + "txPoxer: " + txPower);
        }
        return major;
    }


    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];

        }
        return new String(hexChars);
    }
    public static double proximity(int txPower, double rssi) {
        if(rssi == 0) {
            return -0.1;
        }
        double ratio = rssi*1.0/txPower;
        if (ratio <1.0) {
            return  Math.pow(ratio, 10);
        }
        else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }
}
