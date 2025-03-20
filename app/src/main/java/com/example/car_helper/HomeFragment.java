package com.example.car_helper;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

public class HomeFragment extends Fragment {

    private TextView licensePlateText, carModelText, carYearText, carVinText;
    private TextView driverNameText, driverLicenseText, driverLicenseExpiryText, licenseText;
    private ImageView carImage;
    private TextView finesText; // TextView для отображения штрафов
    private Button searchFinesButton; // Кнопка для поиска штрафов
    private ImageButton toggleVinVisibility; // Кнопка "глаза"
    private SharedPreferences sharedPreferences;
    private TextView averagePriceText;
    private boolean isVinVisible = true;

    public HomeFragment() {
        // Пустой конструктор
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        // Находим все TextView
        licensePlateText = view.findViewById(R.id.license_plate_text);
        carModelText = view.findViewById(R.id.car_model_text);
        carYearText = view.findViewById(R.id.car_year_text);
        carVinText = view.findViewById(R.id.car_vin_text);
        driverNameText = view.findViewById(R.id.driver_name_text);
        driverLicenseText = view.findViewById(R.id.driver_license_text);
        driverLicenseExpiryText = view.findViewById(R.id.driver_license_expiry_text);
        licenseText = view.findViewById(R.id.license);

        // Находим ImageView
        carImage = view.findViewById(R.id.car_image);

        // Загружаем данные и обновляем UI
        loadCarData();
        loadUserData();
        loadVehicleImage(); // Загружаем изображение транспортного средства

        finesText = view.findViewById(R.id.fines_text);
        searchFinesButton = view.findViewById(R.id.search_fines_button);
        averagePriceText = view.findViewById(R.id.average_price_text);

        // Добавляем TextWatcher для поля carModelText
        carModelText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Не используется
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Не используется
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Получаем модель автомобиля из текстового поля
                String carModel = s.toString().replace("Модель: ", "").trim();

                // Получаем среднюю цену из статических данных
                double averagePrice = CarPriceData.getAveragePrice(carModel);

                // Форматируем цену, убирая .0
                String formattedPrice = String.format("%.0f", averagePrice);

                // Обновляем TextView с средней ценой
                if (averagePrice > 0) {
                    averagePriceText.setText(formattedPrice + " руб.\n" + "Средняя цена вашего авто");
                } else {
                    averagePriceText.setText("Средняя цена: не указана");
                }
            }
        });

        // Обработка нажатия на кнопку поиска штрафов
        finesText.setVisibility(GONE);

        carVinText = view.findViewById(R.id.car_vin_text);
        toggleVinVisibility = view.findViewById(R.id.toggle_vin_visibility);

        // Инициализируем SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        // Загружаем состояние видимости VIN
        isVinVisible = sharedPreferences.getBoolean("isVinVisible", true);

        // Устанавливаем начальное состояние
        updateVinVisibility();

        // Обработка нажатия на кнопку "глаза"
        toggleVinVisibility.setOnClickListener(v -> {
            isVinVisible = !isVinVisible; // Меняем состояние флага
            updateVinVisibility(); // Обновляем видимость VIN
            saveVinVisibilityState(); // Сохраняем состояние
        });




        // Обработка нажатия на кнопку поиска штрафов
        searchFinesButton.setOnClickListener(v -> {
            // Получаем госномер из поля license_plate_text
            String licensePlate = licensePlateText.getText().toString().replace("Госномер: ", "").trim();

            // Проверяем, не пустое ли поле
            if (licensePlate.isEmpty()) {
                // Показываем предупреждение
                Toast.makeText(getContext(), "Введите госномер автомобиля", Toast.LENGTH_SHORT).show();

                return; // Прерываем выполнение
            }


            // Запускаем поиск штрафов в фоновом потоке
            new Thread(() -> {
                FineSearch fineSearch = new FineSearch();
                List<Fine> fines = fineSearch.searchFines(licensePlate);

                // Обновляем UI в основном потоке
                requireActivity().runOnUiThread(() -> {
                    if (fines.isEmpty()) {
                        finesText.setText("Штрафы не найдены");
                    } else {
                        StringBuilder finesInfo = new StringBuilder();
                        for (Fine fine : fines) {
                            finesInfo.append("Номер: ").append(fine.getNumber()).append("\n")
                                    .append("Дата: ").append(fine.getDate()).append("\n")
                                    .append("Сумма: ").append(fine.getAmount()).append("\n")
                                    .append("Статус: ").append(fine.getStatus()).append("\n\n");
                        }
                        finesText.setText(finesInfo.toString());
                    }

                    // Показываем finesText после получения данных
                    finesText.setVisibility(VISIBLE);
                });
            }).start();
        });

        // Обработка нажатия на изображение автомобиля
        carImage.setOnClickListener(v -> showVehicleTypeDialog());

        // Находим кнопки
        Button addCarButton = view.findViewById(R.id.add_car_button);
        Button addUserButton = view.findViewById(R.id.add_user_button);

        // Данные авто
        if (licensePlateText.getText().toString().isEmpty()){
            addCarButton.setText("Добавить автомобиль");
        }
        else {
            addCarButton.setText("Изменить данные");
        }

        // Данные пользователя
        if (driverNameText.getText().toString().isEmpty() && driverLicenseText.getText().toString().isEmpty() && driverLicenseExpiryText.getText().toString().isEmpty()){
            addUserButton.setText("Добавить пользователя");
            licenseText.setVisibility(VISIBLE);
        }
        else {
            addUserButton.setText("Изменить данные");
            licenseText.setVisibility(GONE);
        }




        // Обработка нажатия на кнопку "Добавить автомобиль"
        addCarButton.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // Создаем фрагмент добавления автомобиля
            AddCarFragment addCarFragment = new AddCarFragment();

            // Передаем данные через Bundle
            Bundle bundle = new Bundle();
            bundle.putString("licensePlate", licensePlateText.getText().toString().replace("Госномер: ", ""));
            bundle.putString("carModel", carModelText.getText().toString().replace("Модель: ", ""));
            bundle.putString("carYear", carYearText.getText().toString().replace("Год выпуска: ", ""));
            bundle.putString("carVin", carVinText.getText().toString().replace("VIN: ", ""));
            addCarFragment.setArguments(bundle);

            // Заменяем фрагмент
            transaction.replace(R.id.fragment_container, addCarFragment);
            transaction.addToBackStack(null); // Добавляем транзакцию в стек
            transaction.commit();
        });

        // Обработка нажатия на кнопку "Добавить пользователя"
        addUserButton.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // Создаем фрагмент добавления пользователя
            AddUserFragment addUserFragment = new AddUserFragment();

            // Передаем данные через Bundle
            Bundle bundle = new Bundle();
            bundle.putString("userName", driverNameText.getText().toString().replace("Водитель: ", ""));
            bundle.putString("driverLicense", driverLicenseText.getText().toString().replace("Номер ВУ: ", ""));
            bundle.putString("driverLicenseExpiry", driverLicenseExpiryText.getText().toString().replace("Действительны до: ", ""));
            addUserFragment.setArguments(bundle);

            // Заменяем фрагмент
            transaction.replace(R.id.fragment_container, addUserFragment);
            transaction.addToBackStack(null); // Добавляем транзакцию в стек
            transaction.commit();
        });

        Button problemButton = view.findViewById(R.id.problem_button);

        problemButton.setOnClickListener(v -> {
            // Проверяем подключение к интернету
            if (NetworkUtils.isNetworkAvailable(requireContext())) {
                // Создаем новый фрагмент WebViewFragment
                WebViewFragment webViewFragment = new WebViewFragment();

                // Заменяем текущий фрагмент на WebViewFragment
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, webViewFragment);
                transaction.addToBackStack(null); // Добавляем транзакцию в стек
                transaction.commit();
            } else {
                // Показываем сообщение об ошибке
                Toast.makeText(requireContext(), "Нет подключения к интернету", Toast.LENGTH_SHORT).show();
            }
        });

