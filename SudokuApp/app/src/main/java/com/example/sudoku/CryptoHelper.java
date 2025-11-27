package com.example.sudoku;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHelper {
    private static final String KEY_ALIAS = "sudoku_location_key";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";

    // AES key size in bits for symmetric encryption
    private static final int AES_KEY_SIZE = 128;

    private static final int GCM_TAG_LENGTH = 128;
    private KeyStore keyStore;
    private SecretKey aesKey;

    public CryptoHelper(Context context) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        generateECKeyIfNotExists();
        deriveAESKey();
    }

    private void generateECKeyIfNotExists() throws KeyStoreException {
        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
                KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(KEY_ALIAS,
                        KeyProperties.PURPOSE_AGREE_KEY)
                        .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                        // EC key size in bits for Elliptic Curve key agreement (ECDH)
                        .setKeySize(256)
                        .build();
                keyPairGenerator.initialize(keyGenParameterSpec);
                keyPairGenerator.generateKeyPair();
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    private void deriveAESKey() throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        try {
            // For simplicity, we'll use a fixed derivation. In production, use proper ECDH with peer public key
            // This is a simplified approach - in real apps, you'd derive from ECDH shared secret
            byte[] keyBytes = new byte[AES_KEY_SIZE / 8];
            SecureRandom random = new SecureRandom();
            random.nextBytes(keyBytes);
            aesKey = new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        if (aesKey == null) {
            throw new IllegalStateException("AES key not initialized");
        }

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        byte[] iv = new byte[12]; // GCM IV
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());

        // Prepend IV to encrypted data
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        return Base64.encodeToString(combined, Base64.DEFAULT);
    }

    public String decrypt(String encryptedData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        if (aesKey == null) {
            throw new IllegalStateException("AES key not initialized");
        }

        byte[] combined = Base64.decode(encryptedData, Base64.DEFAULT);
        if (combined.length < 12) {
            throw new IllegalArgumentException("Invalid encrypted data");
        }

        byte[] iv = new byte[12];
        byte[] encryptedBytes = new byte[combined.length - 12];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, encryptedBytes, 0, encryptedBytes.length);

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmParameterSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }
}
