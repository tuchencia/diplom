package com.example.car_helper;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebViewFragment extends Fragment {

    private WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_webview, container, false);

        // Находим WebView
        webView = view.findViewById(R.id.webview);
        onBackPressed();
        // Проверяем подключение к интернету
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            // Настраиваем WebView
            setupWebView();

            // Загружаем сайт
            webView.loadUrl("https://www.startmycar.com");
        } else {
            // Показываем сообщение об ошибке
            Toast.makeText(requireContext(), "Нет подключения к интернету", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    // Настройка WebView
    private void setupWebView() {
        webView.setWebViewClient(new WebViewClient()); // Открываем ссылки внутри WebView
        webView.getSettings().setJavaScriptEnabled(true); // Включаем поддержку JavaScript
        webView.getSettings().setDomStorageEnabled(true); // Включаем DOM Storage (если нужно)
    }

    // Обработка нажатия кнопки "Назад"
    public boolean onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack(); // Возвращаемся на предыдущую страницу
            return true;
        }
        return false;
    }
}
