package ppl.momofin.momofinbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.error.UserNotFoundException;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.DocumentRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentServiceImpl implements DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);
    private static final String ALGORITHM = "HmacSHA256";

    @Value("${hmac.secret.key}")
    private String secretKey;

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CDNService cdnService;

    @Autowired
    public DocumentServiceImpl(DocumentRepository documentRepository, UserRepository userRepository, CDNService cdnService) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.cdnService = cdnService;
    }

    @Override
    public String submitDocument(MultipartFile file, String username) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be null or empty");
        }

        String hashString = generateHash(file);
        Optional<Document> documentFound = documentRepository.findByHashString(hashString);

        if (documentFound.isEmpty()) {
            Optional<User> owner = userRepository.findByUsername(username);

            if (owner.isEmpty()) throw new UserNotFoundException("User with username " + username + " not found");;

            User user = owner.get();
            Document document = cdnService.uploadFile(file, user, hashString);

            logger.info("New document saved: {}", document.getName());
            return "Your document " + document.getName()+" has been successfully stored";
        } else {
            logger.info("Document already exists: {}", documentFound.get().getName());
            return documentFound.get().getName() + " has already been stored before";
        }
    }

    @Override
    public Document verifyDocument(MultipartFile file, String username) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be null or empty");
        }

        String hashString = generateHash(file);
        return documentRepository.findByHashString(hashString)
                .orElseThrow(() -> new IllegalStateException("Document not found"));
    }

    private String generateHash(MultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try (InputStream fileStream = file.getInputStream()) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
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

    @Override
    public List<Document> findAllDocumentsByOwner(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        return documentRepository.findAllByOwner(user);
    }
}