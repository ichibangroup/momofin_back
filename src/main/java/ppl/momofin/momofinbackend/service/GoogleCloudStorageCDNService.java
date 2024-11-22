package ppl.momofin.momofinbackend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.DocumentVersion;
import ppl.momofin.momofinbackend.model.DocumentVersionKey;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.DocumentRepository;
import ppl.momofin.momofinbackend.repository.DocumentVersionRepository;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class GoogleCloudStorageCDNService implements CDNService {

    private final Storage storage;
    private final String bucketName;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;

    @Autowired
    public GoogleCloudStorageCDNService(@Value("${gcp.bucket.name}") String bucketName,
                                        @Value("${gcp.project-id}") String projectId,
                                        @Value("${gcp.sa.private-key-id}") String privateKeyId,
                                        @Value("${gcp.sa.private-key-file}") String privateKeyFilePath,
                                        @Value("${gcp.sa.name}") String serviceName,
                                        @Value("${gcp.sa.client-id}") String clientId,
                                        DocumentRepository documentRepository,
                                        DocumentVersionRepository documentVersionRepository) throws IOException {
        String privateKey = new String(Files.readAllBytes(Paths.get(privateKeyFilePath)));
        this.bucketName = bucketName;

        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;

        String serviceAccountJson = String.format(
                """
                        {
                          "type": "service_account",
                          "project_id": "%s",
                          "private_key_id": "%s",
                          "private_key": "%s",
                          "client_email": "%s@%s.iam.gserviceaccount.com",
                          "client_id": "%s",
                          "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                          "token_uri": "https://oauth2.googleapis.com/token",
                          "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
                          "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/%s@%s.iam.gserviceaccount.com"
                        }""",
                projectId, privateKeyId, privateKey, serviceName, projectId, clientId, serviceName, projectId
        );
        try (ByteArrayInputStream serviceAccountStream = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8))) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);

            // Create a Google Cloud Storage service instance
            this.storage = createStorage(credentials);
        } catch (IOException e) {
            throw new IOException("Failed to initialize Google Cloud Storage credentials:", e);
        }
    }

    protected Storage createStorage(GoogleCredentials credentials) {
        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }

    @Override
    public Document submitDocument(MultipartFile file, User user, String hashString) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String cleanedFileName =  cleanFileName(fileName);
        String folderName = user.getOrganization().getName() + "/" + user.getUserId();

        // Define file path for version 1
        String versionedFileName = String.format("%s/%s/version_1_%s", folderName, cleanedFileName, fileName);
        BlobId blobId = BlobId.of(bucketName, versionedFileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("application/pdf")
                .build();
        storage.create(blobInfo, file.getBytes());

        // Create Document entity and its first version
        Document document = new Document();
        document.setDocumentId(UUID.randomUUID());
        document.setName(fileName);
        document.setHashString(hashString);
        document.setOwner(user);
        document.setCurrentVersion(1);

        Document savedDocument = documentRepository.save(document);

        DocumentVersion version = new DocumentVersion();
        version.setFileName(fileName);
        version.setHashString(hashString);
        version.setEditedBy(user);

        version.setVersion(1);
        version.setDocumentId(savedDocument.getDocumentId());
        documentVersionRepository.save(version);

        return savedDocument;
    }


    @Override
    public Document editDocument(MultipartFile file, Document document, String hashString, User editor) throws IOException {
        String fileName = document.getName();
        String cleanedFileName =  cleanFileName(fileName);

        String folderName = document.getOwner().getOrganization().getName() + "/" + document.getOwner().getUserId();

        // Calculate the next version number
        int nextVersion = document.getCurrentVersion() +1;
        String versionedFileName = String.format("%s/%s/version_%d_%s", folderName, cleanedFileName, nextVersion, fileName);

        // Upload new version to GCS
        BlobId blobId = BlobId.of(bucketName, versionedFileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("application/pdf")
                .build();
        storage.create(blobInfo, file.getBytes());

        DocumentVersion newVersion = new DocumentVersion();
        newVersion.setDocumentId(document.getDocumentId());
        newVersion.setVersion(nextVersion);
        newVersion.setFileName(document.getName());
        newVersion.setHashString(hashString);
        newVersion.setEditedBy(editor);

        document.setHashString(hashString);
        document.setCurrentVersion(nextVersion);
        document.setBeingRequested(false);

        documentVersionRepository.save(newVersion);

        return documentRepository.save(document);
    }


    @Override
    public String getViewableUrl(Document document, UUID userId, String organizationName) throws IOException {
        return getViewableUrl(document, userId, organizationName, document.getCurrentVersion());
    }

    @Override
    public String getViewableUrl(Document document, UUID userId, String organizationName, int version) throws IOException {
        String fileName = document.getName();
        String cleanedFileName =  cleanFileName(fileName);

        String versionedFileName = "version_" + version + "_" + fileName;
        String folderName = organizationName + "/" + userId + "/" + cleanedFileName;
        BlobId blobId = BlobId.of(bucketName, folderName + "/" + versionedFileName);
        Blob blob = storage.get(blobId);

        if (blob == null) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        // Generate a signed URL that expires in 1 hour
        URL signedUrl = blob.signUrl(15, TimeUnit.MINUTES);
        return signedUrl.toString();
    }

    private String cleanFileName(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }
}
