package com.example.assignmentq2theme;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private EditText editTextInputValue;
    private Spinner spinnerFrom, spinnerTo;
    private TextView textViewResult, titleTextView;
    private Button buttonConvert;
    private ImageView imageSettings;
    private ConstraintLayout mainLayout;

    private String[] units = {"Feet", "Inches", "Centimeters", "Meters", "Yards"};
    private DecimalFormat df = new DecimalFormat("#.####");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI
        editTextInputValue = findViewById(R.id.editTextInputValue);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        textViewResult = findViewById(R.id.textViewResult);
        buttonConvert = findViewById(R.id.buttonConvert);
        imageSettings = findViewById(R.id.imageSettings);
        titleTextView = findViewById(R.id.titleTextView);
        mainLayout = findViewById(R.id.main_constraint_layout);

        // Retrieve saved theme and apply it
        String savedTheme = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getString("theme", "light"); // default to light
        applyTheme(savedTheme);

        // Spinner setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);
        spinnerFrom.setSelection(0);
        spinnerTo.setSelection(2);

        // Convert button logic
        buttonConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertUnits();
            }
        });

        // Clear result on empty input
        editTextInputValue.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    textViewResult.setText("0.0");
                }
            }
        });

        // Settings icon click opens ThemeActivity
        imageSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ThemeActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    // Handle theme result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String theme = data.getStringExtra("theme");
            applyTheme(theme);
            getSharedPreferences("AppPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("theme", theme)
                    .apply();
        }
    }

    // Updated Theme switching logic
    private void applyTheme(String theme) {
        View rootView = findViewById(android.R.id.content).getRootView();

        int bgColor, textColor, hintColor, buttonBgColor, buttonTextColor, cardColor;

        if (theme.equals("light")) {
            bgColor = ContextCompat.getColor(this, R.color.light_bg);
            textColor = ContextCompat.getColor(this, R.color.light_text);
            hintColor = ContextCompat.getColor(this, android.R.color.darker_gray);
            buttonBgColor = ContextCompat.getColor(this, R.color.button_light);
            buttonTextColor = ContextCompat.getColor(this, R.color.white);
            cardColor = ContextCompat.getColor(this, R.color.white);
        } else {
            bgColor = ContextCompat.getColor(this, R.color.dark_bg);
            textColor = ContextCompat.getColor(this, R.color.white);
            hintColor = ContextCompat.getColor(this, R.color.light_gray);
            buttonBgColor = ContextCompat.getColor(this, R.color.button_dark);
            buttonTextColor = ContextCompat.getColor(this, R.color.white);
            cardColor = ContextCompat.getColor(this, R.color.card_dark);
        }

        // Set background color for root view and main layout
        rootView.setBackgroundColor(bgColor);
        mainLayout.setBackgroundColor(bgColor);

        // Set text colors
        titleTextView.setTextColor(textColor);
        textViewResult.setTextColor(textColor);
        editTextInputValue.setTextColor(textColor);
        editTextInputValue.setHintTextColor(hintColor);

        // Set button colors
        buttonConvert.setBackgroundTintList(ColorStateList.valueOf(buttonBgColor));
        buttonConvert.setTextColor(buttonTextColor);

        // Update card backgrounds
        CardView fromCardView = findViewById(R.id.fromCard);
        CardView toCardView = findViewById(R.id.toCard);

        fromCardView.setCardBackgroundColor(cardColor);
        toCardView.setCardBackgroundColor(cardColor);

        // Find and update labels manually by ID
        try {
            TextView fromLabel = findViewById(R.id.labelFrom);
            TextView toLabel = findViewById(R.id.labelTo);

            if (fromLabel != null) {
                fromLabel.setTextColor(textColor);
            }

            if (toLabel != null) {
                toLabel.setTextColor(textColor);
            }
        } catch (Exception ignored) {
            // Ignore any errors if these IDs don't exist
        }

        // Update spinner adapters with new color
        updateSpinnerTheme(spinnerFrom, textColor);
        updateSpinnerTheme(spinnerTo, textColor);
    }

    // Helper method to update spinner text colors
    private void updateSpinnerTheme(Spinner spinner, int textColor) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Save current selection
        int position = spinner.getSelectedItemPosition();

        // Apply new adapter
        spinner.setAdapter(adapter);

        // Restore selection
        spinner.setSelection(position);
    }

    // Conversion logic
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

    // Convert input unit to meters
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

    // Convert from meters to target unit
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