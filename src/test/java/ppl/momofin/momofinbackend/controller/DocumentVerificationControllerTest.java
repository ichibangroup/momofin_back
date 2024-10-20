package ppl.momofin.momofinbackend.controller;

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
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.security.JwtUtil;
import ppl.momofin.momofinbackend.service.DocumentService;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.UserService;
import java.util.List;

import java.util.Collections;

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

    private static final String VALID_TOKEN = "Bearer validToken";
    private static final String INVALID_TOKEN = "Bearer invalidToken";
    private static final String TEST_USERNAME = "testUser";
    private static final User TEST_USER = new User();

    @BeforeEach
    void setUp() {
        when(jwtUtil.validateToken("validToken", TEST_USERNAME)).thenReturn(true);
        when(jwtUtil.extractUsername("validToken")).thenReturn(TEST_USERNAME);
        Claims claims = new DefaultClaims();
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
        document.setDocumentId(1L);
        document.setName("test.txt");
        document.setHashString("hash123");
        when(documentService.verifyDocument(any(), eq(TEST_USERNAME))).thenReturn(document);

        mockMvc.perform(multipart("/doc/verify")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.document.documentId").value(document.getDocumentId()))
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
}