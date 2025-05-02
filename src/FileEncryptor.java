package src;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class FileEncryptor {

    private static final String ALGORITHM = "AES";
    private static final String MODE = "GCM";
    private static final String PADDING = "NoPadding";
    private static final String TRANSFORMATION = ALGORITHM + "/" + MODE + "/" + PADDING;
    private static final int GCM_TAG_LENGTH = 128; // in bits
    private static final int IV_LENGTH = 12; // in bytes
    private static final int SALT_LENGTH = 16; // in bytes
    private static final int KEY_LENGTH = 256; // in bits
    private static final int ITERATION_COUNT = 65536; // PBKDF2 iterations

    public void encryptFile(File inputFile, File outputFile, char[] password) throws Exception {
        byte[] salt = generateSalt();
        SecretKey secretKey = getKeyFromPassword(password, salt);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = generateIV();
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            // Write salt and IV to the output file
            outputStream.write(salt);
            outputStream.write(iv);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] outputBytes = cipher.update(buffer, 0, bytesRead);
                if (outputBytes != null) {
                    outputStream.write(outputBytes);
                }
            }
            byte[] outputBytes = cipher.doFinal();
            if (outputBytes != null) {
                outputStream.write(outputBytes);
            }
        }
    }

    public void decryptFile(File inputFile, File outputFile, char[] password) throws Exception {
        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            // Read salt and IV from the input file
            byte[] salt = new byte[SALT_LENGTH];
            if (inputStream.read(salt) != SALT_LENGTH) {
                throw new IOException("Error reading salt from file");
            }
            byte[] iv = new byte[IV_LENGTH];
            if (inputStream.read(iv) != IV_LENGTH) {
                throw new IOException("Error reading IV from file");
            }

            SecretKey secretKey = getKeyFromPassword(password, salt);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] outputBytes = cipher.update(buffer, 0, bytesRead);
                if (outputBytes != null) {
                    outputStream.write(outputBytes);
                }
            }
            byte[] outputBytes = cipher.doFinal();
            if (outputBytes != null) {
                outputStream.write(outputBytes);
            }
        }
    }

    private SecretKey getKeyFromPassword(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM);
        return secretKey;
    }

    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    private byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        return iv;
    }
}
