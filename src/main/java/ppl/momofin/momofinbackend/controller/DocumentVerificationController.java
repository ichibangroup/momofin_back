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

@RestController
@RequestMapping("/doc")
public class DocumentVerificationController {
    private final DocumentService documentService;

    @Autowired
    public DocumentVerificationController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/submit")
    public ResponseEntity<Response> submitDocument(@RequestParam("file") MultipartFile file) {
        try {
            String result = documentService.submitDocument(file);

            DocumentSubmissionSuccessResponse successResponse = new DocumentSubmissionSuccessResponse(result);

            return ResponseEntity.ok(successResponse);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            ErrorResponse errorResponse = new ErrorResponse("Error processing document: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Response> verifyDocument(@RequestParam("file") MultipartFile file) {
        try {
            Document document = documentService.verifyDocument(file);

            DocumentVerificationSuccessResponse successResponse = new DocumentVerificationSuccessResponse(document);

            return ResponseEntity.ok(successResponse);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | IllegalStateException e) {
            ErrorResponse errorResponse = new ErrorResponse("Error verifying document: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
