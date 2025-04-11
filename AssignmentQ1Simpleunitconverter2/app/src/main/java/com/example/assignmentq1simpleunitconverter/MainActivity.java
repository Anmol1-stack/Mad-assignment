package com.example.assignmentq1simpleunitconverter;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private EditText editTextInputValue;
    private Spinner spinnerFrom, spinnerTo;
    private TextView textViewResult;
    private Button buttonConvert;

    private String[] units = {"Feet", "Inches", "Centimeters", "Meters", "Yards"};
    private DecimalFormat df = new DecimalFormat("#.####");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        editTextInputValue = findViewById(R.id.editTextInputValue);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        textViewResult = findViewById(R.id.textViewResult);
        buttonConvert = findViewById(R.id.buttonConvert);

        // Set up spinners with unit options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        // Default selections
        spinnerFrom.setSelection(0);
        spinnerTo.setSelection(2);

        // Button-only conversion
        buttonConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertUnits();
            }
        });

        // Watch for input cleared -> show 0.0
        editTextInputValue.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    textViewResult.setText("0.0");
                }
            }
        });
    }

    private void convertUnits() {
        String inputStr = editTextInputValue.getText().toString().trim();

        if (inputStr.isEmpty()) {
            textViewResult.setText("0.0");
            return;
        }

        try {
            double inputValue = Double.parseDouble(inputStr);
            String fromUnit = units[spinnerFrom.getSelectedItemPosition()];
            String toUnit = units[spinnerTo.getSelectedItemPosition()];

            double valueInMeters = convertToMeters(inputValue, fromUnit);
            double result = convertFromMeters(valueInMeters, toUnit);

            textViewResult.setText(df.format(result));

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            textViewResult.setText("Error");
        }
    }

    private double convertToMeters(double value, String fromUnit) {
        switch (fromUnit) {
            case "Feet": return value * 0.3048;
            case "Inches": return value * 0.0254;
            case "Centimeters": return value * 0.01;
            case "Meters": return value;
            case "Yards": return value * 0.9144;
            default: return 0;
        }
    }

    private double convertFromMeters(double meters, String toUnit) {
        switch (toUnit) {
            case "Feet": return meters / 0.3048;
            case "Inches": return meters / 0.0254;
            case "Centimeters": return meters / 0.01;
            case "Meters": return meters;
            case "Yards": return meters / 0.9144;
            default: return 0;
        }
    }
}