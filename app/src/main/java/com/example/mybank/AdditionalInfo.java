package com.example.mybank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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
                String name, surname, phone, pesel, email;
                name = editTextName.getText().toString();
                surname = editTextSurname.getText().toString();
                email = mAuth.getCurrentUser().getEmail();
                phone = editTextPhone.getText().toString();
                pesel = editTextPESEL.getText().toString();

                Map<String, Object> userData = new HashMap<>();
                userData.put("name", name);
                userData.put("surname", surname);
                userData.put("email", email);
                userData.put("balance", 0);
                userData.put("phone", phone);
                userData.put("PESEL", pesel);

                DatabaseReference userRef = mDB.getInstance().getReference().child("UsersInfo").child(mAuth.getUid());
                userRef.setValue(userData);
                Toast.makeText(AdditionalInfo.this, "User information added.",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}