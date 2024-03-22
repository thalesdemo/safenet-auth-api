import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtility {

    private static final String AES_GCM_NOPADDING = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256; // bits
    private static final int GCM_IV_LENGTH = 12; // bytes
    private static final int GCM_TAG_LENGTH = 16; // bytes

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && "generate".equals(args[0])) {
            System.out.println(generateRandomKey());
        } else if (args.length == 3 && "encrypt".equals(args[0])) {
            String key = args[1];
            String plaintext = args[2];
            System.out.println(encrypt(key, plaintext));
        } else if (args.length == 3 && "decrypt".equals(args[0])) {
            String key = args[1];
            String encryptedText = args[2];
            System.out.println(decrypt(key, encryptedText));
        } else {
            System.out.println("Usage: ");
            System.out.println("Generate a key: java EncryptionUtility generate");
            System.out.println("Encrypt text: java EncryptionUtility encrypt <key> <plaintext>");
            System.out.println("Decrypt text: java EncryptionUtility decrypt <key> <encryptedText>");
        }
    }

    public static String generateRandomKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
        SecretKey key = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String encrypt(String base64Key, String plaintext) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "AES");

        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_GCM_NOPADDING);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(cipherText);
    }

    public static String decrypt(String keyStr, String encryptedWithIv) throws Exception {
        byte[] key = Base64.getDecoder().decode(keyStr);
        SecretKey secretKey = new SecretKeySpec(key, "AES");

        // Split the input string on the colon
        String[] parts = encryptedWithIv.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("The encrypted string should have the format 'IV:ciphertext'");
        }

        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] decodedData = Base64.getDecoder().decode(parts[1]);

        Cipher cipher = Cipher.getInstance(AES_GCM_NOPADDING);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));

        byte[] decrypted = cipher.doFinal(decodedData);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
