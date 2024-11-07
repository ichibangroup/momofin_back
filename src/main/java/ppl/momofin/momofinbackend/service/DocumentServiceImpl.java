package ppl.momofin.momofinbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.dto.EditRequestDTO;
import ppl.momofin.momofinbackend.error.UserNotFoundException;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.DocumentVersion;
import ppl.momofin.momofinbackend.model.EditRequest;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.DocumentRepository;
import ppl.momofin.momofinbackend.repository.DocumentVersionRepository;
import ppl.momofin.momofinbackend.repository.EditRequestRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentServiceImpl implements DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);
    private static final String ALGORITHM = "HmacSHA256";

    private static final String FILE_EMPTY_ERROR_MESSAGE = "File must not be null or empty";
    private static final String NOT_FOUND = " not found";

    @Value("${hmac.secret.key}")
    private String secretKey;

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final EditRequestRepository editRequestRepository;
    private final CDNService cdnService;
    private final DocumentVersionRepository documentVersionRepository;

    @Autowired
    public DocumentServiceImpl(DocumentRepository documentRepository, UserRepository userRepository, CDNService cdnService, EditRequestRepository editRequestRepository, DocumentVersionRepository documentVersionRepository) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.cdnService = cdnService;
        this.editRequestRepository = editRequestRepository;
        this.documentVersionRepository = documentVersionRepository;
    }

    @Override
    public String submitDocument(MultipartFile file, String username) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(FILE_EMPTY_ERROR_MESSAGE);
        }

        String hashString = generateHash(file);
        Optional<Document> documentFound = documentRepository.findByHashString(hashString);

        if (documentFound.isEmpty()) {
            Optional<User> owner = userRepository.findByUsername(username);

            if (owner.isEmpty()) throw new UserNotFoundException("User with username " + username + NOT_FOUND);

            User user = owner.get();
            Document document = cdnService.submitDocument(file, user, hashString);

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
            throw new IllegalArgumentException(FILE_EMPTY_ERROR_MESSAGE);
        }

        String hashString = generateHash(file);
        return documentRepository.findByHashString(hashString)
                .orElseThrow(() -> new IllegalStateException("Document not found"));
    }

    @Override
    public Document verifySpecificDocument(MultipartFile file, UUID documentId, String username) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(FILE_EMPTY_ERROR_MESSAGE);
        }

        Optional<Document> documentOptional = documentRepository.findById(documentId);

        if (documentOptional.isEmpty()) {
            throw new IllegalStateException("Document with ID " + documentId + NOT_FOUND);
        }

        Document document = documentOptional.get();

        Optional<User> ownerOptional = userRepository.findByUsername(username);
        if (ownerOptional.isEmpty() || !document.getOwner().equals(ownerOptional.get())) {
            throw new IllegalStateException("You are not authorized to verify this document.");
        }

        String hashString = generateHash(file);

        if (!document.getHashString().equals(hashString)) {
            throw  new IllegalArgumentException("File does not match the specified document.");
        }

        return document;
    }

     String generateHash(MultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
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

    @Override
    public String getViewableUrl(UUID documentId, UUID userId, String organizationName) throws IOException {
        Optional<Document> optionalDocument = documentRepository.findByDocumentId(documentId);

        if (optionalDocument.isEmpty()) throw new IllegalArgumentException("Document with id " + documentId + " does not exist");

        Document document = optionalDocument.get();
        return cdnService.getViewableUrl(document, userId, organizationName);
    }

    @Override
    public String getViewableUrl(UUID documentId, UUID userId, String organizationName, int version) throws IOException {
        Optional<Document> optionalDocument = documentRepository.findByDocumentId(documentId);

        if (optionalDocument.isEmpty()) throw new IllegalArgumentException("Document with id " + documentId + " does not exist");

        Document document = optionalDocument.get();
        return cdnService.getViewableUrl(document, userId, organizationName, version);
    }

    @Override
    public Document fetchDocumentWithDocumentId(UUID documentId) {
        return documentRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new IllegalStateException("Document not found"));
    }

    @Override
    public EditRequest requestEdit(UUID documentId, String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) throw new UserNotFoundException("User with username " + username + NOT_FOUND);
        User user = optionalUser.get();
        EditRequest request = new EditRequest();
        Document document = new Document();
        document.setDocumentId(documentId);

        request.setDocument(document);
        request.setUser(user);
        return editRequestRepository.save(request);
    }

    @Override
    public List<EditRequestDTO> getEditRequests(UUID userId) {
        return editRequestRepository.findByUserIdAsDTO(userId);
    }

    @Override
    public Document editDocument(MultipartFile file, EditRequest editRequest) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(FILE_EMPTY_ERROR_MESSAGE);
        }

        if(!editRequestRepository.existsById(editRequest.getId())) {
            throw new IllegalArgumentException("Edit request not found in the database.");
        }

        String hashString = generateHash(file);

        UUID documentId = editRequest.getDocumentId();
        UUID userId = editRequest.getUserId();

        Optional<Document> document = documentRepository.findByDocumentId(documentId);
        if(document.isEmpty()) {
            throw new IllegalStateException("Document with ID " + documentId + NOT_FOUND);
        }
        Optional<User> editor = userRepository.findById(userId);
        if (editor.isEmpty()) {
            throw new UserNotFoundException("User with ID " + userId + NOT_FOUND);
        }

        Document editedDocument = cdnService.editDocument(file, document.get(), hashString, editor.get());
        editRequestRepository.delete(editRequest);
        return editedDocument;
    }



    @Override
    public void rejectEditRequest(EditRequest editRequest) {
        editRequestRepository.delete(editRequest);
    }

    @Override
    public List<DocumentVersion> findVersionsOfDocument(UUID documentId) {
        return documentVersionRepository.findById_DocumentId(documentId);
    }
}