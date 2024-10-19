package ppl.momofin.momofinbackend.service;

import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.Document;
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
}
