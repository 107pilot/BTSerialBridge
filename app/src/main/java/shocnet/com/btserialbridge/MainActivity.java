package shocnet.com.btserialbridge;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import shocnet.com.btserialbridge.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity {
    TextView out;
    TextView out2;
    Button startButton;
    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter btAdapter = null;
    BluetoothSocket btSocket = null;
    OutputStream outStream = null;
    static final UUID MY_UUID =     // Well known SPP UUID
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.button);
        out = (TextView) findViewById(R.id.out);
        out2 = (TextView) findViewById(R.id.out2);
        out.append("\n...In onCreate()...");
        btAdapter = BluetoothAdapter.getDefaultAdapter();
      //  AlertBox("Startup Hold", "Proceed to CheckBTState()");
        CheckBTState();
    }

    @Override
    public void onStart() {
        super.onStart();
        out.append("\n...In onStart()...");
    }

    @Override
    public void onResume() {
        super.onResume();
        //CheckBTState();
        out.append("\n...In onResume...\n...Attempting client connect...");

        // Set up a pointer to the remote node using it's address.
       // String address = "ac:0d:1b:3d:a5:39";
        final String address = "00:12:08:21:04:87";

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            AlertBox("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try {
            btSocket.connect();
            out.append("\n...Connection established and data link opened...\n");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                AlertBox("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a data stream so we can talk to server.
               // out.append("\n...Sending message to server...");
                try {
                    outStream = btSocket.getOutputStream();
                } catch (IOException e) {
                    AlertBox("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
                }

                String message = "BUMP IT YEAH YEAH!\n";
                out.setText(message);
                byte[] msgBuffer = message.getBytes();
                try {
                    outStream.write(msgBuffer);
                } catch (IOException e) {
                    String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                    if (address.equals("00:00:00:00:00:00"))
                        msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
                    msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

                    AlertBox("Fatal Error", msg);
                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();

        out.append("\n...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                AlertBox("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try {
            btSocket.close();
        } catch (IOException e2) {
            AlertBox("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        out.append("\n...In onStop()...");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        out.append("\n...In onDestroy()...");
    }

    private void CheckBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            out2.setText("BT not supported.");
            AlertBox("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else if(btAdapter.isEnabled()) {
            out2.setText("Bluetooth is enabled");
        } else {
            out2.setText("Bluetooth is disabled. Click HERE to enable");
          //  out2.setOnClickListener(new View.OnClickListener() {
          //      @Override
          //      public void onClick(View v) {
                  enableBT();
          //      }
          //  });
        }
    }

    public void enableBT(){
        //Prompt user to turn on Bluetooth
        Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
        //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        startActivity(enableBtIntent);
    }

    public void AlertBox(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message + " Press OK to exit.")
                .setPositiveButton("OK", new OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                }).show();
    }
}
