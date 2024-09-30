package ppl.momofin.momofinbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.repository.DocumentRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);
    private static final String ALGORITHM = "HmacSHA256";

    @Value("${hmac.secret.key}")
    private String SECRET_KEY;

    private final DocumentRepository documentRepository;

    public DocumentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public String submitDocument(MultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be null or empty");
        }

        String hashString = generateHash(file);
        Optional<Document> documentFound = documentRepository.findByHashString(hashString);

        if (documentFound.isEmpty()) {
            Document document = new Document();
            document.setHashString(hashString);
            document.setName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
            documentRepository.save(document);
            logger.info("New document saved: {}", document.getName());
            return hashString;
        } else {
            logger.info("Document already exists: {}", documentFound.get().getName());
            return hashString + " this document has already been submitted before";
        }
    }

    @Override
    public Document verifyDocument(MultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be null or empty");
        }

        String hashString = generateHash(file);
        return documentRepository.findByHashString(hashString)
                .orElseThrow(() -> new IllegalStateException("Document not found"));
    }

    private String generateHash(MultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try (InputStream fileStream = file.getInputStream()) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKeySpec);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileStream.read(buffer)) != -1) {
                mac.update(buffer, 0, bytesRead);
            }

            byte[] hmacBytes = mac.doFinal();
            return bytesToHex(hmacBytes);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}