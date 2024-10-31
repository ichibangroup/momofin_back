package ppl.momofin.momofinbackend.service;

import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.EditRequest;
import ppl.momofin.momofinbackend.model.User;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface DocumentService {
    String submitDocument(MultipartFile file, String username) throws IOException, NoSuchAlgorithmException, InvalidKeyException;
    Document verifyDocument(MultipartFile file, String username) throws IOException, NoSuchAlgorithmException, InvalidKeyException;
    Document verifySpecificDocument(MultipartFile file, Long documentId, String username) throws IOException, NoSuchAlgorithmException, InvalidKeyException;
    List<Document> findAllDocumentsByOwner(User user);
    String getViewableUrl(Long documentId, String username, String organizationName) throws IOException;
    Document fetchDocumentWithDocumentId(Long documentId);

    EditRequest requestEdit(Long documentId, Long userId);
    Document editDocument(MultipartFile file, EditRequest editRequest) throws IOException, NoSuchAlgorithmException, InvalidKeyException;
    void rejectEditRequest(EditRequest editRequest);
    List<EditRequest> getEditRequests(Long userId);

}
