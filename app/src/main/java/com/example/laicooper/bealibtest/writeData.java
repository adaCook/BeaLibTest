package com.example.laicooper.bealibtest;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by laicooper on 20/6/2016.
 */
public class writeData {
    BufferedWriter bw;
    int[][] rssi;
    int number;
    String[] item;
    int weighted;


    public writeData(BufferedWriter bw, int[][] r, String[] item, int num, int weighted) {
        this.bw = bw;
        this.rssi = r;
        this.item = item;
        this.number = num;
        this.weighted = weighted;
    }

    public void writeRawData() {
        try {

            bw.write(Integer.toString(weighted));
            bw.newLine();
            for (int i = 0; i < number; i++) {
                for (int j = 0; j < 10; j++) {
                    bw.write(item[i]);
                    bw.write(" : ");
                    bw.write(Integer.toString(rssi[i][j]));
                    bw.newLine();
                }
                bw.flush();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }
}