package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ppl.momofin.momofinbackend.config.SecurityConfig;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.security.JwtUtil;
import ppl.momofin.momofinbackend.service.DocumentService;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentVerificationController.class)
@Import(SecurityConfig.class)
public class DocumentVerificationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private JwtUtil jwtUtil;

    private static final String VALID_TOKEN = "Bearer validToken";
    private static final String INVALID_TOKEN = "Bearer invalidToken";
    private static final String TEST_USERNAME = "testUser";

    @Test
    void submitDocument_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        when(jwtUtil.extractUsername("validToken")).thenReturn(TEST_USERNAME);
        when(documentService.submitDocument(any(), eq(TEST_USERNAME))).thenReturn("hash123");

        mockMvc.perform(multipart("/doc/submit")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentSubmissionResult").value("hash123"));

        verify(jwtUtil, times(2)).validateToken("validToken");
        verify(jwtUtil, times(2)).extractUsername("validToken");
        verify(documentService).submitDocument(any(), eq(TEST_USERNAME));
    }

    @Test
    void submitDocument_InvalidToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        when(jwtUtil.validateToken("invalidToken")).thenReturn(false);

        mockMvc.perform(multipart("/doc/submit")
                        .file(file)
                        .header("Authorization", INVALID_TOKEN))
                .andExpect(status().isForbidden());

        verifyNoInteractions(documentService);
    }

    @Test
    void submitDocument_Failure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        when(jwtUtil.extractUsername("validToken")).thenReturn(TEST_USERNAME);
        when(documentService.submitDocument(any(), eq(TEST_USERNAME))).thenThrow(new IOException("Test exception"));

        mockMvc.perform(multipart("/doc/submit")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Error processing document: Test exception"));

        verify(jwtUtil, times(2)).validateToken("validToken");
        verify(jwtUtil, times(2)).extractUsername("validToken");
        verify(documentService).submitDocument(any(), eq(TEST_USERNAME));
    }

    @Test
    void verifyDocumentSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        Document document = new Document();
        document.setDocumentId(1L);
        document.setName("test.txt");
        document.setHashString("hash123");
        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        when(jwtUtil.extractUsername("validToken")).thenReturn(TEST_USERNAME);
        when(documentService.verifyDocument(any(), eq(TEST_USERNAME))).thenReturn(document);

        mockMvc.perform(multipart("/doc/verify")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.document.documentId").value(document.getDocumentId()))
                .andExpect(jsonPath("$.document.hashString").value("hash123"))
                .andExpect(jsonPath("$.document.name").value("test.txt"));

        verify(jwtUtil, times(2)).validateToken("validToken");
        verify(jwtUtil, times(2)).extractUsername("validToken");
        verify(documentService).verifyDocument(any(), eq(TEST_USERNAME));
    }

    @Test
    void verifyDocument_InvalidToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        when(jwtUtil.validateToken("invalidToken")).thenReturn(false);

        mockMvc.perform(multipart("/doc/verify")
                        .file(file)
                        .header("Authorization", INVALID_TOKEN))
                .andExpect(status().isForbidden());

        verifyNoInteractions(documentService);
    }

    @Test
    void verifyDocument_Failure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        when(jwtUtil.extractUsername("validToken")).thenReturn(TEST_USERNAME);
        when(documentService.verifyDocument(any(), eq(TEST_USERNAME))).thenThrow(new NoSuchAlgorithmException("Test exception"));

        mockMvc.perform(multipart("/doc/verify")
                        .file(file)
                        .header("Authorization", VALID_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Error verifying document: Test exception"));

        verify(jwtUtil, times(2)).validateToken("validToken");
        verify(jwtUtil, times(2)).extractUsername("validToken");
        verify(documentService).verifyDocument(any(), eq(TEST_USERNAME));
    }
}