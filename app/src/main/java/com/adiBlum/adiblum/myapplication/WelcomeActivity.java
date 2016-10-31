package com.adiBlum.adiblum.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void saveUserSalaryData(View view) {
        SharedPreferences.Editor editor = getSharedPreferences(getApplicationContext()).edit();
        putHourSalary(editor);
        putDailySalary(editor);
        editor.apply();
        finish();
    }

    private void putDailySalary(SharedPreferences.Editor editor) {
        EditText dailySalary = (EditText) this.findViewById(R.id.dailySalary);
        if (dailySalary.getText().length() > 0) {
            System.out.println("dailySalary is: " + dailySalary.getText().toString());
            editor.putString("dailySalary", dailySalary.getText().toString());
        }
    }

    private void putHourSalary(SharedPreferences.Editor editor) {
        EditText hourSalary = (EditText) this.findViewById(R.id.hourSalary);
        if (hourSalary.getText().length() > 0) {
            System.out.println("hourSalary is: " + hourSalary.getText().toString());
            editor.putString("hourSalary", hourSalary.getText().toString());
        }
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
}
