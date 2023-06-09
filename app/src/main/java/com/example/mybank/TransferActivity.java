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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TransferActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDB;
    private Button buttonCancel, buttonConfirm;
    private EditText editReceiver, editTitle, editAmount;
    private FirebaseUser user;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        mAuth = FirebaseAuth.getInstance();
        editReceiver = findViewById(R.id.receiver);
        editTitle = findViewById(R.id.title);
        editAmount = findViewById(R.id.amount);
        buttonCancel = findViewById(R.id.btn_cancel);
        buttonConfirm = findViewById(R.id.btn_confirm);
        user = mAuth.getCurrentUser();
        DatabaseReference transfersRef = FirebaseDatabase.getInstance().getReference().child("Transfers");

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTitle.getText().toString();
                double amount = Double.parseDouble(editAmount.getText().toString());
                String currentUserId = user.getUid();
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("UsersInfo");

                usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String senderAccountNumber = snapshot.child("accountNumber").getValue(String.class);
                            String receiverAccountNumber = editReceiver.getText().toString();

                            Query query = usersRef.orderByChild("accountNumber").equalTo(receiverAccountNumber);

                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot userSnapshot : snapshot.getChildren()){

                                        if (!userSnapshot.getKey().isEmpty()&&amount>0){
                                            Date date = new Date();
                                            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                                            String transferDate = formatter.format(date);

                                            Transfer transfer = new Transfer(senderAccountNumber, receiverAccountNumber, title, amount, transferDate);

                                            DatabaseReference senderAccountRef = usersRef.child(currentUserId);
                                            senderAccountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.child("balance").exists()) {
                                                        double senderBalance = snapshot.child("balance").getValue(Double.class);
                                                        if(senderBalance >= amount){
                                                            double newSenderBalance = senderBalance - amount;
                                                            senderAccountRef.child("balance").setValue(newSenderBalance);

                                                            String receiverID = userSnapshot.getKey();
                                                            DatabaseReference receiverAccountRef = usersRef.child(receiverID);
                                                            receiverAccountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    double receiverBalance = snapshot.child("balance").getValue(Double.class);
                                                                    double newReceiverBalance = receiverBalance + amount;
                                                                    receiverAccountRef.child("balance").setValue(newReceiverBalance);
                                                                    String transferId = transfersRef.push().getKey();
                                                                    transfersRef.child(transferId).setValue(transfer);

                                                                    Intent intent = new Intent(getApplicationContext(), Transfer_completed.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                        } else{
                                                            Toast.makeText(TransferActivity.this, "Insufficient balance", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(TransferActivity.this, "Balance field not found", Toast.LENGTH_SHORT).show();
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Toast.makeText(TransferActivity.this, "Failed to read data", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else{
                                            Toast.makeText(TransferActivity.this, "Wrong amount", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(TransferActivity.this, "Failed to read data", Toast.LENGTH_SHORT).show();
                                }
                            });


                            }
                            else {
                            Toast.makeText(TransferActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TransferActivity.this, "Failed to read data", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });
    }
    public class Transfer {
        private String sender;
        private String receiver;
        private String title;
        private double amount;
        private String date;



        public Transfer() {

        }

        public Transfer(String senderAccountNumber, String receiverAccountNumber, String title, double amount, String transferDate) {
            this.sender = senderAccountNumber;
            this.receiver = receiverAccountNumber;
            this.title = title;
            this.amount = amount;
            this.date = transferDate;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }
        public String getReceiver() {
            return receiver;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

}