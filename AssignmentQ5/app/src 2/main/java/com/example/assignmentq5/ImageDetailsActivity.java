package com.example.assignmentq5;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailsActivity extends AppCompatActivity {

    private DocumentFile imageFile;
    private TextView tvDetails;
    private ImageView imageView;
    private Button btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        imageView = findViewById(R.id.imageView);
        tvDetails = findViewById(R.id.tvDetails);
        btnDelete = findViewById(R.id.btnDelete);

        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString == null) {
            Toast.makeText(this, "Error: No image provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Uri imageUri = Uri.parse(imageUriString);
        imageFile = DocumentFile.fromSingleUri(this, imageUri);

        if (imageFile == null || !imageFile.exists()) {
            Toast.makeText(this, "Error: Image not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imageView.setImageURI(imageUri);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String lastModified = sdf.format(new Date(imageFile.lastModified()));

        // Try to get the date taken from EXIF data if available
        String dateTaken = tryGetDateTaken(imageUri);
        if (dateTaken == null) {
            dateTaken = lastModified; // Fall back to last modified if EXIF data not available
        }

        tvDetails.setText(
                "Name: " + imageFile.getName() + "\n" +
                        "Path: " + imageUri.getPath() + "\n" +
                        "Size: " + (imageFile.length() / 1024) + " KB\n" +
                        "Date Taken: " + dateTaken
        );

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Image")
                    .setMessage(getString(R.string.confirm_delete))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        if (imageFile.delete()) {
                            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();

                            // Create an intent to return to MainActivity directly
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the activity stack
                            startActivity(intent);

                            // Now finish this activity
                            finish();
                        } else {
                            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        });
    }

    private String tryGetDateTaken(Uri imageUri) {
        try {
            InputStream in = getContentResolver().openInputStream(imageUri);
            if (in == null) return null;

            ExifInterface exif = new ExifInterface(in);
            String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
            in.close();

            if (dateTime != null && !dateTime.isEmpty()) {
                return dateTime;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}