//        // Обработка нажатия на кнопку "Добавить пользователя"
//        addUserButton.setOnClickListener(v -> {
//            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
//            FragmentTransaction transaction = fragmentManager.beginTransaction();
//            transaction.replace(R.id.fragment_container, new AddUserFragment());
//            transaction.addToBackStack(null); // Добавляем транзакцию в стек
//            transaction.commit();
//        });

        return view;
    }
    private void updateVinVisibility() {
        if (isVinVisible) {
            // Показываем VIN
            carVinText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_CLASS_TEXT);
            toggleVinVisibility.setImageResource(R.drawable.baseline_visibility_24); // Иконка "глаз открыт"
        } else {
            // Скрываем VIN (заменяем на звездочки)
            carVinText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
            toggleVinVisibility.setImageResource(R.drawable.baseline_visibility_off_24); // Иконка "глаз закрыт"
        }
    }

    // Сохраняет состояние видимости VIN в SharedPreferences
    private void saveVinVisibilityState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isVinVisible", isVinVisible);
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Загружаем данные и обновляем UI
        loadCarData();
        loadUserData();
        loadVehicleImage(); // Обновляем изображение транспортного средства
    }

    // Загрузка данных об автомобиле
    private void loadCarData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("CarData", Context.MODE_PRIVATE);
        String licensePlate = sharedPreferences.getString("licensePlate", "");
        String carModel = sharedPreferences.getString("carModel", "");
        String carYear = sharedPreferences.getString("carYear", "");
        String carVin = sharedPreferences.getString("carVin", "");

        licensePlateText.setText("Госномер: " + licensePlate);
        carModelText.setText("Модель: " + carModel);
        carYearText.setText("Год выпуска: " + carYear);
        carVinText.setText("VIN: " + carVin);


    }

    // Загрузка данных о пользователе
    private void loadUserData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");
        String driverLicense = sharedPreferences.getString("driverLicense", "");
        String driverLicenseExpiry = sharedPreferences.getString("driverLicenseExpiry", "");

        driverNameText.setText("Водитель: " + userName);
        driverLicenseText.setText("Номер ВУ: " + driverLicense);
        driverLicenseExpiryText.setText("Действительны до: " + driverLicenseExpiry);

    }

    // Показ диалога для выбора типа транспортного средства
    private void showVehicleTypeDialog() {
        // Загружаем кастомный макет
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_vehicle_type, null);

        // Находим RadioGroup и RadioButton
        RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group);
        RadioButton radioCar = dialogView.findViewById(R.id.radio_car);
        RadioButton radioTruck = dialogView.findViewById(R.id.radio_truck);
        RadioButton radioMotorcycle = dialogView.findViewById(R.id.radio_motorcycle);

        // Загружаем сохраненный тип транспортного средства
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("VehicleData", Context.MODE_PRIVATE);
        String savedVehicleType = sharedPreferences.getString("vehicleType", "Легковой автомобиль");

        // Устанавливаем выбранный RadioButton
        switch (savedVehicleType) {
            case "Легковой автомобиль":
                radioCar.setChecked(true);
                break;
            case "Грузовик":
                radioTruck.setChecked(true);
                break;
            case "Мотоцикл":
                radioMotorcycle.setChecked(true);
                break;
        }

        // Создаем AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView); // Устанавливаем кастомный макет


        // Обработка нажатия на кнопку "ОК"
        builder.setPositiveButton("ОК", (dialog, which) -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            String selectedVehicleType = "";

            // Определяем выбранный тип транспортного средства
            if (selectedId == R.id.radio_car) {
                selectedVehicleType = "Легковой автомобиль";
            } else if (selectedId == R.id.radio_truck) {
                selectedVehicleType = "Грузовик";
            } else if (selectedId == R.id.radio_motorcycle) {
                selectedVehicleType = "Мотоцикл";
            }

            // Сохраняем выбранный тип и обновляем изображение
            saveVehicleType(selectedVehicleType);
            updateVehicleImage(selectedVehicleType);
        });

        // Обработка нажатия на кнопку "Отмена"
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        // Показываем диалог
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Сохранение типа транспортного средства
    private void saveVehicleType(String vehicleType) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("VehicleData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("vehicleType", vehicleType);
        editor.apply();
    }

    // Загрузка изображения транспортного средства
    private void loadVehicleImage() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("VehicleData", Context.MODE_PRIVATE);
        String vehicleType = sharedPreferences.getString("vehicleType", "Легковой автомобиль");
        updateVehicleImage(vehicleType); // Обновляем изображение
    }

    // Обновление изображения в зависимости от типа транспортного средства
    private void updateVehicleImage(String vehicleType) {
        switch (vehicleType) {
            case "Легковой автомобиль":
                carImage.setImageResource(R.drawable.auto); // Иконка легкового автомобиля
                break;
            case "Грузовик":
                carImage.setImageResource(R.drawable.truck); // Иконка грузовика
                break;
            case "Мотоцикл":
                carImage.setImageResource(R.drawable.moto); // Иконка мотоцикла
                break;
            default:
                carImage.setImageResource(R.drawable.auto); // Иконка по умолчанию
                break;
        }
    }
}