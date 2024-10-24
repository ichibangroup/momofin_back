package ppl.momofin.momofinbackend.service;

import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

public interface DocumentService {
    String submitDocument(MultipartFile file, String username) throws IOException, NoSuchAlgorithmException, InvalidKeyException;
    Document verifyDocument(MultipartFile file, String username) throws IOException, NoSuchAlgorithmException, InvalidKeyException;
    Document verifySpecificDocument(MultipartFile file, UUID documentId, String username) throws IOException, NoSuchAlgorithmException, InvalidKeyException;
    List<Document> findAllDocumentsByOwner(User user);
    String getViewableUrl(UUID documentId, String username, String organizationName) throws IOException;
    Document fetchDocumentWithDocumentId(UUID documentId);
}
