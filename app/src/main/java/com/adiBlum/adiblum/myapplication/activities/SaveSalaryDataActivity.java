package com.adiBlum.adiblum.myapplication.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.adiBlum.adiblum.myapplication.R;

public class SaveSalaryDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.set_salary);
        initTextViewsWithVals();

        ActionBar myChildToolbar = getSupportActionBar();
        if (myChildToolbar == null) {
            System.out.println("adi - null ");
        } else {
            myChildToolbar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initTextViewsWithVals() {
        EditText hourSalaryEditText = (EditText) this.findViewById(R.id.hourSalary);
        hourSalaryEditText.setText(getSharedPreferences(getApplicationContext()).getString("hourSalary", "0"));
        EditText dailySalaryEditText = (EditText) this.findViewById(R.id.dailySalary);
        dailySalaryEditText.setText(getSharedPreferences(getApplicationContext()).getString("dailySalary", "0"));
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
        } else {
            editor.putString(attributeName, "0");
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
