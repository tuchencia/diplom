package com.example.car_helper;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.Location;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.Response;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchOptions;
import com.yandex.mapkit.search.Session;
import com.yandex.mapkit.traffic.TrafficLayer;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.directions.driving.DrivingRouterType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GasFragment extends Fragment {

    private MapView mapView;
    private MapObjectCollection mapObjects;
    private TrafficLayer trafficLayer;
    private UserLocationLayer userLocationLayer;
    private PlacemarkMapObject userLocationMarker;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private PolylineMapObject routeMapObject;
    private GasStationInfo selectedStation;

    private ImageButton trafficButton;
    private ImageButton locationButton;
    private ImageButton zoomInButton;
    private ImageButton zoomOutButton;
    private ImageButton cancelRouteButton;
    private boolean isRouteActive = false;

    private SearchManager searchManager;
    private LocationManager locationManager;
    private final Map<MapObject, GasStationInfo> markersData = new HashMap<>();

    private boolean isTrafficVisible = false;
    private boolean isLocationActive = false;
    private Point lastKnownLocation;
    private boolean shouldCenterOnLocation = false;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationUpdated(@NonNull Location location) {
            lastKnownLocation = location.getPosition();
            if (shouldCenterOnLocation) {
                centerUserLocation();
                updateUserMarker();
                shouldCenterOnLocation = false;
                setLocationActive(false);
            }
        }

        @Override
        public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus) {
            if (locationStatus == LocationStatus.NOT_AVAILABLE) {
                Toast.makeText(requireContext(), "Местоположение недоступно", Toast.LENGTH_SHORT).show();
                showLocationDisabledWarning();
                setLocationActive(false);
            }
        }
    };

    private void updateUserMarker() {
        if (lastKnownLocation != null) {
            if (userLocationMarker != null) {
                mapObjects.remove(userLocationMarker);
            }
            userLocationMarker = mapObjects.addPlacemark(
                    lastKnownLocation,
                    ImageProvider.fromResource(requireContext(), R.drawable.navigation)
            );
        }
    }

    private void showLocationDisabledWarning() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Геолокация отключена")
                .setMessage("Включите геолокацию в настройках устройства")
                .setPositiveButton("Настройки", (d, w) -> openLocationSettings())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void openLocationSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }



    private final MapObjectTapListener universalTapListener = (mapObject, point) -> {
        GasStationInfo info = markersData.get(mapObject);
        if (info != null) {
            showGasStationInfo(info);
        }
        return true;
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MapKitFactory.initialize(requireContext());
        SearchFactory.initialize(requireContext());
        DirectionsFactory.initialize(requireContext());

        View view = inflater.inflate(R.layout.fragment_gas, container, false);

        initViews(view);
        setupMap();
        setupUserLocationLayer();
        setupButtons();

        searchGasStations();

        return view;
    }

    private void initViews(View view) {
        mapView = view.findViewById(R.id.mapView);
        trafficButton = view.findViewById(R.id.trafficButton);
        locationButton = view.findViewById(R.id.locationButton);
        zoomInButton = view.findViewById(R.id.zoomInButton);
        zoomOutButton = view.findViewById(R.id.zoomOutButton);
        cancelRouteButton = view.findViewById(R.id.cancelRouteButton);
    }

    private void setupMap() {
        mapView.getMap().move(
                new CameraPosition(new Point(55.7558, 37.6173), 12.5f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 2),
                null
        );

        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED);
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        trafficLayer = MapKitFactory.getInstance().createTrafficLayer(mapView.getMapWindow());
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE);
        locationManager = MapKitFactory.getInstance().createLocationManager();
    }

    private void setupUserLocationLayer() {
        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);

        userLocationLayer.setObjectListener(new UserLocationObjectListener() {
            @Override
            public void onObjectAdded(UserLocationView userLocationView) {
                userLocationView.getArrow().setIcon(ImageProvider.fromResource(requireContext(), R.drawable.navigation));
                userLocationView.getAccuracyCircle().setFillColor(0x5500FF00);
                centerUserLocation();
            }

            @Override
            public void onObjectRemoved(UserLocationView userLocationView) {}

            @Override
            public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {}
        });

        updateUserLocationAnchor();
    }

    private void setupButtons() {
        trafficButton.setOnClickListener(v -> toggleTraffic());

        locationButton.setOnClickListener(v -> {
            if (isLocationActive) {
                setLocationActive(false);
            }
            else {
                if (checkLocationPermission()) {
                    requestLocation();
                }
                else {
                    showLocationDisabledWarning();
                }
            }
        });

        zoomInButton.setOnClickListener(v -> zoomIn());
        zoomOutButton.setOnClickListener(v -> zoomOut());
        cancelRouteButton.setOnClickListener(v -> clearRoute());
    }
    private void requestLocation(){
        shouldCenterOnLocation = true;
        setLocationActive(true);

    }

    private void zoomIn() {
        float currentZoom = mapView.getMap().getCameraPosition().getZoom();
        mapView.getMap().move(
                new CameraPosition(
                        mapView.getMap().getCameraPosition().getTarget(),
                        currentZoom + 1,
                        0.0f,
                        0.0f
                ),
                new Animation(Animation.Type.SMOOTH, 0.3f),
                null
        );
    }

    private void zoomOut() {
        float currentZoom = mapView.getMap().getCameraPosition().getZoom();
        mapView.getMap().move(
                new CameraPosition(
                        mapView.getMap().getCameraPosition().getTarget(),
                        currentZoom - 1,
                        0.0f,
                        0.0f
                ),
                new Animation(Animation.Type.SMOOTH, 0.3f),
                null
        );
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        } else {
            Toast.makeText(requireContext(),
                    "Для определения местоположения необходимо разрешение",
                    Toast.LENGTH_SHORT).show();
            setLocationActive(false);
        }
    }

    private void showGpsEnableDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Включить GPS")
                .setMessage("Для точного определения местоположения включите GPS")
                .setPositiveButton("Настройки", (dialog, which) -> {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void toggleTraffic() {
        isTrafficVisible = !isTrafficVisible;
        trafficLayer.setTrafficVisible(isTrafficVisible);
        trafficButton.setImageResource(
                isTrafficVisible ? R.drawable.trafficlightcolor : R.drawable.trafficlight
        );
    }

    private void toggleLocation() {
        setLocationActive(!isLocationActive);
    }

    private void setLocationActive(boolean active) {
        isLocationActive = active;
        userLocationLayer.setVisible(active);
        locationButton.setImageResource(
                active ? R.drawable.locationon : R.drawable.locationoff
        );

        if (active) {
            locationManager.subscribeForLocationUpdates(0, 0, 0, true,
                    FilteringMode.ON, locationListener);

            if (lastKnownLocation != null) {
                mapView.getMap().move(
                        new CameraPosition(lastKnownLocation, 15.0f, 0.0f, 0.0f),
                        new Animation(Animation.Type.SMOOTH, 1f),
                        null
                );
            }
        } else {
            locationManager.unsubscribe(locationListener);
        }
    }

    private void centerUserLocation() {
        if (lastKnownLocation != null) {
            mapView.getMap().move(
                    new CameraPosition(lastKnownLocation, 15f, 0f, 0f),
                    new Animation(Animation.Type.SMOOTH, 0.5f),
                    null
            );
        }
    }

    private void updateUserLocationAnchor() {
        float centerX = mapView.getWidth() / 2f;
        float centerY = mapView.getHeight() / 2f;
        float offsetY = mapView.getHeight() * 0.15f;

        userLocationLayer.setAnchor(
                new PointF(centerX, centerY - offsetY),
                new PointF(centerX, centerY - offsetY)
        );
    }

    private List<BoundingBox> getRussianRegions() {
        List<BoundingBox> regions = new ArrayList<>();
        // Регионы поиска (как в вашем исходном коде)
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
        List<BoundingBox> regions = getRussianRegions();

        for (BoundingBox region : regions) {
            searchManager.submit(
                    "АЗС",
                    Geometry.fromBoundingBox(region),
                    new SearchOptions(),
                    new Session.SearchListener() {
                        @Override
                        public void onSearchResponse(@NonNull Response response) {
                            for (int i = 0; i < response.getCollection().getChildren().size(); i++) {
                                Point point = response.getCollection().getChildren().get(i).getObj().getGeometry().get(0).getPoint();
                                if (point != null) {
                                    addGasStationMarker(response, i, point);
                                }
                            }
                        }

                        @Override
                        public void onSearchError(@NonNull Error error) {
                            Log.e("GasFragment", "Ошибка поиска: " + error);
                        }
                    }
            );
        }
    }

    private void addGasStationMarker(Response response, int index, Point point) {
        Map<String, Double> fuelPrices = new HashMap<>();
        fuelPrices.put("АИ-92", 56.49 + (Math.random() * 5));
        fuelPrices.put("АИ-95", 60.37 + (Math.random() * 5));
        fuelPrices.put("АИ-98", 70.41 + (Math.random() * 5));
        fuelPrices.put("ДТ", 70.62 + (Math.random() * 5));

        GasStationInfo info = new GasStationInfo(
                response.getCollection().getChildren().get(index).getObj().getName(),
                response.getCollection().getChildren().get(index).getObj().getDescriptionText(),
                fuelPrices,
                4 + 2 * (int)(Math.random() * 3), // 4, 6 или 8 колонок
                Math.round((3.7 + Math.random() * 1.3) * 10) / 10.0, // Рейтинг 3.7-5.0
                point.getLatitude(),
                point.getLongitude()
        );

        MapObject marker = mapObjects.addPlacemark(
                point,
                ImageProvider.fromResource(requireContext(), R.drawable.station)
        );

        markersData.put(marker, info);
        marker.addTapListener(universalTapListener);
    }

    private void buildRoute() {
        if (isRouteActive) {
            clearRoute();
            return;
        }
        if (lastKnownLocation == null || selectedStation == null) {
            Toast.makeText(requireContext(), "Не удалось определить ваше местоположение", Toast.LENGTH_SHORT).show();
            return;
        }
        clearRoute();

        // Создаем точки маршрута
        List<RequestPoint> routePoints = new ArrayList<>();
        routePoints.add(new RequestPoint(
                lastKnownLocation,
                RequestPointType.WAYPOINT,
                null, // pointContext - может быть null
                null  // entranceInfo - может быть null
        ));
        routePoints.add(new RequestPoint(
                new Point(selectedStation.getLatitude(), selectedStation.getLongitude()),
                RequestPointType.WAYPOINT,
                null, // pointContext - может быть null
                null  // entranceInfo - может быть null
        ));

        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();

        drivingSession = drivingRouter.requestRoutes(
                routePoints,
                drivingOptions,
                vehicleOptions,
                new DrivingSession.DrivingRouteListener() {
                    @Override
                    public void onDrivingRoutes(@NonNull List<DrivingRoute> routes) {
                        if (!routes.isEmpty() && routes.get(0) != null && routes.get(0).getGeometry() != null) {
                            showRoute(routes.get(0));
                        } else {
                            Toast.makeText(requireContext(),
                                    "Не удалось построить маршрут",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onDrivingRoutesError(@NonNull Error error) {
                        Toast.makeText(requireContext(),
                                "Ошибка построения маршрута: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearRoute() {
        if (routeMapObject != null) {
            mapObjects.remove(routeMapObject);
            routeMapObject = null;
        }
        if (drivingSession != null) {
            drivingSession.cancel();
            drivingSession = null;
        }
        isRouteActive = false;
        cancelRouteButton.animate().alpha(0f).setDuration(300).withEndAction(() ->{
           cancelRouteButton.setVisibility(View.GONE);
        }).start();
    }


    private void showRoute(DrivingRoute route) {
        if (route == null || route.getGeometry() == null) {
            Toast.makeText(requireContext(), "Не удалось построить маршрут", Toast.LENGTH_SHORT).show();
            return;
        }

        clearRoute();

        // Добавляем линию маршрута
        routeMapObject = mapObjects.addPolyline(route.getGeometry());
        routeMapObject.setStrokeColor(Color.MAGENTA);
        routeMapObject.setStrokeWidth(5);

        // Показываем время в пути
        double timeInMinutes = route.getMetadata().getWeight().getTime().getValue() / 60.0;
        String timeText = String.format(Locale.getDefault(), "Время в пути: %.0f мин", timeInMinutes);
        Toast.makeText(requireContext(), timeText, Toast.LENGTH_LONG).show();

        isRouteActive = true;
        cancelRouteButton.animate().alpha(1f).setDuration(300).withStartAction(() -> {
            cancelRouteButton.setVisibility(View.VISIBLE);
            cancelRouteButton.setAlpha(0f);
        }).start();
        // Получаем точки маршрута
        List<Point> routePoints = route.getGeometry().getPoints();
        if (routePoints.isEmpty()) {
            return;
        }

        // Находим минимальные и максимальные координаты
        double minLat = routePoints.get(0).getLatitude();
        double maxLat = routePoints.get(0).getLatitude();
        double minLon = routePoints.get(0).getLongitude();
        double maxLon = routePoints.get(0).getLongitude();

        for (Point point : routePoints) {
            minLat = Math.min(minLat, point.getLatitude());
            maxLat = Math.max(maxLat, point.getLatitude());
            minLon = Math.min(minLon, point.getLongitude());
            maxLon = Math.max(maxLon, point.getLongitude());
        }

        // Создаем камеру для отображения всего маршрута
        Point center = new Point(
                (minLat + maxLat) / 2,
                (minLon + maxLon) / 2
        );

        // Рассчитываем подходящий zoom
        float zoom = calculateZoomLevel(minLat, maxLat, minLon, maxLon);

        mapView.getMap().move(
                new CameraPosition(center, zoom, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 1),
                null
        );
    }

    private float calculateZoomLevel(double minLat, double maxLat, double minLon, double maxLon) {
        double latDiff = maxLat - minLat;
        double lonDiff = maxLon - minLon;
        double maxDiff = Math.max(latDiff, lonDiff);

        if (maxDiff < 0.003) return 17.0f;
        if (maxDiff < 0.01) return 16.0f;
        if (maxDiff < 0.03) return 15.0f;
        if (maxDiff < 0.1) return 14.0f;
        if (maxDiff < 0.3) return 13.0f;
        if (maxDiff < 1.0) return 12.0f;
        if (maxDiff < 3.0) return 11.0f;
        if (maxDiff < 10.0) return 10.0f;
        return 9.0f;
    }



    private void showGasStationInfo(GasStationInfo info) {
        selectedStation = info;
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.gas_station_info, null);

        TextView title = dialogView.findViewById(R.id.title);
        TextView address = dialogView.findViewById(R.id.address);
        TextView pumps = dialogView.findViewById(R.id.pumps);
        RatingBar rating = dialogView.findViewById(R.id.rating);
        TextView ratingText = dialogView.findViewById(R.id.ratingText);
        TextView prices = dialogView.findViewById(R.id.prices);

        title.setText(info.getName());
        address.setText(info.getAddress());
        pumps.setText(getString(R.string.pumps_count, info.getPumpsCount()));
        rating.setRating((float) info.getRating());
        ratingText.setText(String.format(Locale.getDefault(), "%.1f/5", info.getRating()));

        StringBuilder pricesText = new StringBuilder("Цены:\n");
        for (Map.Entry<String, Double> entry : info.getFuelPrices().entrySet()) {
            pricesText.append("• ")
                    .append(entry.getKey())
                    .append(": ")
                    .append(String.format(Locale.getDefault(), "%.2f ₽", entry.getValue()))
                    .append("\n");
        }
        prices.setText(pricesText.toString());

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setView(dialogView)
                .setPositiveButton("Закрыть", null)
                .setNeutralButton("Построить маршрут", (d, which) -> buildRoute())
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent));

        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        neutralButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));

    }

    // Методы жизненного цикла
    @Override
    public void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
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
            clearRoute();
            mapObjects.clear();
            markersData.clear();
            locationManager.unsubscribe(locationListener);
            mapView.onFinishTemporaryDetach();
        }
        super.onDestroyView();
    }

    // Внутренний класс для хранения информации о заправке
    private static class GasStationInfo {
        private final String name;
        private final String address;
        private final Map<String, Double> fuelPrices;
        private final int pumpsCount;
        private final double rating;
        private final double latitude;
        private final double longitude;

        public GasStationInfo(String name, String address,
                              Map<String, Double> fuelPrices,
                              int pumpsCount, double rating, double latitude, double longitude) {
            this.name = name;
            this.address = address;
            this.fuelPrices = fuelPrices;
            this.pumpsCount = pumpsCount;
            this.rating = rating;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getName() { return name; }
        public String getAddress() { return address; }
        public Map<String, Double> getFuelPrices() { return fuelPrices; }
        public int getPumpsCount() { return pumpsCount; }
        public double getRating() { return rating; }
        public double getLatitude() {return latitude;}
        public double getLongitude() {return longitude;}
    }
}