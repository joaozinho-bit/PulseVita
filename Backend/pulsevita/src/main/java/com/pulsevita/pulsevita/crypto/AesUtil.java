package com.pulsevita.pulsevita.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AesUtil {

    // chave de 16 bytes, tem de ser igual a do ESP32
    private static final byte[] CHAVE_AES = "PulseVitaAES123!".getBytes();

    public static String desencriptar(String base64Recebido) throws Exception {
        byte[] dadosEncriptados = Base64.getDecoder().decode(base64Recebido);

        SecretKeySpec keySpec = new SecretKeySpec(CHAVE_AES, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] dadosDesencriptados = cipher.doFinal(dadosEncriptados);
        return new String(dadosDesencriptados);
    }

    public static String encriptar(String texto) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(CHAVE_AES, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] dadosEncriptados = cipher.doFinal(texto.getBytes());
        return Base64.getEncoder().encodeToString(dadosEncriptados);
    }
}