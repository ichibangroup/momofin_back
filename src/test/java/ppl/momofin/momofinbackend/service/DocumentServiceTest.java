package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.repository.DocumentRepository;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "SECRET_KEY", System.getenv("SECRET_KEY"));
        mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello, World!".getBytes());
    }

    @Test
    void submitDocumentNewDocument() throws Exception {
        when(documentRepository.findByHashString(any())).thenReturn(Optional.empty());
        when(documentRepository.save(any())).thenReturn(new Document());

        String result = documentService.submitDocument(mockFile);

        assertNotNull(result);
        assertFalse(result.contains("this document has already been submitted before"));
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void submitDocumentExistingDocument() throws Exception {
        when(documentRepository.findByHashString(any())).thenReturn(Optional.of(new Document()));

        String result = documentService.submitDocument(mockFile);

        assertNotNull(result);
        assertTrue(result.contains("this document has already been submitted before"));
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void submitDocumentNullFile() {
        assertThrows(IllegalArgumentException.class, () -> documentService.submitDocument(null));
    }

    @Test
    void submitDocumentEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> documentService.submitDocument(emptyFile));
    }

    @Test
    void verifyDocumentExistingDocument() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        Document mockDocument = new Document();
        when(documentRepository.findByHashString(any())).thenReturn(Optional.of(mockDocument));

        Document result = documentService.verifyDocument(mockFile);

        assertNotNull(result);
        assertEquals(mockDocument, result);
    }

    @Test
    void verifyDocumentNonExistentDocument() {
        when(documentRepository.findByHashString(any())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> documentService.verifyDocument(mockFile));
    }

    @Test
    void verifyDocumentNullFile() {
        assertThrows(IllegalArgumentException.class, () -> documentService.verifyDocument(null));
    }

    @Test
    void verifyDocumentEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> documentService.verifyDocument(emptyFile));
    }

    @Test
    void generateHashConsistentResults() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String hash1 = invokeGenerateHash(mockFile);
        String hash2 = invokeGenerateHash(mockFile);

        assertEquals(hash1, hash2, "Hash should be consistent for the same file");
    }

    @Test
    void generateHashDifferentResults() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String hash1 = invokeGenerateHash(mockFile);

        MockMultipartFile differentFile = new MockMultipartFile("file", "different.txt", "text/plain", "Different content".getBytes());
        String hash2 = invokeGenerateHash(differentFile);

        assertNotEquals(hash1, hash2, "Hash should be different for different files");
    }

    private String invokeGenerateHash(MockMultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        return (String) ReflectionTestUtils.invokeMethod(documentService, "generateHash", file);
    }
}