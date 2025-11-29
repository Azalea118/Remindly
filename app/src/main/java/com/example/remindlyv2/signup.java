package com.example.remindlyv2;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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


public class signup extends AppCompatActivity implements ConnectionReceiver.ReceiverListener{
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
            message = "";
            yn = "yes";
        } else {
            message = "Not Connected to Internet";
            yn = "no";
            Snackbar snackbar = Snackbar.make(findViewById(R.id.SignupLayout), message, Snackbar.LENGTH_LONG);
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
    public void validateEntries() {
        final TextInputEditText SUsernameE = (TextInputEditText) findViewById(R.id.SUsernameE);
        final TextInputLayout SUsernameL = (TextInputLayout) findViewById(R.id.SUsernameL);

        final TextInputEditText SEmailE = (TextInputEditText) findViewById(R.id.SEmailE);
        final TextInputLayout SEmailL = (TextInputLayout) findViewById(R.id.SEmailL);

        final TextInputEditText SPasswordE = (TextInputEditText) findViewById(R.id.SPasswordE);
        final TextInputLayout SPasswordL = (TextInputLayout) findViewById(R.id.SPasswordL);

        final TextInputEditText CSPasswordE = (TextInputEditText) findViewById(R.id.CSPasswordE);
        final TextInputLayout CSPasswordL = (TextInputLayout) findViewById(R.id.CSPasswordL);

        final String username = SUsernameE.getText().toString().toLowerCase().strip();
        final String email = SEmailE.getText().toString().toLowerCase().strip();
        final String password = SPasswordE.getText().toString().toLowerCase().strip();
        final String conpass = CSPasswordE.getText().toString().toLowerCase().strip();
        if (username.isEmpty()) {
            SUsernameL.setError("This field cannot be blank!");
            SUsernameL.setErrorIconDrawable(0);
            return;
        }
        if (email.isEmpty()) {
            SEmailL.setError("This field cannot be blank!");
            SEmailL.setErrorIconDrawable(0);
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            SEmailL.setError("Invalid Email address");
            SEmailL.setErrorIconDrawable(0);
            return;
        } if (password.isEmpty()) {
            SPasswordL.setError("This field cannot be blank!");
            SPasswordL.setErrorIconDrawable(0);
            return;
        } else if (password.length() < 6) {
            SPasswordL.setError("Password must be at least 6 characters");
            SPasswordL.setErrorIconDrawable(0);
            return;
        } if (!conpass.equals(password)) {
            CSPasswordL.setError("Passwords do not match");
            CSPasswordL.setErrorIconDrawable(0);
            return;
        }
            saveEntries(username, email, password);


    }
    public void saveEntries(String username, String email, String password){
        final TextInputLayout SEmailL = findViewById(R.id.SEmailL);
        final TextInputLayout SUsernameL = findViewById(R.id.SUsernameL);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String replace = email.replace(".", ",");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        String message = task.getException().getMessage();

                        if (message.contains("email address is already in use")) {
                            SEmailL.setError("This Email is already registered");
                            SEmailL.setErrorIconDrawable(0);
                        } else {
                            Toast.makeText(signup.this, message, Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            boolean usernameExists = false;
                            for (DataSnapshot user : snapshot.getChildren()) {
                                String existingUsername = String.valueOf(user.child("username").getValue());
                                if (existingUsername.equals(username)) {
                                    usernameExists = true;
                                    break;
                                }
                            }

                            if (usernameExists) {
                                SUsernameL.setError("This username is already in use");
                                SUsernameL.setErrorIconDrawable(0);

                                if (auth.getCurrentUser() != null) {
                                    auth.getCurrentUser().delete();
                                }
                                return;
                            }

                            databaseReference.child("users").child(replace).child("username").setValue(username);
                            databaseReference.child("users").child(replace).child("Password").setValue(password);

                            Toast.makeText(signup.this, "Registered Successfully", Toast.LENGTH_SHORT).show();

                            new CountDownTimer(2000, 1000) {
                                @Override
                                public void onFinish() {
                                    startActivity(new Intent(signup.this, login.class));
                                    finish();
                                }

                                @Override
                                public void onTick(long millisUntilFinished) {}
                            }.start();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                });
    }


    public static String pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        final Button SignUpBtn = (Button)findViewById(R.id.SignUpBtn);

        final TextInputEditText CSPasswordE = (TextInputEditText) findViewById(R.id.CSPasswordE);
        final TextInputLayout CSPasswordL = (TextInputLayout) findViewById(R.id.CSPasswordL);

        final TextInputEditText SEmailE = (TextInputEditText)findViewById(R.id.SEmailE);
        final TextInputLayout SEmailL = (TextInputLayout)findViewById(R.id.SEmailL);

        final TextInputEditText SPasswordE = (TextInputEditText)findViewById(R.id.SPasswordE);
        final TextInputLayout SPasswordL = (TextInputLayout)findViewById(R.id.SPasswordL);

        final TextInputEditText SUsernameE = (TextInputEditText) findViewById(R.id.SUsernameE);
        final TextInputLayout SUsernameL = (TextInputLayout) findViewById(R.id.SUsernameL);

        final TextView Loginbtn = (TextView)findViewById(R.id.Login);

        Loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CountDownTimer(500, 250){

                    @Override
                    public void onFinish() {
                        Loginbtn.setPaintFlags(Loginbtn.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                        Loginbtn.setTextColor(getResources().getColor(R.color.black));
                        startActivity(new Intent(signup.this, login.class));
                        finish();
                    }

                    @Override
                    public void onTick(long l) {
                        Loginbtn.setPaintFlags(Loginbtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                        Loginbtn.setTextColor(getResources().getColor(R.color.text));
                    }
                }.start();

            }
        });
        SEmailE.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SEmailL.setError(null);
                SEmailL.setErrorEnabled(false);
                SEmailL.setErrorIconDrawable(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        SUsernameE.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SUsernameL.setError(null);
                SUsernameL.setErrorEnabled(false);
                SUsernameL.setErrorIconDrawable(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        CSPasswordE.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                CSPasswordL.setError(null);
                CSPasswordL.setErrorEnabled(false);
                CSPasswordL.setErrorIconDrawable(null);
            }
        });

        SPasswordE.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pass = SPasswordE.getText().toString();
                if(pass.length()<6) {
                    SPasswordL.setError("Password must be at least 6 characters");
                    SPasswordL.setErrorIconDrawable(0);
                }else{
                    SPasswordL.setError(null);
                    SPasswordL.setErrorEnabled(false);
                    SPasswordL.setErrorIconDrawable(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        SignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateEntries();
            }
        });





        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.SignupLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}