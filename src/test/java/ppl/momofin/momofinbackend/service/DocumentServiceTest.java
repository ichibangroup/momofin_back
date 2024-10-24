package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
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
import java.util.UUID;

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

        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        String expectedHash = "testHash";

        Document mockDocument = new Document();
        mockDocument.setHashString(expectedHash);
        mockDocument.setOwner(mockUser);

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(mockUser));

        DocumentServiceImpl documentServiceSpy = spy(documentService);
        doReturn(expectedHash).when(documentServiceSpy).generateHash(any(MultipartFile.class)); // Simulate correct hash

        MockMultipartFile testFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test content".getBytes());
        Document result = documentServiceSpy.verifySpecificDocument(testFile, documentId, mockUsername);

        assertNotNull(result);
        assertEquals(mockDocument.getHashString(), result.getHashString());
    }


    @Test
    void verifySpecificDocumentNullFile() {
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");

        assertThrows(IllegalArgumentException.class, () -> {
            documentService.verifySpecificDocument(null, documentId, mockUsername);
        });
    }

    @Test
    void verifySpecificDocumentEmptyFile() {
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        mockFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> {
            documentService.verifySpecificDocument(mockFile, documentId, mockUsername);
        });
    }

    @Test
    void verifySpecificDocumentDocumentNotFound() {
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            documentService.verifySpecificDocument(mockFile, documentId, mockUsername);
        });
    }

    @Test
    void verifySpecificDocumentAuthorizedUser() throws Exception {
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        String expectedHash = "expectedHash";

        Document mockDocument = new Document();
        mockDocument.setHashString(expectedHash);
        mockDocument.setOwner(mockUser);

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(mockUser));

        DocumentServiceImpl documentServiceSpy = spy(documentService);
        doReturn(expectedHash).when(documentServiceSpy).generateHash(any(MultipartFile.class));

        MockMultipartFile testFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test content".getBytes());

        Document result = documentServiceSpy.verifySpecificDocument(testFile, documentId, mockUsername);

        assertNotNull(result);
        assertEquals(mockDocument.getHashString(), result.getHashString());
    }


    @Test
    void verifySpecificDocumentUnauthorizedUser()  {
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");

        MockMultipartFile testFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        Document mockDocument = new Document();
        mockDocument.setDocumentId(documentId);
        mockDocument.setHashString("expectedHash");
        mockDocument.setOwner(mockUser);

        User unauthorizedUser = new User();
        unauthorizedUser.setName("Unauthorized User");
        Organization organization = new Organization("Another Organization");
        unauthorizedUser.setOrganization(organization);

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(unauthorizedUser));

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            documentService.verifySpecificDocument(testFile, documentId, mockUsername);
        });

        assertEquals("You are not authorized to verify this document.", exception.getMessage());

        verify(userRepository).findByUsername(usernameCaptor.capture());
        assertEquals(mockUsername, usernameCaptor.getValue());
        assertNotEquals(mockDocument.getOwner(), unauthorizedUser);
    }

    @Test
    void verifySpecificDocumentOwnerNotFound() {
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");

        Document mockDocument = new Document();
        mockDocument.setDocumentId(documentId);
        mockDocument.setHashString("expectedHash");

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            documentService.verifySpecificDocument(mockFile, documentId, mockUsername);
        });

        assertEquals("You are not authorized to verify this document.", exception.getMessage());

        verify(documentRepository).findById(documentId);
        verify(userRepository).findByUsername(mockUsername);
    }



    @Test
    void verifySpecificDocumentOwnerMismatch() {
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");

        Document mockDocument = new Document();
        mockDocument.setDocumentId(documentId);
        mockDocument.setHashString("expectedHash");

        User differentOwner = new User();
        differentOwner.setName("Different User");
        Organization organization = new Organization("Different Organization");
        differentOwner.setOrganization(organization);
        mockDocument.setOwner(differentOwner);

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(mockUser));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            documentService.verifySpecificDocument(mockFile, documentId, mockUsername);
        });

        assertEquals("You are not authorized to verify this document.", exception.getMessage());

        verify(documentRepository).findById(documentId);
        verify(userRepository).findByUsername(mockUsername);
    }




    @Test
    void verifySpecificDocumentHashMismatch() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        Document mockDocument = new Document();
        String expectedHash = "testHash";
        String wrongHash = "wrongHash";

        mockDocument.setOwner(mockUser);
        mockDocument.setHashString(expectedHash);

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(mockUser));

        DocumentServiceImpl documentServiceSpy = spy(documentService);

        doReturn(wrongHash).when(documentServiceSpy).generateHash(any(MultipartFile.class));

        MockMultipartFile testFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test content".getBytes());

        assertThrows(IllegalArgumentException.class, () -> {
            documentServiceSpy.verifySpecificDocument(testFile, documentId, mockUsername);
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

    @Test
    void getViewableUrl_DocumentExists_ReturnsViewableUrl() throws IOException {
        // Arrange
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        String username = "testuser";
        String organizationName = "testorg";
        String filename = "testfile.pdf";
        String expectedUrl = "https://cdn.example.com/signed-url";

        Document document = new Document();
        document.setDocumentId(documentId);
        document.setName(filename);

        when(documentRepository.findByDocumentId(documentId)).thenReturn(Optional.of(document));
        when(cdnService.getViewableUrl(filename, username, organizationName)).thenReturn(expectedUrl);

        // Act
        String actualUrl = documentService.getViewableUrl(documentId, username, organizationName);

        // Assert
        assertEquals(expectedUrl, actualUrl);
        verify(documentRepository, times(1)).findByDocumentId(documentId);
        verify(cdnService, times(1)).getViewableUrl(filename, username, organizationName);
    }

    @Test
    void getViewableUrl_DocumentDoesNotExist_ThrowsIllegalArgumentException() throws IOException {
        // Arrange
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        String username = "testuser";
        String organizationName = "testorg";

        when(documentRepository.findByDocumentId(documentId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                documentService.getViewableUrl(documentId, username, organizationName));

        assertEquals("Document with id " + documentId + " does not exist", exception.getMessage());
        verify(documentRepository, times(1)).findByDocumentId(documentId);
        verify(cdnService, never()).getViewableUrl(anyString(), anyString(), anyString());
    }

    @Test
    void getViewableUrl_CdnServiceThrowsIOException_ThrowsIOException() throws IOException {
        // Arrange
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        String username = "testuser";
        String organizationName = "testorg";
        String filename = "testfile.pdf";

        Document document = new Document();
        document.setDocumentId(documentId);
        document.setName(filename);

        when(documentRepository.findByDocumentId(documentId)).thenReturn(Optional.of(document));
        when(cdnService.getViewableUrl(filename, username, organizationName)).thenThrow(new IOException("Failed to get URL"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () ->
                documentService.getViewableUrl(documentId, username, organizationName));

        assertEquals("Failed to get URL", exception.getMessage());
        verify(documentRepository, times(1)).findByDocumentId(documentId);
        verify(cdnService, times(1)).getViewableUrl(filename, username, organizationName);
    }

    @Test
    void fetchDocumentByIdSuccess() {
        Document document = new Document();
        when(documentRepository.findByDocumentId(UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b"))).thenReturn(Optional.of(document));

        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        Document returnedDocument = documentService.fetchDocumentWithDocumentId(documentId);

        assertEquals(document, returnedDocument);
    }

    @Test
    void fetchDocumentByIdFailed() {
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        when(documentRepository.findByDocumentId(documentId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> documentService.fetchDocumentWithDocumentId(documentId) );
    }
}