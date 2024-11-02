package ppl.momofin.momofinbackend.service;

import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;

import java.io.IOException;
import java.util.UUID;

public interface CDNService {
    Document submitDocument(MultipartFile file, User user, String hashString) throws IOException;
    Document editDocument(MultipartFile file, Document document, String hashString) throws IOException;
    String getViewableUrl(Document document, UUID userId, String organizationName) throws IOException;
}