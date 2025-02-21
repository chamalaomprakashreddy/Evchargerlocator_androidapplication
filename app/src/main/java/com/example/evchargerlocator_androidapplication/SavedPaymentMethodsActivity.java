package com.example.evchargerlocator_androidapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class SavedPaymentMethodsActivity extends AppCompatActivity {

    private RecyclerView paymentMethodsRecyclerView;
    private Button continueButton;
    private List<Card> paymentMethods;
    private PaymentMethodsAdapter adapter;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private String selectedCardNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_payment_methods);

        paymentMethodsRecyclerView = findViewById(R.id.paymentMethodsRecyclerView);
        continueButton = findViewById(R.id.continueButton);
        TextView backArrowText = findViewById(R.id.backArrowText);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("SavedCards");

        backArrowText.setOnClickListener(v -> finish());

        paymentMethods = new ArrayList<>();
        adapter = new PaymentMethodsAdapter(paymentMethods, selectedCard -> {
            selectedCardNumber = selectedCard;
            continueButton.setEnabled(true);
        }, this::deleteCard);  // Pass deleteCard function to adapter

        paymentMethodsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        paymentMethodsRecyclerView.setAdapter(adapter);

        loadSavedCards();

        continueButton.setEnabled(false);
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(SavedPaymentMethodsActivity.this, PaymentProcessingActivity.class);
            intent.putExtra("selectedCard", selectedCardNumber);
            startActivity(intent);
        });
    }

    private void loadSavedCards() {
        String userId = auth.getCurrentUser().getUid();
        databaseRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paymentMethods.clear();
                for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                    Card card = cardSnapshot.getValue(Card.class);
                    if (card != null) {
                        card.setCardId(cardSnapshot.getKey()); // Store card ID for deletion
                        paymentMethods.add(card);
                    }
                }
                adapter.notifyDataSetChanged();
                Log.d("SavedPaymentMethods", "Loaded " + paymentMethods.size() + " cards.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SavedPaymentMethods", "Error loading cards: " + error.getMessage());
            }
        });
    }

    private void deleteCard(Card card) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Card")
                .setMessage("Are you sure you want to delete this card?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    String userId = auth.getCurrentUser().getUid();
                    databaseRef.child(userId).child(card.getCardId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(SavedPaymentMethodsActivity.this, "Card deleted", Toast.LENGTH_SHORT).show();
                                loadSavedCards();
                            })
                            .addOnFailureListener(e -> Toast.makeText(SavedPaymentMethodsActivity.this, "Failed to delete card", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
