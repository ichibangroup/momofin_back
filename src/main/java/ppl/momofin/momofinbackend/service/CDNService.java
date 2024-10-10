package ppl.momofin.momofinbackend.service;

import java.io.IOException;

public interface CDNService {
    void uploadFile(byte[] fileBytes, String folderName, String fileName) throws IOException;
}