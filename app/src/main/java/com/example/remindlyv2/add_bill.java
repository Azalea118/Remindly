package com.example.remindlyv2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class add_bill extends AppCompatActivity {

    EditText billName, amount;
    Spinner dueDateSpinner;
    LinearLayout uploadBox;
    Button addBillBtn, cancelBtn;

    Uri selectedImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        billName = findViewById(R.id.billName);
        amount = findViewById(R.id.amount);
        dueDateSpinner = findViewById(R.id.dueDateSpinner);
        uploadBox = findViewById(R.id.uploadBox);
        addBillBtn = findViewById(R.id.addBillBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        setupSpinner();

        uploadBox.setOnClickListener(v -> pickImage());
        addBillBtn.setOnClickListener(v -> saveBill());
        cancelBtn.setOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        ArrayList<String> days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) days.add("Every " + i + "th");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                days
        );
        dueDateSpinner.setAdapter(adapter);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            Toast.makeText(this, "Receipt uploaded!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBill() {
        String name = billName.getText().toString().trim();
        String amountValue = amount.getText().toString().trim();
        String due = dueDateSpinner.getSelectedItem().toString();

        if (name.isEmpty() || amountValue.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent result = new Intent();
        result.putExtra("billName", name);
        result.putExtra("amount", amountValue);
        result.putExtra("dueDate", due);
        result.putExtra("receiptUri", selectedImage != null ? selectedImage.toString() : "");

        setResult(RESULT_OK, result);
        finish();
    }
}
