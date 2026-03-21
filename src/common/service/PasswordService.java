package common.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码安全服务
 * 使用 SHA-256 + 随机盐值 加密密码
 * 
 * @author Can Chen
 * @version 1.0
 */
public class PasswordService {
    
    /** 盐值长度（字节） */
    private static final int SALT_LENGTH = 16;
    
    /** 哈希迭代次数 */
    private static final int HASH_ITERATIONS = 1000;
    
    /** 随机数生成器 */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    /**
     * 加密密码（自动生成随机盐值）
     * 格式：盐值(Base64) + ":" + 哈希值(Base64)
     * 
     * @param plainPassword 明文密码
     * @return 加密后的字符串（含盐值）
     */
    public static String hash(String plainPassword) {
        // 1. 生成随机盐值
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        
        // 2. 加密密码
        byte[] hash = hashWithSalt(plainPassword, salt);
        
        // 3. 返回 盐值:哈希值
        return Base64.getEncoder().encodeToString(salt) + ":" + 
               Base64.getEncoder().encodeToString(hash);
    }
    
    /**
     * 验证密码
     * 
     * @param plainPassword 明文密码
     * @param storedHash 存储的加密字符串（含盐值）
     * @return true=密码正确，false=密码错误
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        
        // 1. 分离盐值和哈希值
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            return false;
        }
        
        // 2. 解码盐值
        byte[] salt;
        try {
            salt = Base64.getDecoder().decode(parts[0]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        
        // 3. 解码预期的哈希值
        byte[] expectedHash;
        try {
            expectedHash = Base64.getDecoder().decode(parts[1]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        
        // 4. 计算输入密码的哈希值
        byte[] actualHash = hashWithSalt(plainPassword, salt);
        
        // 5. 常量时间比较（防止时序攻击）
        return MessageDigest.isEqual(expectedHash, actualHash);
    }
    
    /**
     * 使用指定盐值计算密码哈希
     * 
     * @param password 明文密码
     * @param salt 盐值
     * @return 哈希值字节数组
     */
    private static byte[] hashWithSalt(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            // 先更新盐值
            md.update(salt);
            
            // 再更新密码
            byte[] hash = md.digest(password.getBytes());
            
            // 多次哈希增加破解难度
            for (int i = 0; i < HASH_ITERATIONS; i++) {
                hash = md.digest(hash);
            }
            
            return hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("加密算法不可用", e);
        }
    }
}