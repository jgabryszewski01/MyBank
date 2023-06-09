package com.example.mybank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDB;
    private Button buttonLogout, buttonTransfer;
    private TextView userDetails, balance, accountNum;
    private FirebaseUser user;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null || !mAuth.getCurrentUser().isEmailVerified()){
            if (!mAuth.getCurrentUser().isEmailVerified()){
                FirebaseAuth.getInstance().signOut();
            }
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        String Uid;
        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getCurrentUser().getUid();

        DatabaseReference userInfoRef = FirebaseDatabase.getInstance().getReference().child("UsersInfo").child(Uid).child("email");
        userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String DBEmail = snapshot.getValue(String.class);
                user = mAuth.getCurrentUser();
                String AuthEmail = user.getEmail();
                if(DBEmail == null || !DBEmail.equals(AuthEmail)){
                    Intent intent = new Intent(getApplicationContext(), AdditionalInfo.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        buttonLogout = findViewById(R.id.logout);
        buttonTransfer = findViewById(R.id.Transfer);
        balance = findViewById(R.id.balance);
        userDetails = findViewById(R.id.user_details);
        accountNum = findViewById(R.id.accountNumber);
        user = mAuth.getCurrentUser();

        String Uid = mAuth.getCurrentUser().getUid();
        DatabaseReference userInfoRef = FirebaseDatabase.getInstance().getReference().child("UsersInfo").child(Uid);

        userInfoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue(String.class);
                    String surname = snapshot.child("surname").getValue(String.class);
                    String acNumber = snapshot.child("accountNumber").getValue(String.class);
                    double balanceValue = snapshot.child("balance").getValue(Double.class);
                    userDetails.setText("Hello, " + name + " " + surname);
                    balance.setText("Account balance: " + String.format("%.2f", balanceValue));
                    accountNum.setText("Your account number: " + acNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to read user data.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        buttonTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TransferActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}