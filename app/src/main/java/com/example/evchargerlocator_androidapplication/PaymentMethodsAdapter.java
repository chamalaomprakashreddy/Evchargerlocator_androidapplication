package com.example.evchargerlocator_androidapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PaymentMethodsAdapter extends RecyclerView.Adapter<PaymentMethodsAdapter.ViewHolder> {

    private List<Card> cardList;
    private OnCardSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnCardSelectedListener {
        void onCardSelected(String cardNumber);
    }

    public PaymentMethodsAdapter(List<Card> cardList, OnCardSelectedListener listener) {
        this.cardList = cardList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_method, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Card card = cardList.get(position);

        holder.cardNumber.setText(card.getCardNumber());
        holder.cardHolderName.setText("Cardholder: " + card.getCardHolderName());
        holder.expiryDate.setText("Expires: " + card.getExpiryDate());

        // Set radio button selection
        holder.radioButton.setChecked(position == selectedPosition);
        holder.radioButton.setOnClickListener(v -> {
            int previousSelection = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelection);
            notifyItemChanged(selectedPosition);
            listener.onCardSelected(card.getCardNumber());
        });

        holder.itemView.setOnClickListener(v -> {
            int previousSelection = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelection);
            notifyItemChanged(selectedPosition);
            listener.onCardSelected(card.getCardNumber());
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cardNumber, cardHolderName, expiryDate;
        RadioButton radioButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNumber = itemView.findViewById(R.id.cardNumberText);
            cardHolderName = itemView.findViewById(R.id.cardHolderText);
            expiryDate = itemView.findViewById(R.id.expiryText);
            radioButton = itemView.findViewById(R.id.radioButton);
        }
    }
}