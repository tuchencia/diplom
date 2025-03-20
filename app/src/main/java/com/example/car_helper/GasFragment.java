package com.example.car_helper;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.Session;
import com.yandex.mapkit.traffic.TrafficLayer;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.List;

public class GasFragment extends Fragment {

    private MapView mapView; // Карта
    private Session searchSession;
    private MapObjectCollection mapObjects; // Объекты карты (маркеры)
    private SearchManager searchManager; // Менеджер поиска
    private TrafficLayer trafficLayer; // Слой пробок
    private ImageButton trafficButton; // Кнопка для управления пробками
    private boolean isTrafficVisible = false; // Флаг для отслеживания состояния пробок
    private static final String API_KEY = "4d462aaf-4063-4ad4-881e-91421919792b"; // Замените на ваш API-ключ

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Инициализация MapKit и SearchFactory
        SearchFactory.initialize(requireContext());

        View view = inflater.inflate(R.layout.fragment_gas, container, false);
        mapView = view.findViewById(R.id.mapView);

        // Настройка карты (центрируем на Москве)
        mapView.getMapWindow().getMap().move(
                new CameraPosition(new Point(55.7558, 37.6173), 12.5f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 2),
                null
        );

        // Инициализация SearchManager для поиска заправок
        SearchFactory.initialize(requireContext());
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE);

        // Инициализация коллекции объектов на карте
        mapObjects = mapView.getMapWindow().getMap().getMapObjects().addCollection();

        // Поиск заправок
        searchGasStations();

        // Инициализация слоя пробок
        trafficLayer = MapKitFactory.getInstance().createTrafficLayer(mapView.getMapWindow());
        trafficLayer.setTrafficVisible(isTrafficVisible);

        // Настройка ImageButton для отображения/скрытия пробок
        trafficButton = view.findViewById(R.id.trafficButton);
        trafficButton.setOnClickListener(v -> {
            isTrafficVisible = !isTrafficVisible; // Переключаем состояние
            trafficLayer.setTrafficVisible(isTrafficVisible); // Обновляем слой пробок

            // Меняем иконку в зависимости от состояния
            if (isTrafficVisible) {
                trafficButton.setImageResource(R.drawable.trafficlightcolor); // Иконка "пробки включены"
            } else {
                trafficButton.setImageResource(R.drawable.trafficlight); // Иконка "пробки выключены"
            }
        });

        return view;
    }

    private List<BoundingBox> getRussianRegions() {
        List<com.yandex.mapkit.geometry.BoundingBox> regions = new ArrayList<>();

        regions.add(new BoundingBox(new Point(55.73, 37.55), new Point(55.78, 37.65))); // Центр
        regions.add(new BoundingBox(new Point(55.82, 37.35), new Point(55.91, 37.65))); // Север
        regions.add(new BoundingBox(new Point(55.60, 37.55), new Point(55.73, 37.75))); // Юг
        regions.add(new BoundingBox(new Point(55.70, 37.35), new Point(55.80, 37.55))); // Запад
        regions.add(new BoundingBox(new Point(55.70, 37.65), new Point(55.80, 37.85))); // Восток
        regions.add(new BoundingBox(new Point(55.82, 37.35), new Point(55.91, 37.55))); // Северо-Запад
        regions.add(new BoundingBox(new Point(55.82, 37.65), new Point(55.91, 37.85))); // Северо-Восток
        regions.add(new BoundingBox(new Point(55.60, 37.35), new Point(55.73, 37.55))); // Юго-Запад
        regions.add(new BoundingBox(new Point(55.60, 37.65), new Point(55.73, 37.85))); // Юго-Восток
        regions.add(new BoundingBox(new Point(55.73, 37.55), new Point(55.78, 37.65))); // Центральный округ
        regions.add(new BoundingBox(new Point(55.82, 37.35), new Point(55.91, 37.65))); // Северный округ
        regions.add(new BoundingBox(new Point(55.60, 37.55), new Point(55.73, 37.75))); // Южный округ
        regions.add(new BoundingBox(new Point(55.70, 37.35), new Point(55.80, 37.55))); // Западный округ
        regions.add(new BoundingBox(new Point(55.70, 37.65), new Point(55.80, 37.85))); // Восточный округ
        regions.add(new BoundingBox(new Point(55.82, 37.35), new Point(55.91, 37.55))); // Северо-Западный округ
        regions.add(new BoundingBox(new Point(55.82, 37.65), new Point(55.91, 37.85))); // Северо-Восточный округ
        regions.add(new BoundingBox(new Point(55.60, 37.35), new Point(55.73, 37.55))); // Юго-Западный округ
        regions.add(new BoundingBox(new Point(55.60, 37.65), new Point(55.73, 37.85))); // Юго-Восточный округ
        regions.add(new BoundingBox(new Point(55.73, 37.55), new Point(55.78, 37.65))); // Центральный район
        regions.add(new BoundingBox(new Point(55.82, 37.35), new Point(55.91, 37.65))); // Северный район

        // Московская область (8 участков)
        regions.add(new BoundingBox(new Point(55.91, 36.80), new Point(56.50, 38.00))); // Север
        regions.add(new BoundingBox(new Point(54.80, 37.00), new Point(55.60, 39.00))); // Юг
        regions.add(new BoundingBox(new Point(55.50, 36.00), new Point(56.00, 37.50))); // Запад
        regions.add(new BoundingBox(new Point(55.50, 38.00), new Point(56.00, 39.50))); // Восток
        regions.add(new BoundingBox(new Point(55.91, 36.80), new Point(56.50, 37.50))); // Северо-Запад
        regions.add(new BoundingBox(new Point(55.91, 37.50), new Point(56.50, 38.00))); // Северо-Восток
        regions.add(new BoundingBox(new Point(54.80, 36.80), new Point(55.60, 37.50))); // Юго-Запад
        regions.add(new BoundingBox(new Point(54.80, 37.50), new Point(55.60, 39.00))); // Юго-Восток

        // Города-миллионники
        regions.add(new BoundingBox(new Point(59.80, 30.15), new Point(60.05, 30.50))); // Санкт-Петербург
        regions.add(new BoundingBox(new Point(54.90, 82.80), new Point(55.10, 83.10))); // Новосибирск
        regions.add(new BoundingBox(new Point(56.70, 60.50), new Point(56.90, 60.80))); // Екатеринбург
        regions.add(new BoundingBox(new Point(55.70, 48.90), new Point(55.90, 49.20))); // Казань
        regions.add(new BoundingBox(new Point(56.20, 43.90), new Point(56.40, 44.20))); // Нижний Новгород
        regions.add(new BoundingBox(new Point(55.10, 61.30), new Point(55.30, 61.60))); // Челябинск
        regions.add(new BoundingBox(new Point(53.10, 50.00), new Point(53.30, 50.30))); // Самара
        regions.add(new BoundingBox(new Point(54.90, 73.20), new Point(55.10, 73.50))); // Омск
        regions.add(new BoundingBox(new Point(47.10, 39.50), new Point(47.30, 39.80))); // Ростов-на-Дону
        regions.add(new BoundingBox(new Point(54.60, 55.80), new Point(54.80, 56.10))); // Уфа

        regions.add(new BoundingBox(new Point(55.4, 36.5), new Point(55.9, 38.5))); // Весь регион
        return regions;
    }

    private void searchGasStations() {
        List<com.yandex.mapkit.geometry.BoundingBox> regions = getRussianRegions();

        for (com.yandex.mapkit.geometry.BoundingBox region : regions) {
            Session searchSession = searchManager.submit(
                    "АЗС", // Поисковый запрос
                    Geometry.fromBoundingBox(region),     // Границы региона
                    new SearchOptions(), // Дополнительные параметры поиска
                    new Session.SearchListener() {
                        @Override
                        public void onSearchResponse(@NonNull Response response) {
                            // Обработка результатов поиска
                            response.getCollection().getChildren().forEach(item -> {
                                Point point = item.getObj().getGeometry().get(0).getPoint();
                                if (point != null) {
                                    // Получение имени объекта
                                    String name = item.getObj().getName();
                                    String address = item.getObj().getDescriptionText();

                                    // Создаем объект GasStationInfo для хранения данных о заправке
                                    GasStationInfo gasStationInfo = new GasStationInfo(name, address);

                                    // Добавление маркера на карту
                                    MapObject marker = mapObjects.addPlacemark(
                                            point,
                                            ImageProvider.fromResource(requireContext(), R.drawable.station) // Стандартная иконка
                                    );
                                    marker.setUserData(gasStationInfo); // Сохраняем данные о заправке

                                    // Добавляем обработчик кликов на маркер
                                    marker.addTapListener(new MapObjectTapListener() {
                                        @Override
                                        public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
                                            // Получаем данные о заправке
                                            GasStationInfo gasStationInfo = (GasStationInfo) mapObject.getUserData();

                                            // Отображаем информацию о заправке в AlertDialog
                                            if (gasStationInfo != null) {
                                                showGasStationInfo(gasStationInfo);
                                            }
                                            return true; // Возвращаем true, чтобы событие не передавалось дальше
                                        }
                                    });

                                    // Логирование для отладки
                                    Log.d("GasFragment", "Marker added: " + gasStationInfo.getName() + " at " + point);
                                }
                            });
                        }

                        @Override
                        public void onSearchError(@NonNull Error error) {
                            // Обработка ошибки поиска
                            error.isValid();
                        }
                    }
            );
        }
    }

    private void showGasStationInfo(GasStationInfo gasStationInfo) {
        // Создаем AlertDialog для отображения информации о заправке
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Информация о заправке");
        builder.setMessage(
                "Название: " + gasStationInfo.getName() + "\n" +
                        "Адрес: " + gasStationInfo.getAddress()
        );
        builder.setPositiveButton("ОК", (dialog, which) -> dialog.dismiss()); // Кнопка "ОК"
        builder.setCancelable(true); // Возможность закрыть диалог

        // Показываем диалог
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        MapKitFactory.getInstance().onStart();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) {
            mapView.onFinishTemporaryDetach();
        }
        super.onDestroyView();
    }

    // Внутренний класс для хранения информации о заправке
    private static class GasStationInfo {
        private final String name;
        private final String address;

        public GasStationInfo(String name, String address) {
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }
    }
}