package com.example.car_helper;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;
import java.util.regex.Pattern;

public class InputUtils {

    // Ограничение длины текста
    public static void setMaxLength(EditText editText, int maxLength) {
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(maxLength);
        editText.setFilters(filters);
    }

    // Ограничение на ввод только цифр и английских букв
    public static void setAlphaNumericInput(EditText editText) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern pattern = Pattern.compile("[A-Z0-9]*"); // Только английские буквы и цифры
                if (!pattern.matcher(source).matches()) {
                    return ""; // Отклоняем ввод, если символы не соответствуют шаблону
                }
                return null; // Принимаем ввод
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }

    public static void setAlphaInput(EditText editText) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern pattern = Pattern.compile("[А-Я0-9]*"); // Только русские буквы и цифры
                if (!pattern.matcher(source).matches()) {
                    return ""; // Отклоняем ввод, если символы не соответствуют шаблону
                }
                return null; // Принимаем ввод
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }

    // Ограничение на ввод только цифр
    public static void setNumericInput(EditText editText) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern pattern = Pattern.compile("[0-9]*"); // Только цифры
                if (!pattern.matcher(source).matches()) {
                    return ""; // Отклоняем ввод, если символы не соответствуют шаблону
                }
                return null; // Принимаем ввод
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }
}
