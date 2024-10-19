package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.DocumentRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;

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

    @Mock
    private UserRepository userRepository;

    @Mock
    private CDNService cdnService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private MockMultipartFile mockFile;
    private User mockUser;
    private String mockUsername;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "secretKey", System.getenv("SECRET_KEY"));
        mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello, World!".getBytes());
        mockUsername = "test user";
        mockUser = new User();
        Organization organization = new Organization("Momofin");
        mockUser.setName(mockUsername);
        mockUser.setOrganization(organization);
    }

    @Test
    void submitDocumentNewDocument() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        when(documentRepository.findByHashString(any())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(mockUser));
        when(cdnService.uploadFile(eq(mockFile), eq(mockUser), any())).thenReturn(new Document());

        String result = documentService.submitDocument(mockFile, mockUsername);

        assertNotNull(result);
        assertFalse(result.contains("this document has already been submitted before"));
        verify(userRepository).findByUsername(mockUsername);
    }

    @Test
    void submitDocumentExistingDocument() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        when(documentRepository.findByHashString(any())).thenReturn(Optional.of(new Document("hashString", "Existing Document")));
        String result = documentService.submitDocument(mockFile, mockUsername);

        assertNotNull(result);
        assertTrue(result.contains("has already been stored before"));
        verify(userRepository, never()).findByUsername(mockUsername);
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void submitDocumentNullFile() {
        assertThrows(IllegalArgumentException.class, () -> documentService.submitDocument(null, mockUsername));
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void submitDocumentNoUser() {
        when(documentRepository.findByHashString(any())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> documentService.submitDocument(mockFile, mockUsername));
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void submitDocumentEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> documentService.submitDocument(emptyFile, mockUsername));
    }

    @Test
    void verifyDocumentExistingDocument() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        Document mockDocument = new Document();
        when(documentRepository.findByHashString(any())).thenReturn(Optional.of(mockDocument));

        Document result = documentService.verifyDocument(mockFile, mockUsername);

        assertNotNull(result);
        assertEquals(mockDocument, result);
    }

    @Test
    void verifyDocumentNonExistentDocument() {
        when(documentRepository.findByHashString(any())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> documentService.verifyDocument(mockFile, mockUsername));
    }

    @Test
    void verifyDocumentNullFile() {
        assertThrows(IllegalArgumentException.class, () -> documentService.verifyDocument(null, mockUsername));
    }

    @Test
    void verifyDocumentEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> documentService.verifyDocument(emptyFile, mockUsername));
    }

    @Test
    void verifySpecificDocumentSuccess() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        // Arrange
        Long documentId = 1L;
        String expectedHash = "testHash"; // This is the expected hash from the document

        // Create a mock document with the expected hash
        Document mockDocument = new Document();
        mockDocument.setHashString(expectedHash); // Set the correct hash
        mockDocument.setOwner(mockUser); // Ensure the document has an owner

        // Mock the behavior for finding the document by ID and username
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(mockUser));

        // Use a spy to partially mock DocumentServiceImpl
        DocumentServiceImpl documentServiceSpy = spy(documentService);
        doReturn(expectedHash).when(documentServiceSpy).generateHash(any(MultipartFile.class)); // Simulate correct hash

        // Act
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test content".getBytes());
        Document result = documentServiceSpy.verifySpecificDocument(mockFile, documentId, mockUsername);

        // Assert
        assertNotNull(result);
        assertEquals(mockDocument.getHashString(), result.getHashString());
    }


    @Test
    void verifySpecificDocumentNullFile() {
        Long documentId = 1L;

        assertThrows(IllegalArgumentException.class, () -> {
            documentService.verifySpecificDocument(null, documentId, mockUsername);
        });
    }

    @Test
    void verifySpecificDocumentEmptyFile() {
        Long documentId = 1L;
        mockFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> {
            documentService.verifySpecificDocument(mockFile, documentId, mockUsername);
        });
    }

    @Test
    void verifySpecificDocumentDocumentNotFound() {
        Long documentId = 1L;
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            documentService.verifySpecificDocument(mockFile, documentId, mockUsername);
        });
    }

    @Test
    void verifySpecificDocumentUnauthorizedUser() {
        Long documentId = 1L;
        Document mockDocument = new Document();
        mockDocument.setHashString("expectedHash");
        mockDocument.setOwner(mockUser);

        User unauthorizedUser = new User();
        unauthorizedUser.setName("Unauthorized User");
        Organization organization = new Organization("Another Organization");
        unauthorizedUser.setOrganization(organization);

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(unauthorizedUser)); // Simulate unauthorized access

        assertThrows(IllegalStateException.class, () -> {
            documentService.verifySpecificDocument(mockFile, documentId, mockUsername);
        });    }

    @Test
    void verifySpecificDocumentHashMismatch() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        Long documentId = 1L;
        Document mockDocument = new Document();
        String expectedHash = "testHash"; // This should match the mock document's hash
        String wrongHash = "wrongHash"; // This will be used to simulate a hash mismatch

        // Set up the mock user who is the owner
        mockDocument.setOwner(mockUser); // Ensure that the document has an owner
        mockDocument.setHashString(expectedHash); // Set the correct hash in the mock document

        // Mock the behavior for finding the document by ID
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(mockUser)); // User is the owner

        DocumentServiceImpl documentServiceSpy = spy(documentService);

        // Mock the generateHash method
        doReturn(wrongHash).when(documentServiceSpy).generateHash(any(MultipartFile.class));

        // Act & Assert
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test content".getBytes());

        assertThrows(IllegalArgumentException.class, () -> {
            documentServiceSpy.verifySpecificDocument(mockFile, documentId, mockUsername);
        });
    }


    @Test
    void generateHashConsistentResults() {
        String hash1 = invokeGenerateHash(mockFile);
        String hash2 = invokeGenerateHash(mockFile);

        assertEquals(hash1, hash2, "Hash should be consistent for the same file");
    }

    @Test
    void generateHashDifferentResults()  {
        String hash1 = invokeGenerateHash(mockFile);

        MockMultipartFile differentFile = new MockMultipartFile("file", "different.txt", "text/plain", "Different content".getBytes());
        String hash2 = invokeGenerateHash(differentFile);

        assertNotEquals(hash1, hash2, "Hash should be different for different files");
    }

    private String invokeGenerateHash(MockMultipartFile file) {
        return (String) ReflectionTestUtils.invokeMethod(documentService, "generateHash", file);
    }

    @Test
    void findalldocumentsbyowner() {
        when(documentRepository.findAllByOwner(any())).thenReturn(null);
        documentService.findAllDocumentsByOwner(mockUser);
        verify(documentRepository).findAllByOwner(mockUser);
    }

    @Test
    void findalldocumentsbyownerNull() {
        assertThrows(IllegalArgumentException.class, () -> documentService.findAllDocumentsByOwner(null));
    }
}