package com.example.laicooper.bealibtest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer{

    protected static final String TAG = "RangingActivity";
    protected TextView statusView;
    String[] spinnerItem;
    int waitSec = 10;
    BufferedWriter bw;
    BufferedWriter bw2;
    writeData wd;
    //    ArrayList<Beacon> beaconItem=new ArrayList<>();
    boolean canShowRssi = false;
    private BeaconManager beaconManager;
    private Button butStartRssi, butStartRecord, butStartWrite;
    private Spinner spinnerForMac;
    private TextView textRssi;
    private BeaconCollecAdapter beaconAdapter;
    private int times = 0;
    private int[][] rssiValue;
    private int [] ave=new int [10];
    private double[][] distance = new double[10][20];
    private double[] disAve = new double[10];
    private double sqrtofPower[];
    private int weightedNum = 0;
    private int[][] weightedAve = new int[3][];
    private double[][] weightedPow = new double[3][];
    private double[][] weightedDis = new double[3][];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        butStartRssi=(Button)findViewById(R.id.butRssiStart);
        butStartRecord = (Button) findViewById(R.id.butRecord);
        butStartWrite = (Button) findViewById(R.id.butWrite);
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
        rssiValue = new int[10][20];
        try {


            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsoluteFile() + "/MyBeaconFiles");
            directory.mkdirs();
            File file = new File(directory, "beacoAve.txt");
            File file2 = new File(directory, "beacoRaw.txt");

            FileOutputStream fout = new FileOutputStream(file);
            FileOutputStream fout2 = new FileOutputStream(file2);

            OutputStreamWriter osw = new OutputStreamWriter(fout);
            OutputStreamWriter osw2 = new OutputStreamWriter(fout2);

            bw = new BufferedWriter(osw);
            bw2 = new BufferedWriter(osw2);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
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
                if (beacons.size() > 1) {
//                    for(Beacon beacon:beacons)
//                    Log.i(TAG, "The first beacon I see is about " + beacon.getBluetoothName()+" has rssi: " +
//                            beacon.getRssi());

                    if (times < 10) {
                        beaconAdapter.replaceWith(beacons);

                        for (int j = 0; j < beaconAdapter.getCount(); j++) {

                            rssiValue[j][times] = beaconAdapter.getItem(j).getRssi();
                            distance[j][times] = beaconAdapter.getItem(j).getDistance();
                            Log.i(TAG, beaconAdapter.getItem(j).getBluetoothName() + "  " + times + " time " +
                                    "rssi value and distance: \n" + rssiValue[j][times] + "  " + distance[j][times]);
                        }
                        times++;
                    }

                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (times < 10)
                            statusView.setText("the number of beacons: " + beacons.size() + " still scanning");

                        else
                            statusView.setText("the number of beacons: " + beacons.size() + " can compute rssi");


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



        if(times<10)
            Toast.makeText(this, "Still recording. Wait Please", Toast.LENGTH_SHORT).show();
        else if (weightedNum < 3)
            {
                RssiFilter rf = new RssiFilter(rssiValue, distance, times, beaconAdapter.getCount());
                wd = new writeData(bw2, rssiValue, spinnerItem, beaconAdapter.getCount(), weightedNum);
                wd.writeRawData();
                rf.filteFunction();
                ave = rf.getAve();
                sqrtofPower = rf.getSqrtofPower();
                disAve = rf.getDisAve();
                for (int a = 0; a < beaconAdapter.getCount(); a++) {
                    Log.i(TAG, beaconAdapter.getItem(a).getBluetoothName() + " beacon receives average" +
                            " RSSI value and distance: \n" + ave[a]
                            + "  " + String.format("%.2f", sqrtofPower[a]) + "  " + String.format("%.2f", disAve[a]));
                }
                weightedAve[weightedNum] = ave;
                weightedPow[weightedNum] = sqrtofPower;
                weightedDis[weightedNum] = disAve;
                weightedNum++;
                Toast.makeText(this, "Computing  weighted value, the weighted order: " + weightedNum, Toast.LENGTH_LONG).show();
            } else {
            for (int j = 0; j < beaconAdapter.getCount(); j++) {
                ave[j] = (weightedAve[0][j] * 1 + weightedAve[1][j] * 2 + weightedAve[2][j] * 3) / 6;
                sqrtofPower[j] = (weightedPow[0][j] * 1 + weightedPow[1][j] * 2 + weightedPow[2][j] * 3) / 6;
                disAve[j] = (weightedDis[0][j] * 1 + weightedDis[1][j] * 2 + weightedDis[2][j] * 3) / 6;
                Log.i(TAG, beaconAdapter.getItem(j).getBluetoothName() + " beacon receives weighted average" +
                        " RSSI value and distance: \n" + ave[j]
                        + "  " + String.format("%.2f", sqrtofPower[j]) + "  " + String.format("%.2f", disAve[j]));
            }
            weightedNum = 0;
            Toast.makeText(this, "Finish weighted filtering", Toast.LENGTH_LONG).show();

        }
            canShowRssi=true;
//            textRssi.setText("the first iBeacon average value: "+ave[0]);
            times=0;



    }

    public void startRecord(View view) {
        Toast.makeText(this, "start recording", Toast.LENGTH_LONG).show();

        times = 0;

    }

    public void startWrite(View view) {
        try {

//            File sdCard= Environment.getExternalStorageDirectory();
//            File directory=new File (sdCard.getAbsoluteFile()+"/MyBeaconFiles");
//            directory.mkdirs();
//            File file=new File(directory,"beacoAve.txt");
//            FileOutputStream fout=new FileOutputStream(file);
//            OutputStreamWriter osw=new OutputStreamWriter(fout);
//            BufferedWriter bw=new BufferedWriter(osw);
            bw.write(Integer.toString(weightedNum));
            bw.newLine();
            for (int k = 0; k < 3; k++) {
                bw.write(spinnerItem[k]);
                bw.write("  ");
                bw.write(Integer.toString(ave[k]));
                bw.write("  ");
                bw.write(String.format("%.2f", sqrtofPower[k]));
                bw.write("  ");
                bw.write(String.format("%.2f", disAve[k]));
                bw.newLine();
            }
            bw.flush();

            Toast.makeText(getApplicationContext(), "Write finished", Toast.LENGTH_LONG).show();


        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    public void closeWrite(View v) {
        try {
            bw.flush();
            bw.close();
            bw2.flush();
            bw2.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
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
