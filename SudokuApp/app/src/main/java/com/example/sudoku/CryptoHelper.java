package com.example.sudoku;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHelper {
    private static final String KEY_ALIAS = "sudoku_persistent_aes_key";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TAG = "CryptoHelper";

    private static final int AES_KEY_SIZE = 128;
    private static final int GCM_TAG_LENGTH = 128;

    private static final String PREFS_NAME = "SudokuCryptoPrefs";
    private static final String PREF_KEY_FALLBACK = "fallback_aes_key";

    private SecretKey aesKey;

    public CryptoHelper(Context context) {
        try {
            // Try to use the secure Android KeyStore first
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);

            if (keyStore.containsAlias(KEY_ALIAS)) {
                try {
                    KeyStore.Entry entry = keyStore.getEntry(KEY_ALIAS, null);
                    if (entry instanceof KeyStore.SecretKeyEntry) {
                        this.aesKey = ((KeyStore.SecretKeyEntry) entry).getSecretKey();
                        Log.d(TAG, "Successfully loaded key from Android KeyStore.");
                    } else {
                        Log.w(TAG, "Alias found but entry is not a SecretKey. Generating new key.");
                        generateNewKey(keyStore);
                    }
                } catch (UnrecoverableEntryException e) {
                    Log.w(TAG, "Key in KeyStore is no longer recoverable. Generating new key.", e);
                    deleteInvalidKey(keyStore);
                    generateNewKey(keyStore);
                }
            } else {
                Log.i(TAG, "No key found in KeyStore. Generating a new one.");
                generateNewKey(keyStore);
            }
        } catch (Exception e) {
            Log.e(TAG, "Android KeyStore operations failed. Using insecure SharedPreferences fallback.", e);
            this.aesKey = getOrCreateFallbackKey(context);
        }
    }

    // Constructor for unit tests
    public CryptoHelper(boolean testMode) {
        if (!testMode) {
            throw new IllegalArgumentException("Production CryptoHelper must be initialized with a Context.");
        }
        // For tests, always use a transient in-memory key
        byte[] keyBytes = new byte[AES_KEY_SIZE / 8];
        new SecureRandom().nextBytes(keyBytes);
        this.aesKey = new SecretKeySpec(keyBytes, "AES");
    }

    private SecretKey getOrCreateFallbackKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String encodedKey = prefs.getString(PREF_KEY_FALLBACK, null);
        if (encodedKey != null) {
            Log.w(TAG, "Loading insecure fallback key from SharedPreferences.");
            byte[] keyBytes = Base64.decode(encodedKey, Base64.DEFAULT);
            return new SecretKeySpec(keyBytes, "AES");
        } else {
            Log.w(TAG, "Generating and saving new insecure fallback key to SharedPreferences.");
            byte[] keyBytes = new byte[AES_KEY_SIZE / 8];
            new SecureRandom().nextBytes(keyBytes);
            SecretKey newKey = new SecretKeySpec(keyBytes, "AES");
            String newEncodedKey = Base64.encodeToString(newKey.getEncoded(), Base64.DEFAULT);
            prefs.edit().putString(PREF_KEY_FALLBACK, newEncodedKey).apply();
            return newKey;
        }
    }

    private void generateNewKey(KeyStore keyStore) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        final KeyGenParameterSpec parameterSpec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(AES_KEY_SIZE)
                .build();
        keyGenerator.init(parameterSpec);
        aesKey = keyGenerator.generateKey();
        Log.i(TAG, "Generated new AES key in KeyStore.");
    }

    private void deleteInvalidKey(KeyStore keyStore) {
        try {
            keyStore.deleteEntry(KEY_ALIAS);
            Log.i(TAG, "Deleted invalid key from KeyStore.");
        } catch (KeyStoreException e) {
            Log.e(TAG, "Failed to delete invalid key.", e);
        }
    }

    public String encrypt(String data) throws GeneralSecurityException {
        if (aesKey == null) {
            throw new GeneralSecurityException("Cannot encrypt: AES key is not available.");
        }
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            byte[] iv = new byte[12]; // GCM IV is 12 bytes
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmParameterSpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
            return Base64.encodeToString(combined, Base64.DEFAULT);
        } catch (Exception e) {
            throw new GeneralSecurityException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedData) throws GeneralSecurityException {
        if (aesKey == null) {
            throw new GeneralSecurityException("Cannot decrypt: AES key is not available.");
        }
        try {
            byte[] combined = Base64.decode(encryptedData, Base64.DEFAULT);
            if (combined.length < 12) {
                throw new IllegalArgumentException("Invalid encrypted data format");
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
        } catch (Exception e) {
            throw new GeneralSecurityException("Decryption failed", e);
        }
    }
}
