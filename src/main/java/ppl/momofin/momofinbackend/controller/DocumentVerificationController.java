package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.dto.EditRequestDTO;
import ppl.momofin.momofinbackend.model.DocumentVersion;
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
    private static final String ERROR_RETRIEVING_DOCUMENT= "Error retrieving document: ";

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
            ErrorResponse errorResponse = new ErrorResponse(ERROR_RETRIEVING_DOCUMENT + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/view/{documentId}/{version}")
    public ResponseEntity<Response> getViewableUrl(@PathVariable String documentId, @PathVariable int version, @RequestHeader("Authorization") String token) {
        try {
            UUID userId = getUserId(token, jwtUtil);
            String organizationName = getOrgName(token, jwtUtil);
            String url = documentService.getViewableUrl(UUID.fromString(documentId), userId, organizationName, version);
            Response urlResponse = new DocumentViewUrlResponse(url);

            return ResponseEntity.ok(urlResponse);
        } catch (RuntimeException | IOException e) {
            ErrorResponse errorResponse = new ErrorResponse(ERROR_RETRIEVING_DOCUMENT + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/{documentId}/request-edit")
    public ResponseEntity<Response> requestEdit(
            @PathVariable String documentId,
            @RequestBody EditRequestRequest request) {
        try {
            EditRequest editRequest = documentService.requestEdit(UUID.fromString(documentId), request.getUsername());
            EditRequestDTO editRequestDTO = EditRequestDTO.toDTO(editRequest);
            return ResponseEntity.ok(editRequestDTO);
        } catch (RuntimeException  e) {
            ErrorResponse errorResponse = new ErrorResponse("Error making request: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/edit-request")
    public ResponseEntity<List<EditRequestDTO>> getRequests(
            @RequestHeader("Authorization") String token) {
        UUID userId = getUserId(token, jwtUtil);
        List<EditRequestDTO> editRequests = documentService.getEditRequests(userId);
        return ResponseEntity.ok(editRequests);
    }

    @GetMapping("/edit-request/{documentId}")
    public ResponseEntity<Response> getViewableUrlForEditRequest(
            @PathVariable String documentId,
            @RequestParam String organizationName,
            @RequestHeader("Authorization") String token){
        try {
            User user = new User();
            user.setUserId(getUserId(token, jwtUtil));
            Document document = new Document();
            document.setDocumentId(UUID.fromString(documentId));
            EditRequest request = new EditRequest(user, document);
            String url = documentService.getViewableUrlForEditRequest(UUID.fromString(documentId), request, organizationName);
            Response urlResponse = new DocumentViewUrlResponse(url);

            return ResponseEntity.ok(urlResponse);
        } catch (RuntimeException | IOException e) {
            ErrorResponse errorResponse = new ErrorResponse(ERROR_RETRIEVING_DOCUMENT + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/edit-request/{documentId}")
    public ResponseEntity<Response> editDocument(
            @PathVariable String documentId,
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            User user = new User();
            user.setUserId(getUserId(token, jwtUtil));
            Document document = new Document();
            document.setDocumentId(UUID.fromString(documentId));
            EditRequest request = new EditRequest(user, document);
            Document editedDocument = documentService.editDocument(file, request);
            return ResponseEntity.ok(new DocumentEditSuccessResponse(editedDocument));
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = new ErrorResponse("Error editing document: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/edit-request/{documentId}")
    public ResponseEntity<String> rejectRequest(
            @PathVariable String documentId,
            @RequestHeader("Authorization") String token
            ) {
        User user = new User();
        user.setUserId(getUserId(token, jwtUtil));
        Document document = new Document();
        document.setDocumentId(UUID.fromString(documentId));
        EditRequest request = new EditRequest(user, document);
        documentService.rejectEditRequest(request);
        return ResponseEntity.ok("Request deleted successfully");
    }

    @DeleteMapping("/edit-request/{documentId}/cancel")
    public ResponseEntity<String> cancelRequest(
            @PathVariable String documentId,
            @RequestHeader("Authorization") String token
    ) {
        UUID userId = getUserId(token, jwtUtil);
        documentService.cancelEditRequest(UUID.fromString(documentId), userId);
        return ResponseEntity.ok("Request deleted successfully");
    }

    @GetMapping("{documentId}/versions")
    public ResponseEntity<List<DocumentVersion>> getVersions(
            @PathVariable String documentId
    ) {
        return ResponseEntity.ok(documentService.findVersionsOfDocument(UUID.fromString(documentId)));
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