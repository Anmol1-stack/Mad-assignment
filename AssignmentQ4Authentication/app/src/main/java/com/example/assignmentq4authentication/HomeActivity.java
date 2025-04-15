package com.example.assignmentq4authentication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView welcomeText;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In client for sign out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize UI elements
        welcomeText = findViewById(R.id.welcomeText);
        logoutButton = findViewById(R.id.logoutButton);

        // Display user information
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            welcomeText.setText("Welcome, " + currentUser.getDisplayName() + "!");
        }

        // Set up logout button click listener
        logoutButton.setOnClickListener(view -> signOut());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void signOut() {
        // Sign out from Firebase
        mAuth.signOut();

        // Sign out from Google
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                // Navigate back to MainActivity
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(HomeActivity.this, "Logout failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If no user is signed in, go back to login screen
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
    }
}