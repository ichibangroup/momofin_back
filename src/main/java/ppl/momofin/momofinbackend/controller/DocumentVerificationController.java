package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.response.*;
import ppl.momofin.momofinbackend.security.JwtUtil;
import ppl.momofin.momofinbackend.service.DocumentService;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.service.UserService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/doc")
public class DocumentVerificationController {
    private final DocumentService documentService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(DocumentVerificationController.class);
    @Autowired
    public DocumentVerificationController(DocumentService documentService, JwtUtil jwtUtil, UserService userService) {
        this.documentService = documentService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
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

    @GetMapping("/view")
    public ResponseEntity<Response> getAllUsersDocument(@RequestHeader("Authorization") String token) {
        logger.info("fine 1");
        String username = getUsername(token,jwtUtil);

        User user = userService.fetchUserByUsername(username);
        List<Document> documents = documentService.findAllDocumentsByOwner(user);

        UserDocumentsResponse userDocumentsResponse = new UserDocumentsResponse(user, documents);

        return ResponseEntity.ok(userDocumentsResponse);
    }

    @GetMapping("/view/{documentId}")
    public ResponseEntity<Response> getViewableUrl(@PathVariable Long documentId, @RequestHeader("Authorization") String token) {
        try {
            String username = getUsername(token, jwtUtil);
            String organizationName = getOrgName(token, jwtUtil);
            String url = documentService.getViewableUrl(documentId, username, organizationName);
            Response urlResponse = new DocumentViewUrlResponse(url);

            return ResponseEntity.ok(urlResponse);
        } catch (RuntimeException | IOException e) {
            ErrorResponse errorResponse = new ErrorResponse("Error retrieving document: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    public static String getUsername(String token, JwtUtil jwtUtil) {
        String jwtToken = token.substring(7);
        return jwtUtil.extractUsername(jwtToken);
    }

    public static String getOrgName(String token, JwtUtil jwtUtil) {
        String jwtToken = token.substring(7);
        return jwtUtil.extractOrganizationName(jwtToken);
    }
}