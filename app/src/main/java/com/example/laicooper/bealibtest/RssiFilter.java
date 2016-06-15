package com.example.laicooper.bealibtest;

import android.util.Log;

/**
 * Created by laicooper on 14/6/2016.
 */
public class RssiFilter {
    String TAG=new String("Filter");
    int beaNum,times;
    int[] ave=new int[10];
    int rssiValue[][];
    double power;
    double [] sqrtOfPower=new double[10];
    public RssiFilter(int value[][],int time,int number)
    {
        this.rssiValue=value;
        this.times=time;
        this.beaNum=number;
    }

    public void filteFunction()
    {
        int[] total=new int[10];
        for(int j=0;j<beaNum;j++)
        {
            total[j]=0;
            ave[j]=0;
            for(int b=1;b<times;b++)
             {
                if(rssiValue[j][b-1]>rssiValue[j][b])
                {
                    int temp=rssiValue[j][b];
                    int num=b;
                    do {
                        rssiValue[j][num]=rssiValue[j][num-1];
                        num--;
                    }while(num>0&&rssiValue[j][num-1]>temp);
                    rssiValue[j][num]=temp;
                }

             }
        //中值平均滤波算法
        for(int a=2;a<(times-2);a++) {
            total[j] += rssiValue[j][a];

            Log.i(TAG,j + " beacon has such  value :" +
                    rssiValue[j][a]);
        }
        ave[j]=(int)total[j]/(times-4);
        power=Math.pow(10.0, ave[j] / 10.0);
        sqrtOfPower[j]=Math.sqrt(1 / power);
//                spinnerItem[j]=new String(beaconAdapter.getItem(j).getBluetoothName());

//                Log.i(TAG, spinnerItem[j]+" beacon receives power value: " + Double.toString(power)+
//                        "and sqrt of power: \n" +Double.parseDouble(String.format("%.2f",sqrtofPower)));
    }

}
    public int[] getAve()
    {
        return ave;
    }
    public double[] getSqrtofPower()
    {
        return sqrtOfPower;
    }
}
