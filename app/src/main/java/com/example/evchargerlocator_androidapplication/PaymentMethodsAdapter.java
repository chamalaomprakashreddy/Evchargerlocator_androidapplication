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
    private int selectedPosition = -1;

    public interface OnPaymentMethodClickListener {
        void onMethodSelected(String method);
    }

    public PaymentMethodsAdapter(List<String> paymentMethods, OnPaymentMethodClickListener listener) {
        this.paymentMethods = paymentMethods;
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
        String method = paymentMethods.get(position);
        holder.methodTextView.setText(method);
        holder.itemView.setSelected(position == selectedPosition);
        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
            listener.onMethodSelected(method);
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView methodTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            methodTextView = itemView.findViewById(R.id.methodTextView);
        }
    }
}
