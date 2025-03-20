package com.example.car_helper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

public class PlannedExpenseAdapter extends RecyclerView.Adapter<PlannedExpenseAdapter.PlannedExpenseViewHolder> {
    private List<PlannedExpense> plannedExpenses;
    private SimpleDateFormat dateFormat;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public PlannedExpenseAdapter(List<PlannedExpense> plannedExpenses, SimpleDateFormat dateFormat) {
        this.plannedExpenses = plannedExpenses;
        this.dateFormat = dateFormat;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlannedExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_planned_expense, parent, false);
        return new PlannedExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlannedExpenseViewHolder holder, int position) {
        PlannedExpense plannedExpense = plannedExpenses.get(position);
        holder.description.setText(plannedExpense.getDescription());
        holder.date.setText(dateFormat.format(plannedExpense.getDate()));
        holder.comment.setText(plannedExpense.getComment());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return plannedExpenses.size();
    }

    // Метод для удаления элемента
    public void removeItem(int position) {
        plannedExpenses.remove(position);
        notifyItemRemoved(position);
    }

    static class PlannedExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView description;
        TextView date;
        TextView comment;

        public PlannedExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.planned_description);
            date = itemView.findViewById(R.id.planned_date);
            comment = itemView.findViewById(R.id.planned_comment);
        }
    }
}

