package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
        });

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
                        paymentMethods.add(card);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}