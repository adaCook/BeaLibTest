package com.example.laicooper.bealibtest;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by laicooper on 6/6/2016.
 */
public class BeaconCollecAdapter extends BaseAdapter {

    public class BeaconComparator implements Comparator<Beacon>
    {


        @Override
        public int compare(Beacon beacon1, Beacon beacon2) {


            return beacon1.getBluetoothName().compareTo(beacon2.getBluetoothName());
        }

    }
    private ArrayList<Beacon> beacons;
    public BeaconCollecAdapter()
    {
        this.beacons=new ArrayList<>();
    }
    public void replaceWith(Collection<Beacon> beaCollect)
    {
        this.beacons.clear();
        this.beacons.addAll(beaCollect);
        Collections.sort(this.beacons,new BeaconComparator());
    }
    public  ArrayList<Beacon> getBeacons()
    {
        return this.beacons;
    }

    @Override
    public int getCount() {
        return this.beacons.size();
    }

    @Override
    public Beacon getItem(int position) {
        return beacons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
