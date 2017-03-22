package com.adiBlum.adiblum.myapplication.helpers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.adiBlum.adiblum.myapplication.model.AllLoginData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SaveDataHelper {

    private static String FILENAME = "myHours";

    public static void addToFile(AllLoginData allLoginData, Context ctx) {
        FileOutputStream fos;
        try {
            fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(allLoginData);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("failed to write to file");
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

    public static AllLoginData getDataFromFile(Context ctx) throws IOException, ClassNotFoundException {
        File f = new File(FILENAME);
        if(f.exists())
        {
            FileInputStream fis = ctx.openFileInput(FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            AllLoginData allLoginData = (AllLoginData) is.readObject();
            is.close();
            fis.close();
            return allLoginData;
        }
        else {
            AllLoginData allLoginData = new AllLoginData();
            addToFile(allLoginData, ctx);
            return allLoginData;
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
    public static String getPrettyTimeString(Double timeSpentAtWork) {
        if (timeSpentAtWork == null) {
            return "No data";
        }
        if (timeSpentAtWork > 0) {
            int hours = (int) (timeSpentAtWork / 60 / 60);
            int minutes = (int) ((timeSpentAtWork / 60) % 60);
            return hours + "h" + " " + minutes + "m";
        } else if (timeSpentAtWork == 0) {
            return "Out of work";
        } else return "No data";
    }
}
