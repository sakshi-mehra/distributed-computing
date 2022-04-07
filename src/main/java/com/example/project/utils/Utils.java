package com.example.project.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * @author revanth on 4/4/22
 */
public class Utils {

    public static long getRandom(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static String getHostName() {

        Process hostname;
        try {
            hostname = Runtime.getRuntime().exec("hostname");
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(hostname.getInputStream()));

            StringBuilder stringBuilder = new StringBuilder();
            String s;

            while ((s = stdInput.readLine()) != null) {
                stringBuilder.append(s);
            }

            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
