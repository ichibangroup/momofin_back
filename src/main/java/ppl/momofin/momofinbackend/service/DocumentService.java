package ppl.momofin.momofinbackend.service;

import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.Document;

public interface DocumentService {
    String submitDocument(MultipartFile file) throws Exception;
    Document verifyDocument(MultipartFile file) throws Exception;
}
