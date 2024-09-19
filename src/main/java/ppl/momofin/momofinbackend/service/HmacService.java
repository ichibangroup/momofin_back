package ppl.momofin.momofinbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.repository.DocumentRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class HmacService {

    @Autowired
    private DocumentRepository documentRepository;
    // Method to compute HMAC of a file
    public String calculateHmac(File file, String secret, String algorithm)
            throws NoSuchAlgorithmException, InvalidKeyException, Exception {

        // Create a Mac instance with the desired HMAC algorithm
        Mac mac = Mac.getInstance(algorithm);
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), algorithm);
        mac.init(keySpec);

        // Read file data in chunks and update the MAC
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                mac.update(buffer, 0, bytesRead);
            }
        }

        // Get the final HMAC hash
        byte[] hmacBytes = mac.doFinal();

        // Convert the hash to a hex string
        StringBuilder result = new StringBuilder();
        for (byte b : hmacBytes) {
            result.append(String.format("%02x", b));
        }

        return result.toString();
    }

    public String calculateHmac(MultipartFile file, String secretKey, String algorithm) throws Exception {
        InputStream fileStream = file.getInputStream();

        // Step 1: Convert secret key to a byte array
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), algorithm);

        // Step 2: Initialize the HMAC with the key and algorithm
        Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKeySpec);

        // Step 3: Read the file stream and update HMAC digest
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fileStream.read(buffer)) != -1) {
            mac.update(buffer, 0, bytesRead);
        }

        // Step 4: Complete the HMAC calculation
        byte[] hmacBytes = mac.doFinal();

        StringBuilder result = new StringBuilder();
        for (byte b : hmacBytes) {
            result.append(String.format("%02x", b));
        }

        Document document = new Document();
        document.setHashString(result.toString());
        document.setName(file.getName());

        documentRepository.save(document);

        return result.toString();
    }

    public Document verifyDocs(MultipartFile file, String secretKey, String algorithm) throws Exception {
        InputStream fileStream = file.getInputStream();

        // Step 1: Convert secret key to a byte array
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), algorithm);

        // Step 2: Initialize the HMAC with the key and algorithm
        Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKeySpec);

        // Step 3: Read the file stream and update HMAC digest
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fileStream.read(buffer)) != -1) {
            mac.update(buffer, 0, bytesRead);
        }

        // Step 4: Complete the HMAC calculation
        byte[] hmacBytes = mac.doFinal();

        StringBuilder result = new StringBuilder();
        for (byte b : hmacBytes) {
            result.append(String.format("%02x", b));
        }

        return documentRepository.findByHashString(result.toString());
    }
}
