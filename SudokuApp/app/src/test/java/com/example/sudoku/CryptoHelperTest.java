package com.example.sudoku;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class CryptoHelperTest {

    private CryptoHelper cryptoHelper;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        cryptoHelper = new CryptoHelper(context);
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
