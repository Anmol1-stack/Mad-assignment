package com.example.assignmentq5;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PhotoSelectorActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout emptyStateView;
    private TextView selectorTitle;
    private List<DocumentFile> imageList = new ArrayList<>();
    private Uri folderUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_selector);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        emptyStateView = findViewById(R.id.emptyStateView);
        selectorTitle = findViewById(R.id.selectorTitle);

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
            selectorTitle.setText(getString(R.string.select_photo));
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

            // Use the existing ImageAdapter with a custom click listener
            ImageAdapter adapter = new ImageAdapter(imageList, this::returnSelectedImage);
            recyclerView.setAdapter(adapter);
        }
    }

    private void returnSelectedImage(DocumentFile file) {
        // Return the selected image URI to MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("imageUri", file.getUri().toString());
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}