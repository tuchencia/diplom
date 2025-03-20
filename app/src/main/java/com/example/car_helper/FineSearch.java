package com.example.car_helper;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FineSearch {

    private static final String SEARCH_URL = "https://avtocod.ru/proverka-shtrafov-gibdd";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    public List<Fine> searchFines(String licensePlate) {
        List<Fine> fines = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();

        // Формируем тело запроса (если сайт использует POST)
        FormBody formBody = new FormBody.Builder()
                .add("licensePlate", licensePlate) // Поле для госномера
                .build();

        // Создаем запрос
        Request request = new Request.Builder()
                .url(SEARCH_URL)
                .post(formBody)
                .header("User-Agent", USER_AGENT) // Указываем User-Agent
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // Парсим HTML-страницу
                Document document = Jsoup.parse(response.body().string());

                // Пример: извлекаем данные о штрафах
                Elements fineElements = document.select("div.fine-item"); // Замените на реальный селектор
                for (Element fineElement : fineElements) {
                    String number = fineElement.select("span.fine-number").text(); // Номер штрафа
                    String date = fineElement.select("span.fine-date").text();     // Дата нарушения
                    String amount = fineElement.select("span.fine-amount").text(); // Сумма штрафа
                    String status = fineElement.select("span.fine-status").text(); // Статус оплаты

                    fines.add(new Fine(number, date, amount, status));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fines;
    }
}
