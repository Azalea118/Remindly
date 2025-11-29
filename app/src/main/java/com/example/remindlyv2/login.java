package com.example.remindlyv2;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity implements ConnectionReceiver.ReceiverListener{
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
            Snackbar snackbar = Snackbar.make(findViewById(R.id.LoginBtn), message, Snackbar.LENGTH_LONG);
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

    static String username;
    static String password;
    private boolean validateField(TextInputLayout layout, String value) {
        if (value.isEmpty()) {
            layout.setError("This entry cannot be blank!");
            layout.setErrorIconDrawable(0);
            return false;
        } else {
            layout.setError(null);
            layout.setErrorIconDrawable(null);
            return true;
        }
    }
    public void checkString(){
        TextInputLayout UsernameL = findViewById(R.id.UsernameL);
        TextInputLayout PasswordL = findViewById(R.id.PasswordL);

        boolean validUsername = validateField(UsernameL, username);
        boolean validPassword = validateField(PasswordL, password);

        if (validUsername && validPassword) {
            Auth();
        }
    }
    public void Auth() {
        final CheckBox RememberMe = findViewById(R.id.RememberMe);
        final TextInputLayout UsernameL = findViewById(R.id.UsernameL);
        final TextInputLayout PasswordL = findViewById(R.id.PasswordL);
        final TextInputEditText PasswordE = findViewById(R.id.PasswordE);

        String input = username;
        String passwordInput = password;

        databaseReference.child("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String loginEmail = null;

                        String replace = input.replace(".", ",");
                        if (snapshot.hasChild(replace)) {
                            loginEmail = input;
                        }

                        if (loginEmail == null) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                String dbUsername = userSnap.child("username").getValue(String.class);
                                if (dbUsername != null && dbUsername.equals(input)) {
                                    loginEmail = userSnap.getKey().replace(",", ".");
                                    break;
                                }
                            }
                        }

                        if (loginEmail == null) {
                            UsernameL.setError("No account with this Email/Username.");
                            UsernameL.setErrorIconDrawable(0);
                            return;
                        }

                        final String finalEmail = loginEmail;

                        mAuth.signInWithEmailAndPassword(finalEmail, passwordInput)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {

                                        if (RememberMe.isChecked()) {
                                            SharedPreferences sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();

                                            editor.putBoolean("remember", true);
                                            editor.putString("email", finalEmail);
                                            editor.apply();
                                        }

                                        Toast.makeText(login.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(login.this, dashboard.class));
                                        finish();
                                    }
                                    else {
                                        PasswordL.setError("Password is incorrect.");
                                        PasswordL.setErrorIconDrawable(0);
                                        PasswordE.setText("");
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
       /* SharedPreferences sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        boolean remember = sharedPreferences.getBoolean("remember", false);

        if (remember) {
            Intent intent = new Intent(login.this, dashboard.class);
            startActivity(intent);
            finish();
        } */
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        final Button LoginBtn = (Button)findViewById(R.id.LoginBtn);
        final TextInputEditText UsernameE = (TextInputEditText)findViewById(R.id.UsernameE);
        final TextInputEditText PasswordE = (TextInputEditText)findViewById(R.id.PasswordE);
        final TextInputLayout UsernameL = findViewById(R.id.UsernameL);
        final TextInputLayout PasswordL = findViewById(R.id.PasswordL);

        final TextView Forgotpass = (TextView)findViewById(R.id.ForgotPass);
        final TextView Signup = (TextView)findViewById(R.id.Signup);

        UsernameE.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                UsernameL.setError(null);
                UsernameL.setErrorEnabled(false);
                UsernameL.setErrorIconDrawable(null);
            }
        });
        PasswordE.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /*PasswordL.setError(null);
                PasswordL.setErrorEnabled(false);
                PasswordL.setErrorIconDrawable(null);*/
            }
        });
        Forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CountDownTimer(500, 250){

                    @Override
                    public void onFinish() {
                        Forgotpass.setPaintFlags(Forgotpass.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                        Forgotpass.setTextColor(getResources().getColor(R.color.black));
                        startActivity(new Intent(login.this, forgotpassword.class));
                    }

                    @Override
                    public void onTick(long l) {
                        Forgotpass.setPaintFlags(Forgotpass.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                        Forgotpass.setTextColor(getResources().getColor(R.color.text));
                    }
                }.start();



            }
        });

        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CountDownTimer(500, 250){

                    @Override
                    public void onFinish() {
                        Signup.setPaintFlags(Signup.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                        Signup.setTextColor(getResources().getColor(R.color.black));
                        startActivity(new Intent(login.this, signup.class));
                        finish();
                    }

                    @Override
                    public void onTick(long l) {
                        Signup.setPaintFlags(Signup.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                        Signup.setTextColor(getResources().getColor(R.color.text));
                    }
                }.start();

            }
        });

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection();
                if(yn.equals("yes")){
                    username = UsernameE.getText().toString().toLowerCase().strip();
                    password = PasswordE.getText().toString().strip();
                    checkString();

                }else{
                    String message;
                    message = "Not Connected to Internet";
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.LoginBtn), message, Snackbar.LENGTH_LONG);
                    view = snackbar.getView();
                    snackbar.show();
                }
            }
        });









        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.LoginLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
