package com.example.remindlyv2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class dashboard extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navView;
    ImageView nav;

    LinearLayout almostDueContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_dashboard);

        almostDueContainer = findViewById(R.id.almostDueContainer);
            FloatingActionButton fab = findViewById(R.id.fabAdd);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(dashboard.this, add_bill.class);
            startActivityForResult(intent, 300);
        });



        final ImageView profile = (ImageView)findViewById(R.id.profile);
            drawerLayout = findViewById(R.id.drawerLayout);
            navView = findViewById(R.id.navView);
            nav = findViewById(R.id.nav);

            // OPEN DRAWER WHEN NAV ICON CLICKED
            nav.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));


            // CLOSE BUTTON INSIDE DRAWER HEADER
            View header = navView.getHeaderView(0);
            profile.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               startActivity(new Intent(dashboard.this, profile.class));
                                           }
                                       });

                    // MENU ITEM CLICK EVENTS (FIXED)
                    navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(MenuItem item) {

                            int id = item.getItemId();

                            if (id == R.id.menu_home) {
                                Toast.makeText(dashboard.this, "Home clicked", Toast.LENGTH_SHORT).show();

                            } else if (id == R.id.menu_history) {
                                Toast.makeText(dashboard.this, "History clicked", Toast.LENGTH_SHORT).show();

                            } else if (id == R.id.menu_settings) {
                                startActivity(new Intent(dashboard.this, settings.class));
                                Toast.makeText(dashboard.this, "Settings clicked", Toast.LENGTH_SHORT).show();
                            }

                            drawerLayout.closeDrawer(Gravity.LEFT);
                            return true;
                        }
                    });
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("billName");
            String amount = data.getStringExtra("amount");
            String due = data.getStringExtra("dueDate");
            String imageUri = data.getStringExtra("receiptUri");

            addBillCard(name, amount, due, imageUri);
        }
    }
    private void addBillCard(String name, String amount, String due, String imageUri) {
        View card = getLayoutInflater().inflate(R.layout.activity_bill_item, null);

        TextView billNameText = card.findViewById(R.id.billNameText);
        TextView billAmountText = card.findViewById(R.id.billAmountText);
        TextView billDueText = card.findViewById(R.id.billDueText);
        ImageView billImage = card.findViewById(R.id.billImage);

        billNameText.setText(name);
        billAmountText.setText("₱" + amount);
        billDueText.setText(due);

        if (imageUri != null && !imageUri.isEmpty()) {
            billImage.setImageURI(Uri.parse(imageUri));
        } else {
            billImage.setVisibility(View.GONE);
        }

        almostDueContainer.addView(card);
    }
}
