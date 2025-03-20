package com.example.car_helper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private List<Expense> expenses;
    private SimpleDateFormat dateFormat;

    public ExpenseAdapter(List<Expense> expenses, SimpleDateFormat dateFormat) {
        this.expenses = expenses;
        this.dateFormat = dateFormat;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.amountText.setText(String.format("Сумма: %.2f руб.", expense.getAmount()));
        holder.dateText.setText(String.format("Дата: %s", dateFormat.format(expense.getDate())));
        holder.commentText.setText(String.format("Комментарий: %s",
                expense.getComment() != null ? expense.getComment() : "Нет комментария"));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView amountText, dateText, commentText;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            amountText = itemView.findViewById(R.id.expense_amount);
            dateText = itemView.findViewById(R.id.expense_date);
            commentText = itemView.findViewById(R.id.expense_comment);
        }
    }
}