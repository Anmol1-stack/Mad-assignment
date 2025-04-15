package com.example.assignmentq5;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout emptyStateView;
    private TextView galleryTitle;
    private List<DocumentFile> imageList = new ArrayList<>();
    private ImageAdapter adapter;
    private Uri folderUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            return;
        }

        setContentView(R.layout.activity_gallery);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        emptyStateView = findViewById(R.id.emptyStateView);
        galleryTitle = findViewById(R.id.galleryTitle);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Get folder URI from intent
        String folderUriString = getIntent().getStringExtra("folderUri");
        if (folderUriString == null || folderUriString.isEmpty()) {
            Toast.makeText(this, "Invalid folder URI", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        folderUri = Uri.parse(folderUriString);
        DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri);
        if (folder != null && folder.getName() != null) {
            galleryTitle.setText(folder.getName());
        }

        loadImagesFromFolder();
    }

    private void loadImagesFromFolder() {
        imageList.clear();
        DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri);

        if (folder != null && folder.isDirectory()) {
            for (DocumentFile file : folder.listFiles()) {
                if (file.isFile() && file.getName() != null &&
                        (file.getName().toLowerCase().endsWith(".jpg") ||
                                file.getName().toLowerCase().endsWith(".jpeg") ||
                                file.getName().toLowerCase().endsWith(".png"))) {
                    imageList.add(file);
                }
            }
        } else {
            Toast.makeText(this, "Could not access folder", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show empty state or recycler view based on images found
        if (imageList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
            adapter = new ImageAdapter(imageList, this::openDetails);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate(); // Permission granted, recreate activity
            } else {
                Toast.makeText(this, "Storage permission is required to access images.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void openDetails(DocumentFile file) {
        Intent intent = new Intent(this, ImageDetailsActivity.class);
        intent.putExtra("imageUri", file.getUri().toString());
        startActivity(intent); // We don't need to wait for a result since we'll go straight to MainActivity after deletion
    }
}