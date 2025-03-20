package com.example.car_helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
public class AddUserFragment extends Fragment {

    private TextInputEditText userNameInput, driverLicenseInput, driverLicenseExpiryInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_user, container, false);

        userNameInput = view.findViewById(R.id.user_name_input);
        driverLicenseInput = view.findViewById(R.id.driver_license_input);
        driverLicenseExpiryInput = view.findViewById(R.id.driver_license_expiry_input);
        InputUtils.setMaxLength(driverLicenseExpiryInput, 4);

        // Получаем данные из Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            String userName = bundle.getString("userName", "");
            String driverLicense = bundle.getString("driverLicense", "");
            String driverLicenseExpiry = bundle.getString("driverLicenseExpiry", "");

            // Устанавливаем данные в поля ввода
            userNameInput.setText(userName);
            driverLicenseInput.setText(driverLicense);
            driverLicenseExpiryInput.setText(driverLicenseExpiry);
        }
        Button saveButton = view.findViewById(R.id.save_user_button);
        saveButton.setOnClickListener(v -> saveUserData());

        return view;
    }

    private void saveUserData() {
        String userName = userNameInput.getText().toString();
        String driverLicense = driverLicenseInput.getText().toString();
        String driverLicenseExpiry = driverLicenseExpiryInput.getText().toString();
        if (driverLicenseExpiryInput.length() != 4)
        {
            Toast.makeText(getContext(), "Год выпуска должен быть корректен", Toast.LENGTH_SHORT).show();
            return;
        }


        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userName", userName);
        editor.putString("driverLicense", driverLicense);
        editor.putString("driverLicenseExpiry", driverLicenseExpiry);
        editor.apply();

        Toast.makeText(getContext(), "Данные о пользователе сохранены", Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}