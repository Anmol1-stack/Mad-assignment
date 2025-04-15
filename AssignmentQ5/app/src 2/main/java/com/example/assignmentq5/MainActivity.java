package com.example.assignmentq5;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int FOLDER_PICK_CODE = 2;
    private static final int PHOTO_PICK_CODE = 3;
    private static final int CAMERA_PERMISSION_CODE = 101;
    private Uri photoUri;
    private Uri selectedFolderUri = null;
    private Button btnAboutPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, CAMERA_PERMISSION_CODE);
        } else {
            initializeUI();
        }
    }

    private void initializeUI() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        Button btnOpenGallery = findViewById(R.id.btnOpenGallery);
        btnAboutPhoto = findViewById(R.id.btnAboutPhoto);

        btnTakePhoto.setOnClickListener(v -> takePhoto());
        btnOpenGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, FOLDER_PICK_CODE);
        });

        btnAboutPhoto.setOnClickListener(v -> {
            if (selectedFolderUri != null) {
                openPhotoSelector();
            } else {
                Toast.makeText(this, R.string.no_folder_selected, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openPhotoSelector() {
        DocumentFile folder = DocumentFile.fromTreeUri(this, selectedFolderUri);
        if (folder != null && folder.isDirectory()) {
            // Create PhotoSelectorActivity intent
            Intent intent = new Intent(this, PhotoSelectorActivity.class);
            intent.putExtra("folderUri", selectedFolderUri.toString());
            startActivityForResult(intent, PHOTO_PICK_CODE);
        } else {
            Toast.makeText(this, "Invalid folder", Toast.LENGTH_SHORT).show();
        }
    }

    private void takePhoto() {
        File photoFile;
        try {
            // Create default photo storage location if no folder is selected
            File storageDir = new File(getExternalFilesDir(null), "MyPhotos");
            if (!storageDir.exists()) storageDir.mkdirs();

            photoFile = File.createTempFile(
                    "IMG_" + System.currentTimeMillis(),
                    ".jpg",
                    storageDir
            );
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "File creation failed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Clears the activity stack and restarts MainActivity
     * This can be called when you need to reset the app state
     */
    public void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the activity stack
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FOLDER_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    );

                    // Save selected folder URI for later use
                    selectedFolderUri = uri;

                    // Enable the About Photo button since we now have a folder selected
                    btnAboutPhoto.setEnabled(true);

                    Toast.makeText(this, "Folder selected: " + uri.toString(), Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, GalleryActivity.class);
                    i.putExtra("folderUri", uri.toString());
                    startActivity(i);
                } catch (SecurityException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Permission error for selected folder", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid folder URI", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show();

            // If a folder was selected, copy the photo there
            if (selectedFolderUri != null) {
                copyPhotoToSelectedFolder();
            }
        }
        else if (requestCode == PHOTO_PICK_CODE && resultCode == RESULT_OK && data != null) {
            String imageUriString = data.getStringExtra("imageUri");
            if (imageUriString != null && !imageUriString.isEmpty()) {
                // Open the ImageDetailsActivity with the selected photo
                Intent intent = new Intent(this, ImageDetailsActivity.class);
                intent.putExtra("imageUri", imageUriString);
                startActivity(intent);
            }
        }
    }

    private void copyPhotoToSelectedFolder() {
        if (photoUri == null || selectedFolderUri == null) return;

        try {
            DocumentFile folder = DocumentFile.fromTreeUri(this, selectedFolderUri);
            if (folder != null && folder.isDirectory()) {
                String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
                DocumentFile newFile = folder.createFile("image/jpeg", fileName);

                if (newFile != null) {
                    InputStream in = getContentResolver().openInputStream(photoUri);
                    OutputStream out = getContentResolver().openOutputStream(newFile.getUri());

                    if (in != null && out != null) {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        in.close();
                        out.close();
                        Toast.makeText(this, "Photo copied to selected folder", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to copy photo to selected folder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                initializeUI(); // Permissions granted, initialize UI
            } else {
                Toast.makeText(this, "Camera and storage permissions are required.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}