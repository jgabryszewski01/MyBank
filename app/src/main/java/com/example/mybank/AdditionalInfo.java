package com.example.mybank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AdditionalInfo extends AppCompatActivity {

    FirebaseDatabase mDB;
    Button buttonAdd;
    private EditText editTextName, editTextSurname, editTextPhone, editTextPESEL;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        mDB = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        buttonAdd = findViewById(R.id.add);
        editTextName = findViewById(R.id.name);
        editTextSurname = findViewById(R.id.surname);
        editTextPhone = findViewById(R.id.phone);
        editTextPESEL = findViewById(R.id.pesel);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateAccountNumber();
            }
        });
    }
    private void generateAccountNumber() {
        DatabaseReference accountsRef = FirebaseDatabase.getInstance().getReference().child("Accounts");
        String accountNumber;

        Random random = new Random();
        int randomNumber = random.nextInt(90000000) + 10000000;
        accountNumber = String.valueOf(randomNumber);

        checkAccountNumberExists(accountsRef, accountNumber, new OnAccountNumberCheckedListener() {
            @Override
            public void onAccountNumberChecked(boolean exists) {
                if (exists) {
                    Toast.makeText(AdditionalInfo.this, "Account number already exists.", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference userAccountRef = accountsRef.child(mAuth.getCurrentUser().getUid());
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("UsersInfo").child(mAuth.getUid());
                    userAccountRef.child("accountNumber").setValue(accountNumber);
                    userRef.child("accountNumber").setValue(accountNumber);
                    addUserInfoToDB(accountNumber);
                }
            }
        });
    }

    private void checkAccountNumberExists(DatabaseReference accountsRef, final String accountNumber, final OnAccountNumberCheckedListener listener) {
        accountsRef.orderByChild("accountNumber").equalTo(accountNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean exists = snapshot.exists();
                listener.onAccountNumberChecked(exists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdditionalInfo.this, "Failed to read data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private interface OnAccountNumberCheckedListener {
        void onAccountNumberChecked(boolean exists);
    }
    private void addUserInfoToDB(String accountNumber){
        String name, surname, phone, pesel, email;
        name = editTextName.getText().toString();
        surname = editTextSurname.getText().toString();
        email = mAuth.getCurrentUser().getEmail();
        phone = editTextPhone.getText().toString();
        pesel = editTextPESEL.getText().toString();
        String balance = "500";

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("surname", surname);
        userData.put("email", email);
        userData.put("balance", balance);
        userData.put("phone", phone);
        userData.put("PESEL", pesel);
        userData.put("accountNumber", accountNumber);

        DatabaseReference userRef = mDB.getInstance().getReference().child("UsersInfo").child(mAuth.getUid());
        userRef.setValue(userData);
        Toast.makeText(AdditionalInfo.this, "User information added.",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

}