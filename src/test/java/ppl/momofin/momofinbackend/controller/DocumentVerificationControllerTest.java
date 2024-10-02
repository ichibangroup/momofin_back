package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ppl.momofin.momofinbackend.config.SecurityConfig;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.service.DocumentService;
import java.security.NoSuchAlgorithmException;

import java.io.IOException;

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

    @InjectMocks
    private DocumentVerificationController documentVerificationController;

    @Test
    void submitDocument_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        when(documentService.submitDocument(any())).thenReturn("hash123");

        mockMvc.perform(multipart("/doc/submit").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentSubmissionResult").value("hash123"));
    }

    @Test
    void submitDocument_Failure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        when(documentService.submitDocument(any())).thenThrow(new IOException("Test exception"));

        mockMvc.perform(multipart("/doc/submit").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Error processing document: Test exception"));
    }

    @Test
    void verifyDocumentSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        Document document = new Document();
        document.setDocumentId(1L);
        document.setName("test.txt");
        document.setHashString("hash123");
        when(documentService.verifyDocument(any())).thenReturn(document);

        mockMvc.perform(multipart("/doc/verify").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.document.documentId").value(document.getDocumentId()))
                .andExpect(jsonPath("$.document.hashString").value("hash123"))
                .andExpect(jsonPath("$.document.name").value("test.txt"));
    }

    @Test
    void verifyDocument_Failure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        when(documentService.verifyDocument(any())).thenThrow(new NoSuchAlgorithmException("Test exception"));

        mockMvc.perform(multipart("/doc/verify").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Error verifying document: Test exception"));
    }
}
