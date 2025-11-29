package com.example.remindlyv2;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class forgotpassword extends AppCompatActivity implements ConnectionReceiver.ReceiverListener {

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
            Snackbar snackbar = Snackbar.make(findViewById(R.id.send), message, Snackbar.LENGTH_LONG);
            View view = snackbar.getView();
            snackbar.show();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgotpass);

        FirebaseAuth auth;
        auth = FirebaseAuth.getInstance();

        final TextInputEditText FUsernameE = findViewById(R.id.FUsernameE);
        final TextInputLayout FUsernameL = findViewById(R.id.FUsernameL);
        final ImageView send = findViewById(R.id.send);
        final ImageView back = findViewById(R.id.back);

        back.setOnClickListener(view -> finish());

        send.setOnClickListener(view -> {

            if (yn.equals("yes")) {

                String emailInput = FUsernameE.getText().toString().trim();

                if (emailInput.isEmpty()) {
                    FUsernameE.setError("Enter your email first");
                    FUsernameE.requestFocus();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                    FUsernameE.setError("Enter a valid email");
                    FUsernameE.requestFocus();
                    return;
                }
                String dbKey = emailInput.replace(".", ",");

                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(dbKey);

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            auth.sendPasswordResetEmail(emailInput)
                                    .addOnCompleteListener(t -> {
                                        if (t.isSuccessful()) {

                                            Toast.makeText(forgotpassword.this,
                                                    "Reset link sent to your email",
                                                    Toast.LENGTH_LONG).show();

                                        } else {
                                            Toast.makeText(forgotpassword.this,
                                                    "Failed to send email",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });

                        } else {
                            FUsernameL.setError("Email not found");
                            FUsernameL.setErrorIconDrawable(0);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(forgotpassword.this,
                                "Database error: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });


            } else {
                String message;
                message = "Not Connected to Internet";
                Snackbar snackbar = Snackbar.make(findViewById(R.id.send), message, Snackbar.LENGTH_LONG);
                View view1 = snackbar.getView();
                snackbar.show();
            }

        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ForgotPassLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
