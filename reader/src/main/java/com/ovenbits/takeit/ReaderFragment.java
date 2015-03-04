package com.ovenbits.takeit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by Ehsan Barekati on 3/4/15.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class ReaderFragment extends Fragment implements NfcAdapter.ReaderCallback {
    private static final String TAG = "NFC READER ACTIVITY";
    private TextView text;
    //this indicates select application "00A40400 indicates 'SELECT APPLICATION'. Please refer ISO/IEC 7816 Part 4 "
    private static final byte[] CLA_INS_P1_P2 = {0x00, (byte) 0xA4, 0x04, 0x00};
    //this is basically "F0010203040506"
    //read this for picking a proper name:
    //http://stackoverflow.com/questions/27877373/how-to-get-aid-for-reader-host-based-card-emulation
    private static final byte[] AID_ANDROID = {(byte) 0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06};

    private TextView mMessage;
    private Spinner mSpinner;
    private ImageView mImage;
    private AnimationDrawable mNfcAnimation;
    private NfcAdapter nfcAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reader, container, false);
        mMessage = (TextView) view.findViewById(R.id.message);
        mSpinner = (Spinner) view.findViewById(R.id.spinner);
        mImage = (ImageView) view.findViewById(R.id.imageView);

        mNfcAnimation = (AnimationDrawable) mImage.getBackground();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNfcAnimation.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        nfcAdapter.enableReaderMode(getActivity(), this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null);
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableReaderMode(getActivity());
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
         //   text.setText(values[0]);
            mMessage.setText(mSpinner.getSelectedItem().toString() + " just checked out a device with this id: \n" + values[0]);
            super.onProgressUpdate(values);
        }
    }

}
