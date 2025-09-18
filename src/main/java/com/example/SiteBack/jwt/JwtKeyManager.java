package com.example.SiteBack.jwt;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * JWT密钥管理器，用于生成、加载和管理RSA密钥对
 * 自动检查并创建不存在的密钥文件
 */
@Slf4j
@Getter
@Component  // 声明为Spring组件，由Spring容器管理
public class JwtKeyManager {
    // 定义私钥和公钥的默认存储路径
    private static final String PRIVATE_KEY_PATH = "config/jwt/private.pem";
    private static final String PUBLIC_KEY_PATH = "config/jwt/public.pem";

    /**
     * -- GETTER --
     *  获取私钥
     *
     */
    private PrivateKey privateKey;  // 存储私钥
    /**
     * -- GETTER --
     *  获取公钥
     *
     */
    private PublicKey publicKey;    // 存储公钥

    /**
     * 初始化方法，在Bean创建后自动调用
     * 检查密钥文件是否存在，不存在则生成新的密钥对
     * 然后加载密钥到内存
     */
    @PostConstruct
    public void init() {
        try {
            File privateFile = new File(PRIVATE_KEY_PATH);
            File publicFile = new File(PUBLIC_KEY_PATH);

            // 如果私钥或公钥文件不存在，则生成新的密钥对
            if (!privateFile.exists() || !publicFile.exists()) {
                generateAndSaveKeyPair();
                log.trace("[JwtKeyManager] 已生成新密钥对并保存");
            }log.info("[JwtKeyManager] 使用密钥路径: " + PRIVATE_KEY_PATH);

            // 加载密钥到内存
            this.privateKey = loadPrivateKey(PRIVATE_KEY_PATH);
            this.publicKey = loadPublicKey();
            System.out.println("[JwtKeyManager] 已从文件加载密钥");

        } catch (Exception e) {
            throw new RuntimeException("JWT 密钥初始化失败", e);
        }
    }

    /**
     * 生成RSA密钥对并保存到文件
     * @throws Exception 密钥生成或保存失败时抛出异常
     */
    private void generateAndSaveKeyPair() throws Exception {
        // 使用RSA算法生成2048位的密钥对
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        // 保存私钥和公钥到PEM格式文件
        savePemFile(PRIVATE_KEY_PATH, "PRIVATE KEY", keyPair.getPrivate().getEncoded());
        savePemFile(PUBLIC_KEY_PATH, "PUBLIC KEY", keyPair.getPublic().getEncoded());

    }

    /**
     * 将密钥保存为PEM格式文件
     * @param path 文件路径
     * @param type 密钥类型(PRIVATE KEY或PUBLIC KEY)
     * @param keyBytes 密钥字节数组
     * @throws IOException 文件操作失败时抛出
     */
    private void savePemFile(String path, String type, byte[] keyBytes) throws IOException {
        // 将密钥编码为Base64字符串
        String base64 = Base64.getEncoder().encodeToString(keyBytes);
        // 构建PEM格式内容
        String pem = "-----BEGIN " + type + "-----\n"
                + chunkString(base64, 64)  // 每64字符换行
                + "-----END " + type + "-----\n";
        // 创建目录(如果不存在)并写入文件
        Files.createDirectories(Paths.get(path).getParent());
        Files.write(Paths.get(path), pem.getBytes());
    }

    /**
     * 将字符串按指定长度分块并添加换行符
     * @param str 原始字符串
     * @param chunkSize 每块的长度
     * @return 分块后的字符串
     */
    private String chunkString(String str, int chunkSize) {
        return str.replaceAll("(.{" + chunkSize + "})", "$1\n");
    }

    /**
     * 从PEM文件加载私钥
     * @param path 私钥文件路径
     * @return 加载的PrivateKey对象
     * @throws Exception 文件读取或密钥解析失败时抛出
     */
    private PrivateKey loadPrivateKey(String path) throws Exception {
        // 读取文件内容并移除PEM头尾标记和空白字符
        String key = new String(Files.readAllBytes(Paths.get(path)))
                .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        // Base64解码并使用PKCS8格式解析私钥
        byte[] bytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    /**
     * 从PEM文件加载公钥
     *
     * @return 加载的PublicKey对象
     * @throws Exception 文件读取或密钥解析失败时抛出
     */
    private PublicKey loadPublicKey() throws Exception {
        // 读取文件内容并移除PEM头尾标记和空白字符
        String key = new String(Files.readAllBytes(Paths.get(JwtKeyManager.PUBLIC_KEY_PATH)))
                .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        // Base64解码并使用X509格式解析公钥
        byte[] bytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

}

