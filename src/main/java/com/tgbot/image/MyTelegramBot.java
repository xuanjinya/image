package com.tgbot.image;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class MyTelegramBot extends TelegramLongPollingBot {

    private String token;
    private String folderPath;
    private String fileUrl;
    private String hostPath;

    @Override
    public String getBotUsername() {
        return "MyTelegramBot";
    }

    @Override
    public String getBotToken() {
        return token;
    }

    public MyTelegramBot(String token, String folderPath, String hostPath) {
        this.token = token;
        this.folderPath = folderPath;
        this.hostPath = hostPath;
        this.fileUrl = "https://api.telegram.org/file/bot" + token + "/";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            // 响应的消息对象
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            if (message.hasPhoto()) {
                // 获取最大尺寸的图片
                List<PhotoSize> photos = update.getMessage().getPhoto();
                PhotoSize largestPhoto = photos.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
                assert largestPhoto != null;
                String fileId = largestPhoto.getFileId();
                // 发送响应消息
                String imgUrl = downloadAndSaveImage(fileId, fileUrl, folderPath);
                sendMessage.setText(imgUrl);
            } else {
                sendMessage.setText("没有获取到图片！！！");
            }
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * 从tg服务器下载文件到本地
     * */
    private String downloadAndSaveImage(String fileId, String fileUrl, String folderPath) {
        String fileName = "";
        try {
            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            File file = execute(getFile);
            String filePath = file.getFilePath();
            fileName = generateMeaningfulFileName(filePath);
            URL url = new URL(fileUrl + filePath);
            try (InputStream inputStream = url.openStream();
                 FileOutputStream outputStream = new FileOutputStream(folderPath + fileName)) {
                int bytesRead;
                byte[] buffer = new byte[1024];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

        } catch (Exception e) {
            System.err.println("下载文件时出错: " + e.getMessage());
        }
        return hostPath + "/file/" + fileName;
    }

    /*
     * 生成新的文件名
     * */
    private String generateMeaningfulFileName(String filePath) {
        String fileExtension = filePath.substring(filePath.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString();
        if (fileExtension.equals(".jpg") || fileExtension.equals(".jpeg") || fileExtension.equals(".png") || fileExtension.equals(".gif")) {
            fileName = "image-" + fileName;
        }
        return fileName + fileExtension;
    }

}
