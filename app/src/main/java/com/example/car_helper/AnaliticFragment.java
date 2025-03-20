package com.example.car_helper;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AnaliticFragment extends Fragment {

    private AutoCompleteTextView expenseCategory; // Замените Spinner на AutoCompleteTextView
    private TextInputEditText expenseAmount;
    private TextInputEditText expenseDate;
    private TextInputEditText limitInput;
    private TextInputEditText comment;
    private Button buttonAddExpense;
    private Button buttonSetLimit;
    private Button buttonWeek;
    private Button buttonMonth;
    private Button buttonYear;
    private Button buttonAddPlannedExpense;
    private Button buttonCompareMonths;
    private Button buttonClearExpenses;
    private Button buttonPlannedExpense;
    private PieChart pieChart;
    private BarChart barChart;
    private ProgressBar totalExpenseProgress;
    private TextView totalExpenseText;
    private TextView limitText;
    private SharedPreferences sharedPreferences;
    private TextInputLayout expenseCategoryLayout;
    private List<Expense> expenses = new ArrayList<>();
    private List<PlannedExpense> plannedExpenses = new ArrayList<>(); // Список плановых расходов
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private double monthlyLimit = 0;
    private static final double NOTIFICATION_THRESHOLD = 0.9;
    private double notificationThreshold = 0.9;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analitic, container, false);

        expenseCategoryLayout = view.findViewById(R.id.expense_category_layout);

        expenseCategory = view.findViewById(R.id.expense_category);
        expenseAmount = view.findViewById(R.id.expense_amount);
        expenseDate = view.findViewById(R.id.expense_date);
        comment = view.findViewById(R.id.comment_input);
        limitInput = view.findViewById(R.id.limit_input);
        buttonAddExpense = view.findViewById(R.id.button_add_expense);
        buttonSetLimit = view.findViewById(R.id.button_set_limit);
        buttonWeek = view.findViewById(R.id.button_week);
        buttonMonth = view.findViewById(R.id.button_month);
        buttonYear = view.findViewById(R.id.button_year);
        buttonCompareMonths = view.findViewById(R.id.button_compare_months);
        pieChart = view.findViewById(R.id.pie_chart_expenses);
        barChart = view.findViewById(R.id.bar_chart_comparison);
        totalExpenseProgress = view.findViewById(R.id.total_expense_progress);
        totalExpenseText = view.findViewById(R.id.total_expense_text);
        limitText = view.findViewById(R.id.limit_text);
        buttonClearExpenses = view.findViewById(R.id.button_clear_expenses);
        buttonAddPlannedExpense = view.findViewById(R.id.button_add_planned_expense);
        buttonPlannedExpense = view.findViewById(R.id.view_planned_expense);

        // Установка текущей даты по умолчанию
        expenseDate.setText(dateFormat.format(new Date()));

        // Настройка выпадающего списка с категориями
        setupCategorySpinner();

        // Инициализация SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("expenses_prefs", Context.MODE_PRIVATE);

        // Загрузка данных из SharedPreferences
        loadExpenses();
        loadLimit();
        barChart.setVisibility(View.GONE);

        // Отображение графика и общей суммы
        updateChart(expenses);
        updateTotalExpense(expenses);
        createNotificationChannel();
        setupPieChart();
        loadPlannedExpenses();
        checkPlannedExpensesDates();


        expenseCategoryLayout.setHintTextColor(
                ContextCompat.getColorStateList(requireContext(), R.color.hint_color_selector)
        );

        // Обработчики нажатий
        buttonAddExpense.setOnClickListener(v -> addExpense());
        buttonSetLimit.setOnClickListener(v -> setLimit());
        buttonWeek.setOnClickListener(v -> showStatistics("week"));
        buttonMonth.setOnClickListener(v -> showStatistics("month"));
        buttonYear.setOnClickListener(v -> showStatistics("year"));
        buttonCompareMonths.setOnClickListener(v -> compareMonths());

        // Обработчик нажатия на поле ввода даты
        expenseDate.setOnClickListener(v -> showDatePickerDialog());
        buttonClearExpenses.setOnClickListener(v -> showConfirmationDialog());
        buttonAddPlannedExpense.setOnClickListener(v -> showAddPlannedExpenseDialog());
        buttonPlannedExpense.setOnClickListener(v -> showPlannedExpensesDialog());

        return view;
    }

    private void showAddPlannedExpenseDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_planned_expense, null);
        TextInputEditText plannedDescription = dialogView.findViewById(R.id.planned_description);
        TextInputEditText plannedDate = dialogView.findViewById(R.id.planned_date);
        TextInputEditText plannedComment = dialogView.findViewById(R.id.planned_comment);

        // Обработчик нажатия на поле ввода даты
        plannedDate.setOnClickListener(v -> {
            // Получаем текущую дату
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Создаем DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Обновляем поле ввода даты
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);
                        plannedDate.setText(dateFormat.format(selectedDate.getTime()));
                    },
                    year, month, day
            );

            // Устанавливаем минимальную дату (текущая дата)
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

            // Показываем диалог
            datePickerDialog.show();
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Добавить", (dialog, which) -> {
                    String description = plannedDescription.getText().toString();
                    String dateStr = plannedDate.getText().toString();
                    String comment = plannedComment.getText().toString();

                    if (!description.isEmpty() && !dateStr.isEmpty()) {
                        try {
                            Date date = dateFormat.parse(dateStr);
                            PlannedExpense plannedExpense = new PlannedExpense(description, date, comment);
                            plannedExpenses.add(plannedExpense);
                            savePlannedExpenses();

                            Toast.makeText(getContext(), "Плановый расход добавлен", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Ошибка ввода данных", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void showPlannedExpensesDialog() {
        // Проверяем, есть ли плановые расходы
        if (plannedExpenses.isEmpty()) {
            // Если список пуст, показываем сообщение
            Toast.makeText(getContext(), "Плановые расходы отсутствуют", Toast.LENGTH_SHORT).show();

            return; // Завершаем выполнение метода
        }

        // Если плановые расходы есть, показываем диалог
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_planned_expenses, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.planned_expenses_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Создаем адаптер
        PlannedExpenseAdapter adapter = new PlannedExpenseAdapter(plannedExpenses, dateFormat);


        // Устанавливаем слушатель
        adapter.setOnItemClickListener(position -> {
            androidx.appcompat.app.AlertDialog deleteDialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Удаление планового расхода")
                    .setMessage("Вы уверены, что хотите удалить этот плановый расход?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        // Удаляем элемент из списка
                        plannedExpenses.remove(position);

                        // Уведомляем адаптер об удалении элемента
                        adapter.notifyItemRemoved(position);


                        // Сохраняем изменения в SharedPreferences
                        savePlannedExpenses();


                        // Показываем уведомление об удалении
                        Toast.makeText(getContext(), "Плановый расход удален", Toast.LENGTH_SHORT).show();

                        // Если после удаления список стал пустым, закрываем диалог
                        if (plannedExpenses.isEmpty()) {
                            // Закрываем диалог с плановыми расходами
                            if (dialogView.getParent() != null) {
                                ((ViewGroup) dialogView.getParent()).removeView(dialogView); // Убираем диалог из иерархии
                            }
                            Toast.makeText(getContext(), "Плановые расходы отсутствуют", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Устанавливаем адаптер в RecyclerView
        recyclerView.setAdapter(adapter);

        // Показываем диалог
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Закрыть", (d, which) -> d.dismiss())
                .show();
    }

    private void checkPlannedExpensesDates() {
        Calendar currentDate = Calendar.getInstance();
        Calendar expenseDate = Calendar.getInstance();

        for (Iterator<PlannedExpense> iterator = plannedExpenses.iterator(); iterator.hasNext();) {
            PlannedExpense expense = iterator.next();
            expenseDate.setTime(expense.getDate());

            // Разница в днях между текущей датой и датой расхода
            long diffInMillis = expenseDate.getTimeInMillis() - currentDate.getTimeInMillis();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            // Если до расхода осталось 2 дня или меньше
            if (diffInDays <= 2 && diffInDays >= 0) {
                showNotification(expense.getDescription(), "До планового расхода осталось " + diffInDays + " дня(ей)");
            }

            // Если текущая дата превышает дату расхода на неделю
            if (diffInDays < -7) {
                iterator.remove(); // Удаляем расход
            }
        }

        // Сохраняем изменения в SharedPreferences
        savePlannedExpenses();
    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Создаем канал уведомлений (для Android 8.0 и выше)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "planned_expenses_channel",
                    "Плановые расходы",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Уведомления о плановых расходах");
            notificationManager.createNotificationChannel(channel);
        }

        // Создаем уведомление
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "planned_expenses_channel")
                .setSmallIcon(R.drawable.carsplash) // Иконка уведомления
                .setContentTitle(title) // Заголовок уведомления
                .setContentText(message) // Текст уведомления
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Приоритет
                .setAutoCancel(true); // Уведомление исчезает после нажатия

        // Показываем уведомление
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void savePlannedExpenses() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(plannedExpenses);
        editor.putString("plannedExpenses", json);
        editor.apply();
    }

    private void loadPlannedExpenses() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("plannedExpenses", null);
        Type type = new TypeToken<ArrayList<PlannedExpense>>() {}.getType();
        if (json != null) {
            plannedExpenses = gson.fromJson(json, type);
        } else {
            plannedExpenses = new ArrayList<>();
        }
    }

    private void showDatePickerDialog(TextInputEditText dateField) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    dateField.setText(dateFormat.format(selectedDate.getTime()));
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "expense_limit_channel", // ID канала
                    "Лимит расходов", // Название канала
                    NotificationManager.IMPORTANCE_DEFAULT // Важность канала
            );
            channel.setDescription("Уведомления о приближении к лимиту расходов");

            // Добавление звука
            Uri soundUri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.option);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);
            NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "expense_limit_channel")
                .setSmallIcon(R.drawable.carsplash) // Иконка уведомления
                .setContentTitle(title) // Заголовок уведомления
                .setContentText(message) // Текст уведомления
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Приоритет
                .setAutoCancel(true); // Автоматическое закрытие уведомления после нажатия

        notificationManager.notify(1, builder.build());
    }

    private void saveNotificationThreshold() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("notificationThreshold", (float) notificationThreshold);
        editor.apply();
    }

    private void loadNotificationThreshold() {
        notificationThreshold = sharedPreferences.getFloat("notificationThreshold", 0.9f);
    }

    private void setupCategorySpinner() {
        // Список категорий
        String[] categories = {
                "Топливо",
                "ТО",
                "Ремонт",
                "Страховка",
                "Штрафы",
                "Автоналог",
                "Прочие расходы"
        };

        // Создание адаптера для AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_menu_item,
                categories
        );

        // Установка адаптера
        expenseCategory.setAdapter(adapter);
        expenseCategoryLayout.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.purple_500)
        ));
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Удаление всех расходов")
                .setMessage("Вы уверены, что хотите удалить все расходы? Это действие нельзя отменить.")
                .setPositiveButton("Удалить", (dialog, which) -> clearAllExpenses()) // Действие при подтверждении
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss()) // Действие при отмене
                .setIcon(android.R.drawable.ic_dialog_alert) // Иконка предупреждения
                .show();
    }

    private void showDatePickerDialog() {
        // Получаем текущую дату
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Создаем DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Обновляем поле ввода даты
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    expenseDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                year, month, day
        );

        // Показываем диалог
        datePickerDialog.show();
    }

    private void addExpense() {
        String category = expenseCategory.getText().toString();
        String amountStr = expenseAmount.getText().toString();
        String dateStr = expenseDate.getText().toString();
        String comm = comment.getText().toString();

        if (category.isEmpty()) {
            Toast.makeText(getContext(), "Выберите категорию", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!amountStr.isEmpty() && !dateStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                Date date = dateFormat.parse(dateStr);
                expenses.add(new Expense(category, amount, date, comm));
                saveExpenses();
                updateChart(expenses);
                updateTotalExpense(expenses);
                clearInputs();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Ошибка ввода данных", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
        }
        comment.setText("");
    }

    private void updateChart(List<Expense> expenseList) {
        // Показать PieChart
        pieChart.setVisibility(View.VISIBLE);

        Map<String, Float> categorySums = new HashMap<>();

        for (Expense expense : expenseList) {
            String category = expense.getDescription();
            float amount = (float) expense.getAmount();
            categorySums.put(category, categorySums.getOrDefault(category, 0f) + amount);
        }

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categorySums.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "Расходы");
        int[] colors = {
                0xFF9C27B0, // Фиолетовый (Purple 500)
                0xFF673AB7, // Темно-фиолетовый (Deep Purple 500)
                0xFFFFEB3B, // Желтый (Yellow 500)
                0xFFD7BA89, // Бежевый (#D7BA89)
                0xFF462255, // Темно-фиолетовый (#462255)
                0xFFB47EB3  // Светло-фиолетовый (#B47EB3)
        };
        dataSet.setColors(colors);


        pieChart.setCenterText("Расходы"); // Текст в центре
        pieChart.setCenterTextSize(18f); // Размер текста в центре
        pieChart.setRotationAngle(0); // Начальный угол вращения
        pieChart.setRotationEnabled(true); // Включение вращения
        pieChart.setHighlightPerTapEnabled(true); // Подсветка при нажатии

        // Настройка легенды
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM); // Позиция легенды
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER); // Выравнивание легенды
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL); // Ориентация легенды
        legend.setDrawInside(false); // Легенда вне диаграммы
        legend.setXEntrySpace(7f); // Отступ между элементами легенды
        legend.setYEntrySpace(0f); // Отступ между строками легенды
        legend.setYOffset(10f); // Смещение легенды по вертикали
        legend.setWordWrapEnabled(true); // Перенос текста легенды

        pieChart.getDescription().setEnabled(false);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private void updateTotalExpense(List<Expense> expenseList) {
        double total = 0;
        for (Expense expense : expenseList) {
            total += expense.getAmount();
        }
        totalExpenseText.setText(String.format("Общая сумма: %.2f руб.", total));
        totalExpenseProgress.setProgress((int) total);

        // Проверка лимита
        if (monthlyLimit > 0) {
            double progress = (total / monthlyLimit) * 100;
            totalExpenseProgress.setProgress((int) progress);

            // Уведомление о приближении к лимиту
            if (progress >= NOTIFICATION_THRESHOLD * 100 && progress < 100) {
                Toast.makeText(getContext(), "Вы близки к лимиту расходов!", Toast.LENGTH_SHORT).show();
                sendNotification("Лимит расходов", "Вы близки к лимиту расходов!");
            }

            // Уведомление о превышении лимита
            if (total > monthlyLimit) {
                Toast.makeText(getContext(), "Лимит превышен!", Toast.LENGTH_SHORT).show();
                sendNotification("Лимит расходов", "Лимит превышен!");
            }
        }
    }

    private void clearInputs() {
        expenseAmount.setText("");
        expenseDate.setText(dateFormat.format(new Date()));
        expenseCategory.setText(""); // Сброс выбора категории
    }

    private void clearAllExpenses() {
        // Очистка списка расходов
        expenses.clear();

        // Сохранение изменений в SharedPreferences
        saveExpenses();

        // Обновление интерфейса
        updateChart(expenses);
        updateTotalExpense(expenses);

        // Уведомление пользователя
        Toast.makeText(getContext(), "Все расходы удалены", Toast.LENGTH_SHORT).show();
    }

    private void saveExpenses() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(expenses);
        editor.putString("expenses", json);
        editor.apply();
    }

    private void loadExpenses() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("expenses", null);
        Type type = new TypeToken<ArrayList<Expense>>(){}.getType();
        if (json != null) {
            expenses = gson.fromJson(json, type);
        } else {
            expenses = new ArrayList<>();
        }
    }

    private void setLimit() {
        String limitStr = limitInput.getText().toString();
        if (!limitStr.isEmpty()) {
            monthlyLimit = Double.parseDouble(limitStr);
            limitText.setText(String.format("Лимит: %.2f руб.", monthlyLimit));
            saveLimit();
            updateTotalExpense(expenses);
        } else {
            Toast.makeText(getContext(), "Введите лимит", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLimit() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("monthlyLimit", (float) monthlyLimit);
        editor.apply();
    }

    private void loadLimit() {
        monthlyLimit = sharedPreferences.getFloat("monthlyLimit", 0);
        limitText.setText(String.format("Лимит: %.2f руб.", monthlyLimit));
    }

    private void showStatistics(String period) {
        // Скрыть BarChart при отображении статистики
        barChart.setVisibility(View.GONE);

        Calendar calendar = Calendar.getInstance();
        List<Expense> filteredExpenses = new ArrayList<>();

        for (Expense expense : expenses) {
            calendar.setTime(expense.getDate());
            switch (period) {
                case "week":
                    if (isSameWeek(calendar, Calendar.getInstance())) {
                        filteredExpenses.add(expense);
                    }
                    break;
                case "month":
                    if (isSameMonth(calendar, Calendar.getInstance())) {
                        filteredExpenses.add(expense);
                    }
                    break;
                case "year":
                    if (isSameYear(calendar, Calendar.getInstance())) {
                        filteredExpenses.add(expense);
                    }
                    break;
            }
        }
        updateChart(filteredExpenses);
        updateTotalExpense(filteredExpenses);
    }

    private boolean isSameWeek(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    private boolean isSameMonth(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    private boolean isSameYear(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    private void compareMonths() {
        // Показать BarChart
        barChart.setVisibility(View.VISIBLE);

        Calendar cal = Calendar.getInstance();
        List<Expense> currentMonthExpenses = new ArrayList<>();
        List<Expense> previousMonthExpenses = new ArrayList<>();

        for (Expense expense : expenses) {
            cal.setTime(expense.getDate());
            if (isSameMonth(cal, Calendar.getInstance())) {
                currentMonthExpenses.add(expense);
            } else if (cal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) - 1) {
                previousMonthExpenses.add(expense);
            }
        }

        double currentMonthTotal = getTotalExpenses(currentMonthExpenses);
        double previousMonthTotal = getTotalExpenses(previousMonthExpenses);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, (float) previousMonthTotal));
        barEntries.add(new BarEntry(1, (float) currentMonthTotal));

        BarDataSet dataSet = new BarDataSet(barEntries, "Сравнение месяцев");
        dataSet.setColors(new int[]{0xFF424242, 0xFF9E9E9E}); // Темно-серый и светло-серый

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Настройка BarChart
        barChart.setScaleEnabled(false); // Запретить изменение размера
        barChart.setDragEnabled(false);  // Запретить перетаскивание

        // Убрать сетку
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);

        // Массив с названиями месяцев в единственном числе (именительный падеж)
        String[] monthNames = new String[]{
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };

        // Получаем индексы текущего и предыдущего месяцев
        Calendar calendar = Calendar.getInstance();
        int currentMonthIndex = calendar.get(Calendar.MONTH); // Текущий месяц
        int previousMonthIndex = (currentMonthIndex - 1 + 12) % 12; // Предыдущий месяц

        // Получаем названия месяцев из массива
        String currentMonthName = monthNames[currentMonthIndex];
        String previousMonthName = monthNames[previousMonthIndex];

        // Добавить названия месяцев
        final String[] months = new String[]{previousMonthName, currentMonthName};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(months.length);
        barChart.getDescription().setEnabled(false);
        barChart.getAxisLeft().setEnabled(true); // Включить левую ось
        barChart.getAxisRight().setEnabled(false); // Отключить правую ось

        // Обновить график
        barChart.invalidate();
    }

    private double getTotalExpenses(List<Expense> expenseList) {
        double total = 0;
        for (Expense expense : expenseList) {
            total += expense.getAmount();
        }
        return total;
    }

    private void setupPieChart() {
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof PieEntry) {
                    PieEntry pieEntry = (PieEntry) e;
                    String category = pieEntry.getLabel(); // Получаем категорию
                    showExpensesForCategory(category); // Показываем расходы для категории
                }
            }

            @Override
            public void onNothingSelected() {
                // Ничего не делаем
            }
        });
    }

    private void showExpensesForCategory(String category) {
        List<Expense> filteredExpenses = new ArrayList<>();
        for (Expense expense : expenses) {
            if (expense.getDescription().equals(category)) {
                filteredExpenses.add(expense);
            }
        }

        // Создаем макет для диалога
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_expenses_list, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.expenses_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Настраиваем адаптер
        ExpenseAdapter adapter = new ExpenseAdapter(filteredExpenses, dateFormat);
        recyclerView.setAdapter(adapter);

        // Создаем Material диалог
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Расходы по категории: " + category)
                .setView(dialogView) // Устанавливаем кастомный макет
                .setPositiveButton("Закрыть", (dialog, which) -> dialog.dismiss())
                .setBackground(getResources().getDrawable(R.drawable.dialog_background, null))
                .show();
    }

    private void setPlannedExpenseNotification(PlannedExpense plannedExpense) {
        Context context = requireContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Создаем Intent для BroadcastReceiver
        Intent intent = new Intent(context, PlannedExpenseReceiver.class);
        intent.putExtra("title", "Напоминание о плановом расходе");
        intent.putExtra("message", "До планового расхода '" + plannedExpense.getDescription() + "' осталось 2 дня.");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                plannedExpense.hashCode(), // Уникальный ID для каждого уведомления
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Устанавливаем уведомление за 2 дня до даты расхода
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(plannedExpense.getDate());
        calendar.add(Calendar.DAY_OF_YEAR, -2); // За 2 дня до даты

        // Устанавливаем AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    private void cancelPlannedExpenseNotification(PlannedExpense plannedExpense) {
        Context context = requireContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Создаем Intent для BroadcastReceiver
        Intent intent = new Intent(context, PlannedExpenseReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                plannedExpense.hashCode(), // Уникальный ID для каждого уведомления
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Отменяем уведомление
        alarmManager.cancel(pendingIntent);
    }
}