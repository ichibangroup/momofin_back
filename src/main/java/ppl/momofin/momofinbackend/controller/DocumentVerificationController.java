package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.EditRequest;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.request.EditRequestRequest;
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
import java.util.UUID;

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

    @PostMapping("/verify/{documentId}")
    public ResponseEntity<Response> verifyDocument(@RequestHeader("Authorization") String token, @RequestParam("file") MultipartFile file, @PathVariable("documentId") String documentId) {
        try {
            String username = getUsername(token, jwtUtil);

            Document verifiedDocument = documentService.verifySpecificDocument(file, UUID.fromString(documentId), username);

            DocumentVerificationSuccessResponse successResponse = new DocumentVerificationSuccessResponse(verifiedDocument);
            return ResponseEntity.ok(successResponse);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse("Verification failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    @GetMapping("/verify/{documentId}")
    public ResponseEntity<Document> getDocumentToBeVerified(@PathVariable("documentId") String documentId) {
        return ResponseEntity.ok(documentService.fetchDocumentWithDocumentId(UUID.fromString(documentId)));
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
    public ResponseEntity<Response> getViewableUrl(@PathVariable String documentId, @RequestHeader("Authorization") String token) {
        try {
            UUID userId = getUserId(token, jwtUtil);
            String organizationName = getOrgName(token, jwtUtil);
            String url = documentService.getViewableUrl(UUID.fromString(documentId), userId, organizationName);
            Response urlResponse = new DocumentViewUrlResponse(url);

            return ResponseEntity.ok(urlResponse);
        } catch (RuntimeException | IOException e) {
            ErrorResponse errorResponse = new ErrorResponse("Error retrieving document: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/{documentId}/request-edit")
    public ResponseEntity<EditRequest> requestEdit(
            @PathVariable String documentId,
            @RequestBody EditRequestRequest request) {
        EditRequest editRequest = documentService.requestEdit(UUID.fromString(documentId), request.getUsername());
        return ResponseEntity.ok(editRequest);
    }

    @GetMapping("/edit-request")
    public ResponseEntity<List<EditRequest>> getRequests(
            @RequestHeader("Authorization") String token) {
        UUID userId = getUserId(token, jwtUtil);
        List<EditRequest> editRequests = documentService.getEditRequests(userId);
        return ResponseEntity.ok(editRequests);
    }

    @PostMapping("/edit-request/{documentId}")
    public ResponseEntity<Document> editDocument(
            @PathVariable String documentId,
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        User user = new User();
        user.setUserId(getUserId(token, jwtUtil));
        Document document = new Document();
        document.setDocumentId(UUID.fromString(documentId));
        EditRequest request = new EditRequest(user, document);
        Document editedDocument = documentService.editDocument(file, request);
        return ResponseEntity.ok(editedDocument);
    }

    public static String getUsername(String token, JwtUtil jwtUtil) {
        String jwtToken = token.substring(7);
        return jwtUtil.extractUsername(jwtToken);
    }

    public static UUID getUserId(String token, JwtUtil jwtUtil) {
        String jwtToken = token.substring(7);
        return UUID.fromString(jwtUtil.extractUserId(jwtToken));
    }

    public static String getOrgName(String token, JwtUtil jwtUtil) {
        String jwtToken = token.substring(7);
        return jwtUtil.extractOrganizationName(jwtToken);
    }
}