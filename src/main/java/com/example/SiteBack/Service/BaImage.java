package com.example.SiteBack.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class BaImage {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final String IMAGE_FOLDER = "Image";
    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 5MB

    //获取或创建一个 本地图片 仓库
    public Path getOrCreateImageFolder() throws IOException {
        Path imageFolderPath = Paths.get(System.getProperty("user.dir"), IMAGE_FOLDER);
        return Files.createDirectories(imageFolderPath); // createDirectories已经处理了存在的情况
    }



//---------------------  ↓Outside Function   ---------------------------------------------------------------------------------


    //保存用户的图片
    public boolean saveImageByUserNameAndTime(MultipartFile file, String userName) {

        try {
            validateFile(file);
            Path userFolder = getOrCreateUserFolder(userName);
            String fileName = generateFileName(userName, file);
            file.transferTo(userFolder.resolve(fileName).toFile());
            log.info("User image saved successfully at: {}", userFolder);
            return true;
        } catch (IOException | IllegalArgumentException e) {
            log.error("Error saving user image for user: {}", userName, e);
            return false;
        }
    }



    // 读取用户图片方法
    public Path readUserImage(String userName, String fileName) throws IOException {
        // 获取用户文件夹路径
        Path userFolder = getOrCreateUserFolder(userName);

        // 拼接文件路径
        Path filePath = userFolder.resolve(fileName);

        if (!Files.exists(filePath)) {
            log.error("Image file not found for user: " + userName);
            throw new IOException("Image file not found.");
        }

        return filePath;
    }



//------------------------   ↓Inside function  -----------------------------------------------------------------------------------------------


    //验证图片
    private void validateFile(MultipartFile file) {
        // 1. 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        // 2. 检查文件大小是否超限
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        // 3. 检查文件扩展名是否合法
        String extension = getFileExtension(file).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension.substring(1))) { // 去掉开头的"."再检查
            throw new IllegalArgumentException(
                    "仅支持以下图片格式: " + String.join(", ", ALLOWED_EXTENSIONS)
            );
        }
    }

    private String generateFileName(String userName, MultipartFile file) {
        return userName + "_" + getCurrentTimestamp() + getFileExtension(file);
    }


    // 获取用户文件夹，如果不存在则创建
    private Path getOrCreateUserFolder(String userName) throws IOException {
        // 获取项目根目录下的 Image 文件夹
        Path imageFolder = getOrCreateImageFolder();

        // 使用用户名创建文件夹
        Path userFolder = imageFolder.resolve(userName);

        // 如果用户文件夹不存在，自动创建
        if (!Files.exists(userFolder)) {
            Files.createDirectories(userFolder);
            log.info("User folder created: " + userFolder);
        }

        return userFolder;
    }

    // 获取文件扩展名
    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 防止路径遍历攻击（如 "../../malicious.exe"）
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("文件名包含非法路径字符");
        }

        // 提取扩展名
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex < 0) {
            throw new IllegalArgumentException("文件缺少扩展名");
        }

        return originalFilename.substring(dotIndex); // 返回带点的扩展名（如 ".jpg"）
    }

    // 获取当前时间戳
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return sdf.format(new Date());
    }
}
