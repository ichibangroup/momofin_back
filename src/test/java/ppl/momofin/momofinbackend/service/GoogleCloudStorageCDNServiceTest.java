package ppl.momofin.momofinbackend.service;

import com.google.cloud.storage.Blob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.DocumentVersion;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.DocumentRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleCloudStorageCDNServiceTest {

    @Mock
    private Storage mockStorage;
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private Blob blob;

    private GoogleCloudStorageCDNService cdnService;
    private String bucketName;
    private String projectId;
    private String privateKeyId;
    private String privateKeyFilePath;
    private String serviceName;
    private String clientId;
    private User user;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() throws IOException {
        bucketName = System.getenv("GCP_BUCKET_NAME");
        projectId = System.getenv("GCP_PROJECT");
        privateKeyId = System.getenv("GCP_SA_PRIVATE_KEY_ID");
        privateKeyFilePath = System.getenv("PKEY_DIRECTORY") + "/gcp_sa_private_key.pem";
        serviceName = System.getenv("GCP_SA_NAME");
        clientId =System.getenv("GCP_SA_CLIENT_ID");

        Organization organization = new Organization("Momofin");
        user = new User(organization, "Executive John", "Marshall Jordan", "marshall@jordan.email.com", "1234567890999", "Executive");

        mockFile = new MockMultipartFile("test file", "test-file.pdf", MediaType.APPLICATION_PDF_VALUE,"test file content".getBytes());

        cdnService = new GoogleCloudStorageCDNService(bucketName, projectId, privateKeyId, privateKeyFilePath, serviceName, clientId, documentRepository) {
            @Override
            protected Storage createStorage(GoogleCredentials credentials) {
                return mockStorage;
            }
        };
    }

    @Test
    void testUploadFile() throws IOException {
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);

        cdnService.submitDocument(mockFile, user, "hashString");

        BlobId expectedBlobId = BlobId.of(bucketName, user.getOrganization().getName() + "/" + user.getUsername() + "/test-file/version_1_test-file.pdf");
        BlobInfo expectedBlobInfo = BlobInfo.newBuilder(expectedBlobId)
                .setContentType("application/pdf")
                .build();

        verify(documentRepository).save(documentCaptor.capture());

        Document savedDocument = documentCaptor.getValue();

        assertNotNull(savedDocument);
        assertEquals("test-file.pdf", savedDocument.getName());
        assertEquals("hashString", savedDocument.getHashString());
        assertEquals(user, savedDocument.getOwner());
        verify(mockStorage).create(expectedBlobInfo, mockFile.getBytes());
    }

    @Test
    void testConstructorWithInvalidCredentials() {
        String wrongFilePath = System.getenv("PKEY_DIRECTORY")+"/wrongkey.pem";
        assertThrows(IOException.class, () -> {
            new GoogleCloudStorageCDNService(bucketName, projectId, privateKeyId, wrongFilePath, serviceName, clientId, documentRepository);
        });
    }

    @Test
    void testGetViewableUrl_Success() throws IOException {
        // Arrange
        String fileName = "test.pdf";
        String username = "user123";
        String organizationName = "OrgXYZ";
        String folderPath = organizationName + "/" + username + "/" + fileName;
        BlobId blobId = BlobId.of(bucketName, folderPath);

        when(mockStorage.get(blobId)).thenReturn(blob);
        URI signedUri = URI.create("https://signed-url.com");
        URL signedUrl = signedUri.toURL();
        when(blob.signUrl(1, TimeUnit.HOURS)).thenReturn(signedUrl);

        // Act
        String viewableUrl = cdnService.getViewableUrl(fileName, username, organizationName);

        // Assert
        assertNotNull(viewableUrl);
        assertEquals("https://signed-url.com", viewableUrl);
        verify(mockStorage).get(blobId); // Ensure the correct blob was fetched
        verify(blob).signUrl(1, TimeUnit.HOURS); // Ensure a signed URL was created
    }

    @Test
    void testGetViewableUrl_FileNotFound() {
        // Arrange
        String fileName = "nonexistent.pdf";
        String username = "user123";
        String organizationName = "OrgXYZ";
        String folderPath = organizationName + "/" + username + "/" + fileName;
        BlobId blobId = BlobId.of(bucketName, folderPath);

        when(mockStorage.get(blobId)).thenReturn(null);

        // Act & Assert
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            cdnService.getViewableUrl(fileName, username, organizationName);
        });

        assertEquals("File not found: " + fileName, exception.getMessage());
        verify(mockStorage).get(blobId); // Ensure the correct blob was attempted to be fetched
    }

    @Test
    public void testEditDocument_NewVersionUploaded() throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(mockFile.getOriginalFilename()));
        String cleanedFileName =  fileName.replaceFirst("[.][^.]+$", "");


        // Expected values
        String folderName = user.getOrganization().getName() + "/" + user.getUsername();
        Document document = new Document();
        document.setOwner(user);
        document.setName("test-file");
        int nextVersion = document.getVersions().size() + 1;
        String expectedVersionedFileName = String.format("%s/%s/version_%d_%s", folderName, cleanedFileName, nextVersion, fileName);

        BlobId expectedBlobId = BlobId.of(bucketName, expectedVersionedFileName);
        BlobInfo expectedBlobInfo = BlobInfo.newBuilder(expectedBlobId).setContentType("application/pdf").build();

        // Mock storage interactions
        when(documentRepository.save(any(Document.class))).thenReturn(document);

        // Execute the method
        Document updatedDocument = cdnService.editDocument(mockFile, document, "newHash");

        // Verify interactions and results
        verify(mockStorage).create(expectedBlobInfo, mockFile.getBytes());
        verify(documentRepository).save(document);

        assertEquals("newHash", updatedDocument.getHashString());
        assertEquals(nextVersion, updatedDocument.getVersions().size());
        assertEquals(nextVersion, updatedDocument.getCurrentVersion().getVersion());
        assertEquals(cleanedFileName, updatedDocument.getCurrentVersion().getFileName());
    }

    @Test
    public void testEditDocument_UpdatesDocumentAndVersionList() throws IOException {
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        DocumentVersion existingVersion = new DocumentVersion();
        existingVersion.setVersion(1);
        Document document = new Document();
        document.setOwner(user);
        document.setName("test-file");
        document.getVersions().add(existingVersion);

        // Execute the method
        cdnService.editDocument(mockFile, document, "updatedHash");

        verify(documentRepository).save(documentCaptor.capture());

        Document updatedDocument = documentCaptor.getValue();

        // Check that the new version is added and that the hash is updated
        assertEquals("updatedHash", updatedDocument.getHashString());
        assertEquals(2, updatedDocument.getVersions().size());
        assertEquals(2, updatedDocument.getCurrentVersion().getVersion());
        assertEquals("test-file", updatedDocument.getCurrentVersion().getFileName());
    }
}