package com.ovenbits.takekit;

import android.annotation.TargetApi;
import android.nfc.cardemulation.HostApduService;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Ehsan Barekati on 2/25/15.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class MyHostApduService extends HostApduService {
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Log.e("Detected", "Helloooooooo!!!");
        int i = 0;
        return "Hello Worldddddd".getBytes();
    }

    @Override
    public void onDeactivated(int reason) {

    }
}
