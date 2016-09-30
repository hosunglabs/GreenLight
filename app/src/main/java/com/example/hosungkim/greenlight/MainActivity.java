package com.example.hosungkim.greenlight;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String RED = "빨간불 입니다";
    private static final String GREEN = "녹색불 입니다";
    private static final int REQUEST_ENABLE_BT = 1;

    private TextToSpeech tts;
    private BluetoothAdapter bluetoothAdapter;
    private TimerTask timerTask;
    private Timer timer;
    private Vibrator vibrator;

    private RelativeLayout background;
    private Button btnNotify;
    private TextView message_signal;
    private TextView message_second;

    private int second = 0;
    private int old_second = 0;
    private boolean signal = false;
    private boolean ble = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        background = (RelativeLayout) findViewById(R.id.background);
        btnNotify = (Button) findViewById(R.id.btnNotify);
        message_signal = (TextView) findViewById(R.id.message_signal);
        message_second = (TextView) findViewById(R.id.message_second);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tts = new TextToSpeech(this, this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void isNotify(View v){
        if(ble == false) {
            bluetoothAdapter.stopLeScan(mLeScanCallback);
            btnNotify.setText("켜기");
            ble = true;
        }
        else {
            bluetoothAdapter.startLeScan(mLeScanCallback);
            btnNotify.setText("끄기");
            ble = false;
        }
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts. setLanguage(Locale.KOREA);

            if(result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            }
        }
        else {
            Log.e("TTS", "Init Failed");

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!bluetoothAdapter.isEnabled()) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        bluetoothAdapter.startLeScan(mLeScanCallback);

        timerTask = new TimerTask() {
            @Override
            public void run() {
            }
        };

        timer = new Timer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    @Override
    protected void onDestroy() {

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void speakOut(String message) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                    final int second = BleDataParsing.parseScanRecord(scanRecord) - 1;

                    if(device.getAddress().equals("74:DA:EA:B2:95:21")) {
                        Log.d("test", "" + second);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (second == 0) {
                                    if (signal == true) {
                                        background.setBackgroundResource(R.drawable.red);
                                        message_signal.setText(RED);
                                        message_second.setText("...");
                                        vibrator.vibrate(new long[]{0, 300, 300, 700}, -1);
                                        speakOut(RED);
                                        signal = false;
                                    }

                                } else {
                                    if (signal == false) {
                                        background.setBackgroundResource(R.drawable.green);
                                        message_signal.setText(GREEN);
                                        vibrator.vibrate(700);
                                        speakOut(GREEN);
                                        signal = true;
                                    }
                                    message_second.setText(second + "초 남았습니다");

                                    if (second % 3 == 0 && second != old_second) {
                                        speakOut(String.valueOf(second) + "초");
                                    }

                                    old_second = second;
                                }
                            }
                        });
                    }
                }
            };
}