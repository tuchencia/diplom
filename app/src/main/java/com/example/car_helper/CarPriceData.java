package com.example.car_helper;
import java.util.HashMap;
import java.util.Map;

public class CarPriceData {

    private static final Map<String, Double> carPrices = new HashMap<>();

    static {
        // Добавляем статические данные о ценах
        carPrices.put("Lada Granta", 800_000.0);
        carPrices.put("Lada Vesta", 1_200_000.0);
        carPrices.put("Lada Niva", 1_000_000.0);
        carPrices.put("Lada Largus", 1_100_000.0);
        carPrices.put("Kia Rio", 1_500_000.0);
        carPrices.put("Hyundai Solaris", 1_400_000.0);
        carPrices.put("Renault Logan", 1_000_000.0);
        carPrices.put("Renault Duster", 1_600_000.0);
        carPrices.put("Toyota Camry", 2_500_000.0);
        carPrices.put("Toyota RAV4", 3_000_000.0);
        carPrices.put("Toyota Corolla", 1_800_000.0);
        carPrices.put("Volkswagen Polo", 1_300_000.0);
        carPrices.put("Volkswagen Tiguan", 2_800_000.0);
        carPrices.put("Skoda Rapid", 1_400_000.0);
        carPrices.put("Skoda Octavia", 2_000_000.0);
        carPrices.put("Nissan Qashqai", 2_200_000.0);
        carPrices.put("Nissan X-Trail", 2_700_000.0);
        carPrices.put("Mitsubishi Outlander", 2_500_000.0);
        carPrices.put("Mazda CX-5", 2_600_000.0);
        carPrices.put("BMW X5", 5_000_000.0);
        carPrices.put("BMW 3 Series", 3_500_000.0);
        carPrices.put("Audi A4", 3_000_000.0);
        carPrices.put("Audi Q5", 4_500_000.0);
        carPrices.put("Mercedes-Benz C-Class", 4_000_000.0);
        carPrices.put("Mercedes-Benz GLC", 5_500_000.0);
        carPrices.put("Ford Focus", 1_500_000.0);
        carPrices.put("Ford Kuga", 2_300_000.0);
        carPrices.put("Chevrolet Niva", 1_100_000.0);
        carPrices.put("Chevrolet Cruze", 1_400_000.0);
        carPrices.put("Hyundai Creta", 1_800_000.0);
        carPrices.put("Hyundai Tucson", 2_400_000.0);
        carPrices.put("Kia Sportage", 2_300_000.0);
        carPrices.put("Kia Seltos", 1_900_000.0);
        carPrices.put("Subaru Forester", 2_800_000.0);
        carPrices.put("Subaru Outback", 3_200_000.0);
        carPrices.put("Lexus RX", 6_000_000.0);
        carPrices.put("Lexus NX", 4_500_000.0);
        carPrices.put("Volvo XC60", 4_000_000.0);
        carPrices.put("Volvo XC90", 6_500_000.0);
        carPrices.put("Land Rover Discovery", 7_000_000.0);
        carPrices.put("Land Rover Range Rover", 10_000_000.0);
        carPrices.put("Porsche Cayenne", 8_000_000.0);
        carPrices.put("Porsche Macan", 6_500_000.0);
        carPrices.put("Jeep Grand Cherokee", 5_000_000.0);
        carPrices.put("Jeep Wrangler", 4_500_000.0);
        carPrices.put("Haval Jolion", 1_700_000.0);
        carPrices.put("Haval F7", 2_000_000.0);
        carPrices.put("Geely Coolray", 1_600_000.0);
        carPrices.put("Geely Atlas", 1_900_000.0);
        carPrices.put("Chery Tiggo 7", 1_800_000.0);
        carPrices.put("Chery Tiggo 8", 2_200_000.0);
        carPrices.put("UAZ Patriot", 1_500_000.0);
        carPrices.put("UAZ Hunter", 1_200_000.0);
        carPrices.put("Suzuki Vitara", 2_000_000.0);
        carPrices.put("Suzuki Jimny", 2_300_000.0);
        carPrices.put("Peugeot 408", 1_600_000.0);
        carPrices.put("Peugeot 3008", 2_400_000.0);
        carPrices.put("Citroen C4", 1_500_000.0);
        carPrices.put("Citroen C5 Aircross", 2_200_000.0);
        carPrices.put("Opel Astra", 400_000.0);
        carPrices.put("Opel Mokka", 1_800_000.0);
        carPrices.put("Fiat Ducato", 2_500_000.0);
        carPrices.put("Fiat 500", 1_600_000.0);
        carPrices.put("Datsun on-DO", 800_000.0);
        carPrices.put("Datsun mi-DO", 850_000.0);
        carPrices.put("Zotye T600", 1_200_000.0);
        carPrices.put("Zotye T700", 1_500_000.0);
        carPrices.put("Lifan X60", 900_000.0);
        carPrices.put("Lifan Solano", 800_000.0);
        carPrices.put("ВАЗ 2107", 300_000.0);
        carPrices.put("ВАЗ 2114", 350_000.0);
        carPrices.put("ВАЗ 2121 Нива", 500_000.0);
        carPrices.put("ГАЗ Волга", 400_000.0);
        carPrices.put("ГАЗ Газель", 1_000_000.0);
        carPrices.put("УАЗ Буханка", 1_100_000.0);
        carPrices.put("УАЗ Патриот", 1_500_000.0);
    }

    // Метод для получения средней цены по модели
    public static double getAveragePrice(String model) {
        return carPrices.getOrDefault(model, 0.0);
    }
}
