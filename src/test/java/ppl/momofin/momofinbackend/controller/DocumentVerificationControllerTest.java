package ppl.momofin.momofinbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import ppl.momofin.momofinbackend.dto.EditRequestDTO;
import ppl.momofin.momofinbackend.error.UserNotFoundException;
import ppl.momofin.momofinbackend.model.*;
import ppl.momofin.momofinbackend.request.EditRequestRequest;
import ppl.momofin.momofinbackend.security.JwtUtil;
import ppl.momofin.momofinbackend.service.DocumentService;
import ppl.momofin.momofinbackend.service.UserService;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String VALID_TOKEN = "Bearer validToken";
    private static final String INVALID_TOKEN = "Bearer invalidToken";
    private static final String TEST_USERNAME = "testUser";
    private static final User TEST_USER = new User();

    @BeforeEach
    void setUp() {
        when(jwtUtil.validateToken("validToken", TEST_USERNAME)).thenReturn(true);
        when(jwtUtil.extractUsername("validToken")).thenReturn(TEST_USERNAME);
        when(jwtUtil.extractUserId("validToken")).thenReturn("292aeace-0148-4a20-98bf-bf7f12871efe");
        Claims claims = new DefaultClaims();
        TEST_USER.setUserId(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"));
        TEST_USER.setOrganization(new Organization());
        claims.put("roles", Collections.singletonList("ROLE_USER"));
        when(jwtUtil.extractAllClaims("validToken")).thenReturn(claims);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void submitDocument_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        when(documentService.submitDocument(any(), eq(TEST_USERNAME))).thenReturn("Your document test.txt has been successfully submitted");

        mockMvc.perform(multipart("/doc/submit")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentSubmissionResult").value("Your document test.txt has been successfully submitted"));

        verify(documentService).submitDocument(any(), eq(TEST_USERNAME));
    }

    @Test
    void submitDocument_Unauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        when(jwtUtil.validateToken("validToken")).thenReturn(false);
        mockMvc.perform(multipart("/doc/submit")
                        .file(file)
                        .header("Authorization", "Bearer invalidToken"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(documentService);
    }

    @Test
    void submitDocument_IllegalState() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        when(documentService.submitDocument(file, TEST_USERNAME)).thenThrow(new IllegalStateException("File must not be null or empty"));
        mockMvc.perform(multipart("/doc/submit")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Error processing document: File must not be null or empty"));
    }

    @Test
    void verifyDocument_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        Document document = new Document();
        document.setDocumentId(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff"));
        document.setName("test.txt");
        document.setHashString("hash123");
        when(documentService.verifyDocument(any(), eq(TEST_USERNAME))).thenReturn(document);

        mockMvc.perform(multipart("/doc/verify")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.document.documentId").value(document.getDocumentId().toString()))
                .andExpect(jsonPath("$.document.hashString").value("hash123"))
                .andExpect(jsonPath("$.document.name").value("test.txt"));

        verify(documentService).verifyDocument(any(), eq(TEST_USERNAME));
    }

    @Test
    void verifyDocument_NotFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        when(documentService.verifyDocument(any(), eq(TEST_USERNAME))).thenThrow(new IllegalStateException("Document not found"));

        mockMvc.perform(multipart("/doc/verify")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Error verifying document: Document not found"));

        verify(documentService).verifyDocument(any(), eq(TEST_USERNAME));
    }

    @Test
    void verifySpecifiedDocument_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        Document document = new Document();
        document.setDocumentId(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff"));
        document.setName("test.txt");
        document.setHashString("expectedHash");

        when(documentService.verifySpecificDocument(any(), eq(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff")), any())).thenReturn(document);

        mockMvc.perform(multipart("/doc/verify/bd7ef7cf-8875-45fb-9fe5-f36319acddff")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.document.documentId").value(document.getDocumentId().toString()))
                .andExpect(jsonPath("$.document.name").value(document.getName()))
                .andExpect(jsonPath("$.document.hashString").value(document.getHashString()));

        verify(documentService).verifySpecificDocument(any(), eq(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff")), any());
    }



    @Test
    void verifySpecifiedDocument_NotFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        when(documentService.verifySpecificDocument(any(), eq(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff")), any())).thenThrow(new IllegalStateException("Document with ID bd7ef7cf-8875-45fb-9fe5-f36319acddff not found"));

        mockMvc.perform(multipart("/doc/verify/bd7ef7cf-8875-45fb-9fe5-f36319acddff")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("Document with ID bd7ef7cf-8875-45fb-9fe5-f36319acddff not found"));

        verify(documentService).verifySpecificDocument(any(), eq(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff")), any());
    }

    @Test
    void verifySpecifiedDocument_IOException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        when(documentService.verifySpecificDocument(any(), eq(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff")), any())).thenThrow(new IOException("I/O error occurred"));

        mockMvc.perform(multipart("/doc/verify/bd7ef7cf-8875-45fb-9fe5-f36319acddff")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Verification failed: I/O error occurred"));

        verify(documentService).verifySpecificDocument(any(), eq(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff")), any());
    }

    @Test
    void verifySpecifiedDocument_NoSuchAlgorithmException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        when(documentService.verifySpecificDocument(any(), eq(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff")), any())).thenThrow(new NoSuchAlgorithmException("Algorithm not found"));

        mockMvc.perform(multipart("/doc/verify/bd7ef7cf-8875-45fb-9fe5-f36319acddff")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Verification failed: Algorithm not found"));

        verify(documentService).verifySpecificDocument(any(), eq(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff")), any());
    }

    @Test
    void verifySpecifiedDocument_InvalidKeyException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        when(documentService.verifySpecificDocument(any(), eq(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff")), any())).thenThrow(new InvalidKeyException("Invalid key error"));

        mockMvc.perform(multipart("/doc/verify/bd7ef7cf-8875-45fb-9fe5-f36319acddff")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Verification failed: Invalid key error"));

        verify(documentService).verifySpecificDocument(any(), eq(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff")), any());
    }



    @Test
    void findAllDocumentsByOwner_Success() throws Exception {
        String strippedToken = "validToken";

        when(jwtUtil.validateToken(strippedToken, TEST_USERNAME)).thenReturn(true);
        when(jwtUtil.extractUsername(strippedToken)).thenReturn(TEST_USERNAME);

        when(userService.fetchUserByUsername(TEST_USERNAME)).thenReturn(TEST_USER);
        TEST_USER.setName(TEST_USERNAME);
        when(documentService.findAllDocumentsByOwner(TEST_USER)).thenReturn(List.of(new Document()));

        mockMvc.perform(get("/doc/view")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value(TEST_USERNAME))
                .andExpect(jsonPath("$.documents").isArray());

        verify(jwtUtil, times(1)).validateToken(strippedToken, TEST_USERNAME);
        verify(jwtUtil, times(2)).extractUsername(strippedToken);  // Expecting 2 invocations (filter + controller)
        verify(documentService, times(1)).findAllDocumentsByOwner(TEST_USER);
    }

    @Test
    void findAllDocumentsByOwner_InvalidToken() throws Exception {
        when(jwtUtil.validateToken("invalidToken")).thenReturn(false);

        mockMvc.perform(get("/doc/view")
                        .header("Authorization", INVALID_TOKEN))
                .andExpect(status().isForbidden());

        verifyNoInteractions(documentService);
    }

    @Test
    void getViewableUrl_Success_ReturnsOkResponse() throws Exception {
        // Arrange
        UUID documentId = UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff");
        String organizationName = "testorg";
        String viewableUrl = "https://cdn.example.com/document-url";

        when(jwtUtil.extractOrganizationName("validToken")).thenReturn(organizationName);
        when(documentService.getViewableUrl(documentId, UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"), organizationName)).thenReturn(viewableUrl);

        mockMvc.perform(get("/doc/view/bd7ef7cf-8875-45fb-9fe5-f36319acddff")
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(viewableUrl));

        verify(jwtUtil, times(1)).extractUserId("validToken");
        verify(jwtUtil, times(1)).extractOrganizationName("validToken");
        verify(documentService, times(1)).getViewableUrl(documentId, UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"), organizationName);
    }

    @Test
    void getViewableUrl_DocumentServiceThrowsIOException_ReturnsBadRequest() throws Exception {
        // Arrange
        UUID documentId = UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff");
        String organizationName = "testorg";

        when(jwtUtil.extractOrganizationName("validToken")).thenReturn(organizationName);
        when(documentService.getViewableUrl(documentId, UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"), organizationName)).thenThrow(new IOException("File not found: test.txt"));

        mockMvc.perform(get("/doc/view/bd7ef7cf-8875-45fb-9fe5-f36319acddff")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Error retrieving document: File not found: test.txt"));

        verify(jwtUtil, times(1)).extractUserId("validToken");
        verify(jwtUtil, times(1)).extractOrganizationName("validToken");
        verify(documentService, times(1)).getViewableUrl(documentId, UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"), organizationName);
    }

    @Test
    void getDocumentToBeVerifiedTest() throws Exception {
        Document document = new Document("hashString", "documentName");

        when(documentService.fetchDocumentWithDocumentId(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff"))).thenReturn(document);

        mockMvc.perform(get("/doc/verify/bd7ef7cf-8875-45fb-9fe5-f36319acddff")
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hashString").value("hashString"))
                .andExpect(jsonPath("$.name").value("documentName"));
    }

    @Test
    void testRequestEdit_Success() throws Exception {
        Document document = new Document();
        document.setDocumentId(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff"));
        User owner = new User();
        owner.setOrganization(new Organization());
        document.setOwner(owner);
        EditRequest editRequest = new EditRequest(TEST_USER, document);
        EditRequestRequest request = new EditRequestRequest();
        request.setUsername("Bertrum");
        when(documentService.requestEdit(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff"), "Bertrum")).thenReturn(editRequest);

        mockMvc.perform(post("/doc/bd7ef7cf-8875-45fb-9fe5-f36319acddff/request-edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value("bd7ef7cf-8875-45fb-9fe5-f36319acddff"))
                .andExpect(jsonPath("$.userId").value("292aeace-0148-4a20-98bf-bf7f12871efe"));

        verify(documentService).requestEdit(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff"), "Bertrum");
    }

    @Test
    void testRequestEdit_DocumentServiceThrowsIOException_ReturnsBadRequest() throws Exception {
        // Arrange
        Document document = new Document();
        document.setDocumentId(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff"));
        EditRequestRequest request = new EditRequestRequest();
        request.setUsername("Bertrum");
        when(documentService.requestEdit(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff"), "Bertrum")).thenThrow(new UserNotFoundException("User with username Bertrum does not exist"));

        mockMvc.perform(post("/doc/bd7ef7cf-8875-45fb-9fe5-f36319acddff/request-edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Error making request: User with username Bertrum does not exist"));

        verify(documentService).requestEdit(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff"), "Bertrum");
    }

    @Test
    void testGetRequests_Success() throws Exception {
        Document document = new Document();
        document.setOwner(TEST_USER);
        document.setDocumentId(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff"));

        EditRequest editRequest = new EditRequest(TEST_USER, document);
        EditRequestDTO editRequestDTO = EditRequestDTO.toDTO(editRequest);
        List<EditRequestDTO> editRequests = List.of(editRequestDTO);

        when(documentService.getEditRequests(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"))).thenReturn(editRequests);

        mockMvc.perform(get("/doc/edit-request")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documentId").value("bd7ef7cf-8875-45fb-9fe5-f36319acddff"))
                .andExpect(jsonPath("$[0].userId").value("292aeace-0148-4a20-98bf-bf7f12871efe"));

        verify(documentService).getEditRequests(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"));
    }

    @Test
    void testEditDocument_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        // Mock the Document returned after editing
        Document editedDocument = new Document();
        editedDocument.setDocumentId(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff"));
        editedDocument.setName("test-document.pdf");

        when(documentService.editDocument(eq(file), any(EditRequest.class)))
                .thenReturn(editedDocument);

        mockMvc.perform(multipart("/doc/edit-request/{documentId}", "bd7ef7cf-8875-45fb-9fe5-f36319acddff")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value("bd7ef7cf-8875-45fb-9fe5-f36319acddff"))
                .andExpect(jsonPath("$.name").value("test-document.pdf"));
    }
    @Test
    void testEditDocument_MissingFile() throws Exception {
        mockMvc.perform(multipart("/doc/edit-request/{documentId}", "bd7ef7cf-8875-45fb-9fe5-f36319acddff")
                        .header("Authorization", VALID_TOKEN))  // File not provided
                .andExpect(status().isBadRequest());
    }

    @Test
    void  testFetchDocumentVersions() throws Exception {
        UUID documentId = UUID.randomUUID();
        DocumentVersion documentVersion = new DocumentVersion(1, documentId, "test.pdf", "jydkvlklififilviugilfilgi");
        DocumentVersion documentVersion2 = new DocumentVersion(2, documentId, "test.pdf", "iouivoikuicvliiulibivuivilb");
        List<DocumentVersion> versionList = new ArrayList<>();
        versionList.add(documentVersion);
        versionList.add(documentVersion2);
        when(documentService.findVersionsOfDocument(documentId)).thenReturn(versionList);
        mockMvc.perform(get("/doc/"+documentId+"/versions")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].version").value(1))
                .andExpect(jsonPath("$[1].version").value(2))
                .andExpect(jsonPath("$[0].documentId").value(documentId.toString()))
                .andExpect(jsonPath("$[1].documentId").value(documentId.toString()));

    }

    @Test
    void getViewableUrlVersion_Success_ReturnsOkResponse() throws Exception {
        // Arrange
        UUID documentId = UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff");
        String organizationName = "testorg";
        String viewableUrl = "https://cdn.example.com/document-url";

        when(jwtUtil.extractOrganizationName("validToken")).thenReturn(organizationName);
        when(documentService.getViewableUrl(documentId, UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"), organizationName,2)).thenReturn(viewableUrl);

        mockMvc.perform(get("/doc/view/bd7ef7cf-8875-45fb-9fe5-f36319acddff/2")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(viewableUrl));

        verify(jwtUtil, times(1)).extractUserId("validToken");
        verify(jwtUtil, times(1)).extractOrganizationName("validToken");
        verify(documentService, times(1)).getViewableUrl(documentId, UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"), organizationName,2);
    }

    @Test
    void getViewableUrlVersion_DocumentServiceThrowsIOException_ReturnsBadRequest() throws Exception {
        // Arrange
        UUID documentId = UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff");
        String organizationName = "testorg";

        when(jwtUtil.extractOrganizationName("validToken")).thenReturn(organizationName);
        when(documentService.getViewableUrl(documentId, UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"), organizationName, 333)).thenThrow(new IOException("File not found: test.txt"));

        mockMvc.perform(get("/doc/view/bd7ef7cf-8875-45fb-9fe5-f36319acddff/333")
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Error retrieving document: File not found: test.txt"));

        verify(jwtUtil, times(1)).extractUserId("validToken");
        verify(jwtUtil, times(1)).extractOrganizationName("validToken");
        verify(documentService, times(1)).getViewableUrl(documentId, UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"), organizationName, 333);
    }
}