package com.example.evchargerlocator_androidapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PaymentMethodsAdapter extends RecyclerView.Adapter<PaymentMethodsAdapter.ViewHolder> {

    private List<String> paymentMethods;
    private OnPaymentMethodClickListener listener;

    public interface OnPaymentMethodClickListener {
        void onPaymentMethodClick(String method);
    }

    public PaymentMethodsAdapter(List<String> paymentMethods, OnPaymentMethodClickListener listener) {
        this.paymentMethods = paymentMethods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String method = paymentMethods.get(position);
        holder.paymentMethodText.setText(method);
        holder.itemView.setOnClickListener(v -> listener.onPaymentMethodClick(method));
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView paymentMethodText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            paymentMethodText = itemView.findViewById(android.R.id.text1);
        }
    }
}
