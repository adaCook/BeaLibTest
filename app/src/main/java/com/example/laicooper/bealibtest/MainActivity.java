package com.example.laicooper.bealibtest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer{

    protected static final String TAG = "RangingActivity";
    protected int i=0;
    private BeaconManager beaconManager;
    protected TextView statusView;
    private Button butStartRssi;
    private Spinner spinnerForMac;
    private TextView textRssi;
    private BeaconCollecAdapter beaconAdapter;
    private static int times=0;
    private int [][] rssiValue=new int [10][10];
    private int [] ave=new int [10];
    String [] spinnerItem;
    int waitSec=10;

//    ArrayList<Beacon> beaconItem=new ArrayList<>();
    boolean canShowRssi=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        butStartRssi=(Button)findViewById(R.id.butRssiStart);
        spinnerForMac=(Spinner)findViewById(R.id.spinnerMac);
        verifyBluetooth();//notify if the moblile device dose not open bluetooth
        this.statusView=(TextView)findViewById(R.id.statusView);
        textRssi=(TextView)findViewById(R.id.textRssi);
//        this.beaconAdapter=new BeaconCollecAdapter();
        this.beaconAdapter=new BeaconCollecAdapter();
        beaconManager=BeaconManager.getInstanceForApplication(this);
        beaconManager.setForegroundScanPeriod(1000l);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19," +
                "i:20-21,i:22-23,p:24-24,d:25-25       "));
        Beacon.setHardwareEqualityEnforced(true);//区别有共同UUID值的设备
        beaconManager.bind(this);
        spinnerItem=getResources().getStringArray(R.array.macAddress);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.dropdown_item, spinnerItem);
        spinnerForMac.setAdapter(adapter);
        spinnerForMac.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0,
                                       View arg1, int arg2, long arg3) {
                if(!canShowRssi)
                    textRssi.setText("still recording");
                else {
                    int index = arg0.getSelectedItemPosition();
                    textRssi.setText("ibeacon name: " +spinnerItem[index]+
                            " has rssi " + ave[index]);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");

            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon !");

            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });
        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {   }

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
//                    for(Beacon beacon:beacons)
//                    Log.i(TAG, "The first beacon I see is about " + beacon.getBluetoothName()+" has rssi: " +
//                            beacon.getRssi());
                    if(waitSec>0)
                    waitSec--;
                    else if (times < 10) {
                        beaconAdapter.replaceWith(beacons);

                        for (int j = 0; j < beaconAdapter.getCount(); j++) {

                            rssiValue[j][times] = beaconAdapter.getItem(j).getRssi();
                            Log.i(TAG,beaconAdapter.getItem(j).getBluetoothName()+"  "+times+" time " +
                                    "rssi value: "+rssiValue[j][times]);
                        }
                        times++;
                    }

                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(times<10)
                        statusView.setText("the number of beacons: " + beacons.size( )+" still scanning");

                        else
                            statusView.setText("the number of beacons: " + beacons.size( )+" can compute rssi");


                    }
                });
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        }catch (RemoteException e){}
    }
    public void startMeasuring(View view)
    {
        double sqrtofPower[];


        if(times<10)
            Toast.makeText(this, "Still recording. Wait Please", Toast.LENGTH_SHORT).show();
        else
        {
            RssiFilter rf=new RssiFilter(rssiValue,times,beaconAdapter.getCount());
            rf.filteFunction();
            ave=rf.getAve();
            sqrtofPower=rf.getSqrtofPower();
            for(int j=0;j<beaconAdapter.getCount();j++)
            {

                Log.i(TAG,  beaconAdapter.getItem(j).getBluetoothName()+" beacon receives average RSSI value: \n"  + ave[j]
                       +"  "+String.format("%.2f",sqrtofPower[j] ));
            }

            canShowRssi=true;
//            textRssi.setText("the first iBeacon average value: "+ave[0]);
            times=0;


        }
    }

//notify if you forget to open your bluetooth
    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        } catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();

        }
    }
}
