package com.ovenbits.takekit;

import android.annotation.TargetApi;
import android.content.Context;
import android.nfc.cardemulation.HostApduService;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by Ehsan Barekati on 2/25/15.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class MyHostApduService extends HostApduService {
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String uid = tManager.getDeviceId();
        Log.e("Detected", uid);
        int i = 0;
        return uid.getBytes();
    }

    @Override
    public void onDeactivated(int reason) {

    }
}
