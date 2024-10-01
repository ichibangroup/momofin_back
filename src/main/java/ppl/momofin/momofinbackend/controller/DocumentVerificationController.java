package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.response.DocumentSubmissionSuccessResponse;
import ppl.momofin.momofinbackend.response.DocumentVerificationSuccessResponse;
import ppl.momofin.momofinbackend.response.ErrorResponse;
import ppl.momofin.momofinbackend.response.Response;
import ppl.momofin.momofinbackend.service.DocumentService;
import ppl.momofin.momofinbackend.model.Document;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class DocumentVerificationController {
    public ResponseEntity<Response> submitDocument(@RequestParam("file") MultipartFile file) {
        return null;
    }

    public ResponseEntity<Response> verifyDocument(@RequestParam("file") MultipartFile file) {
        return null;
    }
}
