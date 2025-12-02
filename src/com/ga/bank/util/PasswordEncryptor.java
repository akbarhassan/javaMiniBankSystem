package com.ga.bank.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

public class PasswordEncryptor {

    public String encrypt(String plainPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (plainPassword.isEmpty())
            throw new RuntimeException("Password should not be empty");

        //generating salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        // initializing hasher PBKDF2
        KeySpec spec = new PBEKeySpec(plainPassword.toCharArray(),
                salt, 65536, 128);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();

        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);
        return saltBase64 + ":" + hashBase64;
    }



    public boolean compare(String plainPassword,String hashedPassword){
        try {
            String[] parts = hashedPassword.split(":");
            if (parts.length != 2) {
                return false;
            }
            String hashedSalt = parts[0];
            String hashedHash = parts[1];

            // decode to byte
            byte[] salt = Base64.getDecoder().decode(hashedSalt);
            byte[] storedHash = Base64.getDecoder().decode(hashedHash);

            // salt plain
            KeySpec spec = new PBEKeySpec(
                    plainPassword.toCharArray(),
                    salt,
                    65536,
                    128
            );

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] newHash = factory.generateSecret(spec).getEncoded();

            return Arrays.equals(storedHash, newHash);

        }catch(Exception e){
            return false;
        }
    }
}
