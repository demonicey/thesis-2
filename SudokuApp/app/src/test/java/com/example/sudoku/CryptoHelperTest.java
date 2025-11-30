package com.example.sudoku;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)

public class CryptoHelperTest {

    private CryptoHelper cryptoHelper;

    @Before
    public void setUp() throws Exception {
        // Use test mode to avoid Android KeyStore issues in unit tests
        cryptoHelper = new CryptoHelper(true);
    }

    @Test
    public void testEncryptDecrypt() throws Exception {
        String originalText = "Hello, World! This is a test message.";
        String encrypted = cryptoHelper.encrypt(originalText);
        String decrypted = cryptoHelper.decrypt(encrypted);
        assertEquals("Decrypted text should match original", originalText, decrypted);
    }

    @Test
    public void testEncryptDecryptEmptyString() throws Exception {
        String originalText = "";
        String encrypted = cryptoHelper.encrypt(originalText);
        String decrypted = cryptoHelper.decrypt(encrypted);
        assertEquals("Decrypted empty string should match original", originalText, decrypted);
    }

    @Test
    public void testEncryptDecryptSpecialCharacters() throws Exception {
        String originalText = "Special chars: !@#$%^&*()_+{}|:<>?[]\\;',./";
        String encrypted = cryptoHelper.encrypt(originalText);
        String decrypted = cryptoHelper.decrypt(encrypted);
        assertEquals("Decrypted special characters should match original", originalText, decrypted);
    }
}
