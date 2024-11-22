package ppl.momofin.momofinbackend.service;

import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.dto.EditRequestDTO;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.DocumentVersion;
import ppl.momofin.momofinbackend.model.EditRequest;
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
    String getViewableUrl(UUID documentId, UUID userId, String organizationName) throws IOException;
    String getViewableUrl(UUID documentId, UUID userId, String organizationName, int version) throws IOException;
    Document fetchDocumentWithDocumentId(UUID documentId);

    EditRequest requestEdit(UUID documentId, String username);
    Document editDocument(MultipartFile file, EditRequest editRequest) throws IOException, NoSuchAlgorithmException, InvalidKeyException;
    void rejectEditRequest(EditRequest editRequest);
    List<EditRequestDTO> getEditRequests(UUID userId);
    List<DocumentVersion> findVersionsOfDocument(UUID documentId);
    String getViewableUrlForEditRequest(UUID documentId, EditRequest editRequest, String organizationName) throws IOException;

}
