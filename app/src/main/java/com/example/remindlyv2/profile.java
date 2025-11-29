package com.example.remindlyv2;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class profile extends AppCompatActivity implements ConnectionReceiver.ReceiverListener{
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://remindlydatabase-default-rtdb.firebaseio.com/").getReference();
    public static String yn = "no";
    private void checkConnection() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.new.conn.CONNECTIVITY_CHANGE");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(new ConnectionReceiver(), intentFilter, RECEIVER_EXPORTED);
        }
        ConnectionReceiver.Listener = this;
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        showSnackBar(isConnected);
    }
    private void showSnackBar(boolean isConnected) {
        String message;
        if (isConnected) {
            yn = "yes";
        } else {
            message = "Not Connected to Internet";
            yn = "no";
            /*Snackbar snackbar = Snackbar.make(findViewById(R.id.), message, Snackbar.LENGTH_LONG);
            View view = snackbar.getView();
            snackbar.show();*/
        }

    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        showSnackBar(isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkConnection();
    }


    private TextView txtName, txtEmail, txtPhone;
    private MaterialButton btnEdit;
    private LinearLayout btnRateUs, btnReportIssue, btnShare,   btnLogout;
    private FloatingActionButton fabAdd;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_profile) {
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // Views
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);

        btnEdit = findViewById(R.id.btnEdit);

        btnRateUs = findViewById(R.id.rate_us);
        btnReportIssue = findViewById(R.id.report_issue);
        btnShare = findViewById(R.id.share_app);
        btnLogout = findViewById(R.id.log_out);

        fabAdd = findViewById(R.id.fabAdd);

        loadUserData();
        setupActions();
    }

    // Load user data from Firebase
    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();

        usersRef.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {

                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);

                    txtName.setText(name != null ? name : "Unknown");
                    txtEmail.setText(email != null ? email : "No Email");
                    txtPhone.setText(phone != null ? phone : "No Phone");

                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Button click functions
    private void setupActions() {

        btnEdit.setOnClickListener(v -> {
           // Intent i = new Intent(ProfileActivity.this, EditProfileActivity.class);
           // startActivity(i);
        });

        btnRateUs.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + getPackageName())));
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });

        btnReportIssue.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:developer@email.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "App Issue Report");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Describe your issue here:");
            startActivity(emailIntent);
        });

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Try the Remindly app! Download it here:\nhttps://play.google.com/store/apps/details?id="
                            + getPackageName());
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        btnLogout.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(profile.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(profile.this, "Logged out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(profile.this, login.class));
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(profile.this, login.class));
            finish();
        });

    }
}
