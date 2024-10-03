package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.response.DocumentSubmissionSuccessResponse;
import ppl.momofin.momofinbackend.response.DocumentVerificationSuccessResponse;
import ppl.momofin.momofinbackend.response.ErrorResponse;
import ppl.momofin.momofinbackend.response.Response;
import ppl.momofin.momofinbackend.security.JwtUtil;
import ppl.momofin.momofinbackend.service.DocumentService;
import ppl.momofin.momofinbackend.model.Document;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/doc")
public class DocumentVerificationController {
    private final DocumentService documentService;
    private final JwtUtil jwtUtil;

    @Autowired
    public DocumentVerificationController(DocumentService documentService, JwtUtil jwtUtil) {
        this.documentService = documentService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/submit")
    public ResponseEntity<Response> submitDocument(@RequestHeader("Authorization") String token, @RequestParam("file") MultipartFile file) {
        try {
            String username = getUsername(token, jwtUtil);
            String result = documentService.submitDocument(file, username);
            DocumentSubmissionSuccessResponse successResponse = new DocumentSubmissionSuccessResponse(result);
            return ResponseEntity.ok(successResponse);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | IllegalStateException e) {
            ErrorResponse errorResponse = new ErrorResponse("Error processing document: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Response> verifyDocument(@RequestHeader("Authorization") String token, @RequestParam("file") MultipartFile file) {
        try {
            String username = getUsername(token, jwtUtil);
            Document document = documentService.verifyDocument(file, username);
            DocumentVerificationSuccessResponse successResponse = new DocumentVerificationSuccessResponse(document);
            return ResponseEntity.ok(successResponse);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | IllegalStateException e) {
            ErrorResponse errorResponse = new ErrorResponse("Error verifying document: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    public static String getUsername(String token, JwtUtil jwtUtil) {
        String jwtToken = token.substring(7);
        return jwtUtil.extractUsername(jwtToken);
    }
}