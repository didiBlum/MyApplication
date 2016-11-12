package com.adiBlum.adiblum.myapplication.helpers;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SaveDataHelper {

    private static String FILENAME = "myHours";

    public static void addToFile(String s, Context ctx) {
        try {
            FileOutputStream fos = ctx.openFileOutput(FILENAME, Context.MODE_APPEND);
            fos.write(s.getBytes());
            fos.close();
            System.out.println("saved to file: " + s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getStringDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private static String readFile(Context ctx) throws IOException {
//        System.out.println("dir is : " + ctx.getFilesDir());
        FileInputStream fileInputStream = ctx.openFileInput(FILENAME);
        InputStreamReader isr = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static Map<String, Double> getDataFromFile(Context ctx) {
        Map<String, Double> stringDoubleHashMap = new HashMap<>();
        String readFile;
        try {
            readFile = readFile(ctx);
            return parseFile(readFile);
        } catch (IOException e) {
            System.out.println("unable to read file");
            return stringDoubleHashMap;
        }

    }

    private static Map<String, Double> parseFile(String readFile) {
        Map<String, Double> stringDoubleHashMap = new HashMap<>();
        String regularExpression = ";";
        String[] strings = readFile.split(regularExpression);
        for (String s : strings) {
            String[] parts = s.split(":");
            stringDoubleHashMap.put(parts[0], Double.parseDouble(parts[1]));
        }
        return stringDoubleHashMap;
    }

    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    @NonNull
    public static String getPrettyTimeString(double timeSpentAtWork) {
        if (timeSpentAtWork > 0) {
            int hours = (int) (timeSpentAtWork / 60 / 60);
            int minutes = (int) ((timeSpentAtWork / 60) % 60);
            return hours + "h" + " " + minutes + "m";
        } else if (timeSpentAtWork == 0) {
            return "Out of work";
        } else return "No data";
    }
}
