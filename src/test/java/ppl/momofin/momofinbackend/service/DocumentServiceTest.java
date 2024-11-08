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
import ppl.momofin.momofinbackend.dto.EditRequestDTO;
import ppl.momofin.momofinbackend.error.UserNotFoundException;
import ppl.momofin.momofinbackend.model.*;
import ppl.momofin.momofinbackend.repository.DocumentRepository;
import ppl.momofin.momofinbackend.repository.DocumentVersionRepository;
import ppl.momofin.momofinbackend.repository.EditRequestRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
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
    @Mock
    private EditRequestRepository editRequestRepository;
    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private MockMultipartFile mockFile;
    private User mockUser;
    private String mockUsername;
    private Document document;
    private UUID documentId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "secretKey", System.getenv("SECRET_KEY"));
        mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello, World!".getBytes());
        mockUsername = "test user";
        mockUser = new User();
        Organization organization = new Organization("Momofin");
        mockUser.setName(mockUsername);
        mockUser.setUserId(UUID.randomUUID());
        mockUser.setOrganization(organization);

        document = new Document();
        documentId = UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe");
        document.setDocumentId(documentId);
        document.setName("testfile.pdf");
    }

    @Test
    void submitDocumentNewDocument() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        when(documentRepository.findByHashString(any())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(mockUser));
        when(cdnService.submitDocument(eq(mockFile), eq(mockUser), any())).thenReturn(new Document());

        String result = documentService.submitDocument(mockFile, mockUsername);

        assertNotNull(result);
        assertFalse(result.contains("this document has already been submitted before"));
        verify(userRepository).findByUsername(mockUsername);
    }

    @Test
    void submitDocumentExistingDocument() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        when(documentRepository.findByHashString(any())).thenReturn(Optional.of(new Document("hashString", "Existing Document",1)));
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

        String expectedHash = "testHash";

        Document mockDocument = new Document();
        mockDocument.setHashString(expectedHash);
        mockDocument.setOwner(mockUser);

        when(documentRepository.findById(document.getDocumentId())).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(mockUser));

        DocumentServiceImpl documentServiceSpy = spy(documentService);
        doReturn(expectedHash).when(documentServiceSpy).generateHash(any(MultipartFile.class)); // Simulate correct hash

        MockMultipartFile testFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test content".getBytes());
        Document result = documentServiceSpy.verifySpecificDocument(testFile, document.getDocumentId(), mockUsername);

        assertNotNull(result);
        assertEquals(mockDocument.getHashString(), result.getHashString());
    }


    @Test
    void verifySpecificDocumentNullFile() {

        assertThrows(IllegalArgumentException.class, () -> {
            documentService.verifySpecificDocument(null, documentId, mockUsername);
        });
    }

    @Test
    void verifySpecificDocumentEmptyFile() {
        mockFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> {
            documentService.verifySpecificDocument(mockFile, documentId, mockUsername);
        });
    }

    @Test
    void verifySpecificDocumentDocumentNotFound() {
        when(documentRepository.findById(document.getDocumentId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> {
            documentService.verifySpecificDocument(mockFile, documentId, mockUsername);
        });
    }

    @Test
    void verifySpecificDocumentAuthorizedUser() throws Exception {
        String expectedHash = "expectedHash";

        Document mockDocument = new Document();
        mockDocument.setHashString(expectedHash);
        mockDocument.setOwner(mockUser);

        when(documentRepository.findById(document.getDocumentId())).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(mockUser));

        DocumentServiceImpl documentServiceSpy = spy(documentService);
        doReturn(expectedHash).when(documentServiceSpy).generateHash(any(MultipartFile.class));

        MockMultipartFile testFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test content".getBytes());

        Document result = documentServiceSpy.verifySpecificDocument(testFile, document.getDocumentId(), mockUsername);

        assertNotNull(result);
        assertEquals(mockDocument.getHashString(), result.getHashString());
    }


    @Test
    void verifySpecificDocumentUnauthorizedUser()  {

        MockMultipartFile testFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        Document mockDocument = new Document();
        mockDocument.setDocumentId(document.getDocumentId());
        mockDocument.setHashString("expectedHash");
        mockDocument.setOwner(mockUser);

        User unauthorizedUser = new User();
        unauthorizedUser.setName("Unauthorized User");
        Organization organization = new Organization("Another Organization");
        unauthorizedUser.setOrganization(organization);

        when(documentRepository.findById(document.getDocumentId())).thenReturn(Optional.of(mockDocument));
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

        Document mockDocument = new Document();
        mockDocument.setDocumentId(document.getDocumentId());
        mockDocument.setHashString("expectedHash");

        when(documentRepository.findById(document.getDocumentId())).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            documentService.verifySpecificDocument(mockFile, documentId, mockUsername);
        });

        assertEquals("You are not authorized to verify this document.", exception.getMessage());

        verify(documentRepository).findById(document.getDocumentId());
        verify(userRepository).findByUsername(mockUsername);
    }



    @Test
    void verifySpecificDocumentOwnerMismatch() {

        Document mockDocument = new Document();
        mockDocument.setDocumentId(document.getDocumentId());
        mockDocument.setHashString("expectedHash");

        User differentOwner = new User();
        differentOwner.setName("Different User");
        Organization organization = new Organization("Different Organization");
        differentOwner.setOrganization(organization);
        mockDocument.setOwner(differentOwner);

        when(documentRepository.findById(document.getDocumentId())).thenReturn(Optional.of(mockDocument));
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(mockUser));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            documentService.verifySpecificDocument(mockFile, documentId, mockUsername);
        });

        assertEquals("You are not authorized to verify this document.", exception.getMessage());

        verify(documentRepository).findById(document.getDocumentId());
        verify(userRepository).findByUsername(mockUsername);
    }




    @Test
    void verifySpecificDocumentHashMismatch() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        Document mockDocument = new Document();
        String expectedHash = "testHash";
        String wrongHash = "wrongHash";

        mockDocument.setOwner(mockUser);
        mockDocument.setHashString(expectedHash);

        when(documentRepository.findById(document.getDocumentId())).thenReturn(Optional.of(mockDocument));
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
        UUID userId = UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe");
        String organizationName = "testorg";
        String expectedUrl = "https://cdn.example.com/signed-url";

        when(documentRepository.findByDocumentId(document.getDocumentId())).thenReturn(Optional.of(document));
        when(cdnService.getViewableUrl(document, userId, organizationName)).thenReturn(expectedUrl);

        // Act
        String actualUrl = documentService.getViewableUrl(document.getDocumentId(), userId, organizationName);

        // Assert
        assertEquals(expectedUrl, actualUrl);
        verify(documentRepository, times(1)).findByDocumentId(document.getDocumentId());
        verify(cdnService, times(1)).getViewableUrl(document, userId, organizationName);
    }

    @Test
    void getViewableUrl_DocumentDoesNotExist_ThrowsIllegalArgumentException() throws IOException {
        // Arrange
        UUID userId = UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe");
        String organizationName = "testorg";

        when(documentRepository.findByDocumentId(document.getDocumentId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                documentService.getViewableUrl(documentId, userId, organizationName));

        assertEquals("Document with id " + document.getDocumentId() + " does not exist", exception.getMessage());
        verify(documentRepository, times(1)).findByDocumentId(document.getDocumentId());
        verify(cdnService, never()).getViewableUrl(any(Document.class), any(UUID.class), anyString());
    }

    @Test
    void getViewableUrl_CdnServiceThrowsIOException_ThrowsIOException() throws IOException {
        // Arrange
        UUID userId = UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe");
        String organizationName = "testorg";

        when(documentRepository.findByDocumentId(document.getDocumentId())).thenReturn(Optional.of(document));
        when(cdnService.getViewableUrl(document, userId, organizationName)).thenThrow(new IOException("Failed to get URL"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () ->
                documentService.getViewableUrl(documentId, userId, organizationName));

        assertEquals("Failed to get URL", exception.getMessage());
        verify(documentRepository, times(1)).findByDocumentId(document.getDocumentId());
        verify(cdnService, times(1)).getViewableUrl(document, userId, organizationName);
    }

    @Test
    void fetchDocumentByIdSuccess() {
        when(documentRepository.findByDocumentId(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"))).thenReturn(Optional.of(document));

        Document returnedDocument = documentService.fetchDocumentWithDocumentId(documentId);

        assertEquals(document, returnedDocument);
    }

    @Test
    void fetchDocumentByIdFailed() {
        when(documentRepository.findByDocumentId(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"))).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> documentService.fetchDocumentWithDocumentId(documentId) );
    }

    @Test
    void testRequestEdit_Success() {
        // Mock the expected behavior
        EditRequest editRequest = new EditRequest();
        editRequest.setDocument(document);
        editRequest.setUser(mockUser);
        when(editRequestRepository.save(any(EditRequest.class))).thenReturn(editRequest);
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.of(mockUser));

        // Execute the method
        EditRequest result = documentService.requestEdit(document.getDocumentId(), mockUsername);

        // Verify interactions and result
        verify(editRequestRepository).save(any(EditRequest.class));
        assertEquals(document.getDocumentId(), result.getDocumentId());
        assertEquals(mockUser.getUserId(), result.getUserId());
    }

    @Test
    void testRequestEdit_UserDoesNotExist() {
        // Mock the expected behavior
        EditRequest editRequest = new EditRequest();
        editRequest.setDocument(document);
        editRequest.setUser(mockUser);
        when(userRepository.findByUsername(mockUsername)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            documentService.requestEdit(documentId, mockUsername);
        });
        String expectedErrorMessage = "User with username " + mockUsername + " does not exist";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void testGetEditRequests_ReturnsRequestsForUser() {
        // Set up sample edit requests
        EditRequest editRequest1 = new EditRequest();
        editRequest1.setDocument(document);
        document.setOwner(mockUser);
        editRequest1.setUser(mockUser);
        EditRequestDTO editRequest1DTO = EditRequestDTO.toDTO(editRequest1);

        List<EditRequestDTO> editRequests = new ArrayList<>();
        editRequests.add(editRequest1DTO);
        when(editRequestRepository.findByUserIdAsDTO(mockUser.getUserId())).thenReturn(editRequests);

        // Execute the method
        List<EditRequestDTO> result = documentService.getEditRequests(mockUser.getUserId());

        // Verify interactions and result
        verify(editRequestRepository).findByUserIdAsDTO(mockUser.getUserId());
        assertEquals(1, result.size());
        assertEquals(editRequest1DTO, result.get(0));
    }

    @Test
    void testEditDocument_Success() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        EditRequest editRequest = new EditRequest();
        editRequest.setDocument(document);
        document.setOwner(mockUser);
        editRequest.setUser(mockUser);

        Document updatedDocument = new Document();
        updatedDocument.setDocumentId(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"));

        when(userRepository.findById(mockUser.getUserId())).thenReturn(Optional.of(mockUser));
        when(documentRepository.findByDocumentId(editRequest.getDocumentId())).thenReturn(Optional.of(document));

        // Mock interactions
        when(cdnService.editDocument(any(MultipartFile.class), any(Document.class), anyString(), eq(mockUser))).thenReturn(updatedDocument);
        when(editRequestRepository.existsById(editRequest.getId())).thenReturn(true);

        // Execute the method
        Document result = documentService.editDocument(mockFile, editRequest);

        // Verify interactions and result
        verify(cdnService).editDocument(eq(mockFile), eq(document), anyString(), eq(mockUser));
        verify(editRequestRepository).delete(editRequest);
        assertEquals(updatedDocument.getDocumentId(), result.getDocumentId());
    }

    @Test
    void testEditDocument_FileEmpty_ThrowsException() {
        // Set up an empty file and an EditRequest
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "application/pdf", new byte[0]);
        EditRequest editRequest = new EditRequest();
        editRequest.setDocument(document);

        // Execute and verify exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            documentService.editDocument(emptyFile, editRequest);
        });
        assertEquals("File must not be null or empty", exception.getMessage());
    }

    @Test
    void testEditDocument_FileNull_ThrowsException() {
        EditRequest editRequest = new EditRequest();
        editRequest.setDocument(document);

        // Execute and verify exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            documentService.editDocument(null, editRequest);
        });
        assertEquals("File must not be null or empty", exception.getMessage());
    }

    @Test
    void testEditDocument_EditRequestNotExists_ThrowsException() {

        EditRequest editRequest = new EditRequest();
        editRequest.setDocument(document);
        when(editRequestRepository.existsById(editRequest.getId())).thenReturn(false);

        // Execute and verify exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            documentService.editDocument(mockFile, editRequest);
        });
        assertEquals("Edit request not found in the database.", exception.getMessage());
    }

    @Test
    void testRejectEditRequest_Success() {
        // Set up EditRequest
        EditRequest editRequest = new EditRequest();
        editRequest.setDocument(document);
        editRequest.setUser(mockUser);

        // Execute the method
        documentService.rejectEditRequest(editRequest);

        // Verify interactions
        verify(editRequestRepository).delete(editRequest);
    }

    @Test
    void  testFetchDocumentVersions() {
        DocumentVersion documentVersion = new DocumentVersion(1, documentId, "test.pdf", "jydkvlklififilviugilfilgi");
        DocumentVersion documentVersion2 = new DocumentVersion(2, documentId, "test.pdf", "iouivoikuicvliiulibivuivilb");
        List<DocumentVersion> versionList = new ArrayList<>();
        versionList.add(documentVersion);
        versionList.add(documentVersion2);
        when(documentVersionRepository.findById_DocumentId(documentId)).thenReturn(versionList);

        List<DocumentVersion> results = documentService.findVersionsOfDocument(documentId);

        assertEquals(results, versionList);
        verify(documentVersionRepository).findById_DocumentId(documentId);
    }

    @Test
    void getViewableUrlVersion_DocumentExists_ReturnsViewableUrl() throws IOException {
        // Arrange
        UUID userId = UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe");
        String organizationName = "testorg";
        String expectedUrl = "https://cdn.example.com/signed-url";

        when(documentRepository.findByDocumentId(document.getDocumentId())).thenReturn(Optional.of(document));
        when(cdnService.getViewableUrl(document, userId, organizationName,3)).thenReturn(expectedUrl);

        // Act
        String actualUrl = documentService.getViewableUrl(document.getDocumentId(), userId, organizationName,3);

        // Assert
        assertEquals(expectedUrl, actualUrl);
        verify(documentRepository, times(1)).findByDocumentId(document.getDocumentId());
        verify(cdnService, times(1)).getViewableUrl(document, userId, organizationName,3);
    }

    @Test
    void getViewableUrlVersion_DocumentDoesNotExist_ThrowsIllegalArgumentException() throws IOException {
        // Arrange
        UUID userId = UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe");
        String organizationName = "testorg";

        when(documentRepository.findByDocumentId(document.getDocumentId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                documentService.getViewableUrl(documentId, userId, organizationName, 555));

        assertEquals("Document with id " + document.getDocumentId() + " does not exist", exception.getMessage());
        verify(documentRepository, times(1)).findByDocumentId(document.getDocumentId());
        verify(cdnService, never()).getViewableUrl(any(Document.class), any(UUID.class), anyString());
    }

    @Test
    void getViewableUrlVersion_CdnServiceThrowsIOException_ThrowsIOException() throws IOException {
        // Arrange
        UUID userId = UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe");
        String organizationName = "testorg";

        when(documentRepository.findByDocumentId(document.getDocumentId())).thenReturn(Optional.of(document));
        when(cdnService.getViewableUrl(document, userId, organizationName, 555)).thenThrow(new IOException("Failed to get URL"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () ->
                documentService.getViewableUrl(documentId, userId, organizationName, 555));

        assertEquals("Failed to get URL", exception.getMessage());
        verify(documentRepository, times(1)).findByDocumentId(document.getDocumentId());
        verify(cdnService, times(1)).getViewableUrl(document, userId, organizationName, 555);
    }

    @Test
    void editDocument_WhenDocumentNotFound_ThrowsIllegalStateException() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        EditRequest editRequest = new EditRequest();
        EditRequestKey key = new EditRequestKey();
        key.setUserId(userId);
        key.setDocumentId(documentId);
        editRequest.setId(key);
        editRequest.setDocumentId(documentId);
        editRequest.setUserId(userId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(editRequestRepository.existsById(editRequest.getId())).thenReturn(true);
        when(documentRepository.findByDocumentId(documentId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> documentService.editDocument(file, editRequest)
        );

        assertEquals("Document with ID " + documentId + " not found", exception.getMessage());
        verify(documentRepository).findByDocumentId(documentId);
        verify(editRequestRepository).existsById(editRequest.getId());
        verify(userRepository, never()).findById(any());
        verify(cdnService, never()).editDocument(any(), any(), any(), any());
        verify(editRequestRepository, never()).delete(any());
    }

    @Test
    void editDocument_WhenUserNotFound_ThrowsUserNotFoundException() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        EditRequest editRequest = new EditRequest();
        EditRequestKey key = new EditRequestKey();
        key.setUserId(userId);
        key.setDocumentId(documentId);
        editRequest.setId(key);
        editRequest.setDocumentId(documentId);
        editRequest.setUserId(userId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(editRequestRepository.existsById(editRequest.getId())).thenReturn(true);
        when(documentRepository.findByDocumentId(documentId)).thenReturn(Optional.of(document));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> documentService.editDocument(file, editRequest)
        );

        assertEquals("User with ID " + userId + " not found", exception.getMessage());
        verify(documentRepository).findByDocumentId(documentId);
        verify(editRequestRepository).existsById(editRequest.getId());
        verify(userRepository).findById(userId);
        verify(cdnService, never()).editDocument(any(), any(), any(), any());
        verify(editRequestRepository, never()).delete(any());
    }

}