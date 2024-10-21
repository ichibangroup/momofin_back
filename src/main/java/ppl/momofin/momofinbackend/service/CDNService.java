package ppl.momofin.momofinbackend.service;

import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;

import java.io.IOException;

public interface CDNService {
    Document uploadFile(MultipartFile file, User user, String hashString) throws IOException;
    String getViewableUrl(String fileName, String username, String organizationName) throws IOException;
}