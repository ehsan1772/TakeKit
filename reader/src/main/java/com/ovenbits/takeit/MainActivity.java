package com.ovenbits.takeit;

import android.annotation.TargetApi;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;


@TargetApi(Build.VERSION_CODES.KITKAT)
public class MainActivity extends ActionBarActivity implements NfcAdapter.ReaderCallback {
    public static final String MIME_TEXT_PLAIN = "text/plain";
    private static final String TAG = "NFC READER ACTIVITY";
    private TextView text;
    private NfcAdapter nfcAdapter;
    //this indicates select application "00A40400 indicates 'SELECT APPLICATION'. Please refer ISO/IEC 7816 Part 4 "
    private static final byte[] CLA_INS_P1_P2 = { 0x00, (byte)0xA4, 0x04, 0x00 };
    //this is basically "F0010203040506"
    //read this for picking a proper name:
    //http://stackoverflow.com/questions/27877373/how-to-get-aid-for-reader-host-based-card-emulation
    private static final byte[] AID_ANDROID = { (byte)0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableReaderMode(this);
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        Log.d(TAG, "Tag Discovered");
        Worker worker = new Worker();
        worker.execute(tag);
    }

    private byte[] createSelectAidApdu(byte[] aid) {
        byte[] result = new byte[6 + aid.length];
        System.arraycopy(CLA_INS_P1_P2, 0, result, 0, CLA_INS_P1_P2.length);
        result[4] = (byte)aid.length;
        System.arraycopy(aid, 0, result, 5, aid.length);
        result[result.length - 1] = 0;
        return result;
    }

    private class Worker extends AsyncTask<Tag, String, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            IsoDep isoDep = IsoDep.get(tag);
            try {
                isoDep.connect();
                Log.d(TAG, "sending AID " + createSelectAidApdu(AID_ANDROID));
                  byte[] response = isoDep.transceive(createSelectAidApdu(AID_ANDROID));
                    Log.d("Tag is=", new String(response));
                    publishProgress(new String(response));
                isoDep.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "returning");
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            text.setText(values[0]);
            super.onProgressUpdate(values);
        }
    }

}
