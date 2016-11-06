package com.adiBlum.adiblum.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

public class SaveSalaryDataActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_salary);
        initTextViewsWithVals();
    }

    private void initTextViewsWithVals() {
        EditText hourSalaryEditText = (EditText) this.findViewById(R.id.hourSalary);
        hourSalaryEditText.setText(getSharedPreferences(getApplicationContext()).getString("hourSalary", "-1"));
        EditText dailySalaryEditText = (EditText) this.findViewById(R.id.dailySalary);
        dailySalaryEditText.setText(getSharedPreferences(getApplicationContext()).getString("dailySalary", "-1"));
    }

    public void saveUserSalaryData(View view) {
        SharedPreferences.Editor editor = getSharedPreferences(getApplicationContext()).edit();
        putHourSalary(editor);
        putDailySalary(editor);
        editor.apply();
        finish();
    }

    private void putDailySalary(SharedPreferences.Editor editor) {
        putSalary(editor, (EditText) this.findViewById(R.id.dailySalary), "dailySalary");
    }

    private void putSalary(SharedPreferences.Editor editor, EditText editText, String attributeName) {
        if (editText.getText().length() > 0) {
            editor.putString(attributeName, editText.getText().toString());
        }
        else {
            editor.putString(attributeName, "-1");
        }
    }

    private void putHourSalary(SharedPreferences.Editor editor) {
        EditText hourSalary = (EditText) this.findViewById(R.id.hourSalary);
        putSalary(editor, hourSalary, "hourSalary");
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
}
