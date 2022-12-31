package com.tgbot.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;

@Configuration
public class MyConfiguration implements WebMvcConfigurer {

    @Value("${token}")
    private String token;
    @Value("${folderPath}")
    private String folderPath;
    @Value("${hostPath}")
    private String hostPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/file/**")
                .addResourceLocations("file:///" + folderPath);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() {
        DefaultBotSession defaultBotSession = new DefaultBotSession();
        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(defaultBotSession.getClass());
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            telegramBotsApi.registerBot(new MyTelegramBot(token, folderPath, hostPath));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return telegramBotsApi;
    }

}
