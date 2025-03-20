package com.example.car_helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;


public class AddCarFragment extends Fragment {

    private TextInputEditText licensePlateInput, carModelInput, carYearInput, carVinInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_car, container, false);

        licensePlateInput = view.findViewById(R.id.license_plate_input);
        carModelInput = view.findViewById(R.id.car_model_input);
        carYearInput = view.findViewById(R.id.car_year_input);
        carVinInput = view.findViewById(R.id.car_vin_input);

        // Устанавливаем ограничения для полей
        InputUtils.setAlphaInput(licensePlateInput);
        InputUtils.setMaxLength(licensePlateInput, 9);
        InputUtils.setNumericInput(carYearInput);
        InputUtils.setMaxLength(carYearInput, 4);
        InputUtils.setAlphaNumericInput(carVinInput);
        InputUtils.setMaxLength(carVinInput, 17);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String licensePlate = bundle.getString("licensePlate", "");
            String carModel = bundle.getString("carModel", "");
            String carYear = bundle.getString("carYear", "");
            String carVin = bundle.getString("carVin", "");

            // Устанавливаем данные в поля ввода
            licensePlateInput.setText(licensePlate);
            carModelInput.setText(carModel);
            carYearInput.setText(carYear);
            carVinInput.setText(carVin);
        }

        // Обработка нажатия на кнопку "Сохранить"
        Button saveCarButton = view.findViewById(R.id.save_car_button);
        saveCarButton.setOnClickListener(v -> {
            String licensePlate = licensePlateInput.getText().toString().trim();
            String carModel = carModelInput.getText().toString().trim();
            String carYear = carYearInput.getText().toString().trim();
            String carVin = carVinInput.getText().toString().trim();

            // Проверка корректности данных
            if (licensePlate.isEmpty() || carModel.isEmpty() || carYear.isEmpty() || carVin.isEmpty()) {
                Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (licensePlate.length() > 9 || licensePlate.length() < 8) {
                Toast.makeText(getContext(), "Госномер должен быть не более 9 символов", Toast.LENGTH_SHORT).show();
                return;
            }

            if (carYear.length() != 4) {
                Toast.makeText(getContext(), "Год выпуска должен быть корректен", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!carVin.matches("[A-Z0-9]+")) {
                Toast.makeText(getContext(), "VIN должен содержать только цифры и английские буквы", Toast.LENGTH_SHORT).show();
                return;
            }

            // Сохраняем данные
            saveCarData(licensePlate, carModel, carYear, carVin);
            Toast.makeText(getContext(), "Данные сохранены", Toast.LENGTH_SHORT).show();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    private void saveCarData(String licensePlate, String carModel, String carYear, String carVin) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("CarData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("licensePlate", licensePlate);
        editor.putString("carModel", carModel);
        editor.putString("carYear", carYear);
        editor.putString("carVin", carVin);
        editor.apply();
    }
}
