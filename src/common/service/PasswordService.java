package common.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordService {
    private static final String PBKDF2_PREFIX = "PBKDF2";
    private static final String PBKDF2_ALGO = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private PasswordService() {
    }

    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null.");
        }
        try {
            byte[] salt = new byte[SALT_BYTES];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);
            byte[] derived = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_BITS);
            return PBKDF2_PREFIX + "$" + PBKDF2_ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(derived);
        } catch (Exception e) {
            throw new IllegalStateException("PBKDF2 hashing failed.", e);
        }
    }

    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        try {
            if (storedHash.startsWith(PBKDF2_PREFIX + "$")) {
                String[] parts = storedHash.split("\\$");
                if (parts.length != 4) {
                    return false;
                }
                int iterations = Integer.parseInt(parts[1]);
                byte[] salt = Base64.getDecoder().decode(parts[2]);
                byte[] expected = Base64.getDecoder().decode(parts[3]);
                byte[] actual = pbkdf2(rawPassword.toCharArray(), salt, iterations, expected.length * 8);
                return MessageDigest.isEqual(actual, expected);
            }
            // Backward compatibility for existing SHA-256 hashes in users.json.
            return legacySha256(rawPassword).equals(storedHash);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean needsUpgrade(String storedHash) {
        return storedHash != null && !storedHash.startsWith(PBKDF2_PREFIX + "$");
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyBits) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGO);
        return factory.generateSecret(spec).getEncoded();
    }

    private static String legacySha256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hashBytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", e);
        }
    }
}
