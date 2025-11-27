import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class TestCrypto {
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 128; // Changed from 256 to 128
    private static final int GCM_TAG_LENGTH = 128;

    public static void main(String[] args) {
        try {
            // Generate a 128-bit AES key
            byte[] keyBytes = new byte[AES_KEY_SIZE / 8];
            SecureRandom random = new SecureRandom();
            random.nextBytes(keyBytes);
            SecretKeySpec aesKey = new SecretKeySpec(keyBytes, "AES");

            // Test data
            String originalText = "Hello, World! This is a test message for 128-bit AES.";
            System.out.println("Original text: " + originalText);

            // Encrypt
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            byte[] iv = new byte[12]; // GCM IV
            random.nextBytes(iv);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmParameterSpec);
            byte[] encryptedBytes = cipher.doFinal(originalText.getBytes());

            // Prepend IV to encrypted data
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            System.out.println("Encryption successful. Encrypted length: " + combined.length);

            // Decrypt
            byte[] combinedDecrypt = combined;
            byte[] ivDecrypt = new byte[12];
            byte[] encryptedBytesDecrypt = new byte[combinedDecrypt.length - 12];
            System.arraycopy(combinedDecrypt, 0, ivDecrypt, 0, ivDecrypt.length);
            System.arraycopy(combinedDecrypt, ivDecrypt.length, encryptedBytesDecrypt, 0, encryptedBytesDecrypt.length);

            Cipher cipherDecrypt = Cipher.getInstance(AES_TRANSFORMATION);
            GCMParameterSpec gcmParameterSpecDecrypt = new GCMParameterSpec(GCM_TAG_LENGTH, ivDecrypt);
            cipherDecrypt.init(Cipher.DECRYPT_MODE, aesKey, gcmParameterSpecDecrypt);

            byte[] decryptedBytes = cipherDecrypt.doFinal(encryptedBytesDecrypt);
            String decryptedText = new String(decryptedBytes);

            System.out.println("Decrypted text: " + decryptedText);
            System.out.println("Test passed: " + originalText.equals(decryptedText));

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
