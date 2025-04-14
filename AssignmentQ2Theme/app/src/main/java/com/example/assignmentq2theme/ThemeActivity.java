package com.example.assignmentq2theme;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ThemeActivity extends AppCompatActivity {
    Button btnLight, btnDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_theme);

        btnLight = findViewById(R.id.btnLight);
        btnDark = findViewById(R.id.btnDark);

        // Get current theme to show correct initial state
        String currentTheme = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getString("theme", "light");
        updateThemeUI(currentTheme);

        btnLight.setOnClickListener(view -> applyThemeAndReturn("light"));
        btnDark.setOnClickListener(view -> applyThemeAndReturn("dark"));
    }

    private void updateThemeUI(String theme) {
        View root = getWindow().getDecorView().getRootView();

        int bgColor, textColor;

        if (theme.equals("light")) {
            bgColor = ContextCompat.getColor(this, R.color.light_bg);
            textColor = ContextCompat.getColor(this, R.color.light_text);
        } else {
            bgColor = ContextCompat.getColor(this, R.color.dark_bg);
            textColor = ContextCompat.getColor(this, R.color.dark_text);
        }

        // Apply theme to this activity
        root.setBackgroundColor(bgColor);
    }

    private void applyThemeAndReturn(String theme) {
        View root = getWindow().getDecorView().getRootView();

        int bgColor, textColor, buttonLightBg, buttonDarkBg;

        if (theme.equals("light")) {
            bgColor = ContextCompat.getColor(this, R.color.light_bg);
            textColor = ContextCompat.getColor(this, R.color.light_text);
            buttonLightBg = ContextCompat.getColor(this, R.color.button_light);
            buttonDarkBg = ContextCompat.getColor(this, R.color.button_dark);
        } else {
            bgColor = ContextCompat.getColor(this, R.color.dark_bg);
            textColor = ContextCompat.getColor(this, R.color.dark_text);
            buttonLightBg = ContextCompat.getColor(this, R.color.button_light);
            buttonDarkBg = ContextCompat.getColor(this, R.color.button_dark);
        }

        // Apply theme to this activity
        root.setBackgroundColor(bgColor);

        // Update button appearances
        btnLight.setTextColor(theme.equals("light") ? ContextCompat.getColor(this, R.color.white) : textColor);
        btnDark.setTextColor(theme.equals("dark") ? ContextCompat.getColor(this, R.color.white) : textColor);

        btnLight.setBackgroundTintList(ColorStateList.valueOf(buttonLightBg));
        btnDark.setBackgroundTintList(ColorStateList.valueOf(buttonDarkBg));

        // Send result and finish
        Intent resultIntent = new Intent();
        resultIntent.putExtra("theme", theme);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}