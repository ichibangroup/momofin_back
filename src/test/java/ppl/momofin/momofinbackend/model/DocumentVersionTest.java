package ppl.momofin.momofinbackend.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentVersionTest {
    @Test
    void testEditRequestEmptyConstructor() {
        DocumentVersion documentVersion = new DocumentVersion();

        assertNotNull(documentVersion.getId());
        assertNull(documentVersion.getDocument());
        assertEquals(1,documentVersion.getVersion());
        assertNull(documentVersion.getFileName());
        assertNull(documentVersion.getHashString());
        assertNotNull(documentVersion.getCreatedDate());
    }

    @Test
    void testEditRequestConstructor() {
        Document document = new Document();
        int version = 4;
        String filename = "test-file";
        String hash = "hash";


        DocumentVersion documentVersion = new DocumentVersion(version,document, filename, hash);

        assertNotNull(documentVersion.getId());
        assertEquals(document,documentVersion.getDocument());
        assertEquals(version,documentVersion.getVersion());
        assertEquals(filename,documentVersion.getFileName());
        assertEquals(hash, documentVersion.getHashString());
        assertNotNull(documentVersion.getCreatedDate());
    }

    @Test
    void testGetSetId() {
        DocumentVersionKey key = new DocumentVersionKey();

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setId(key);

        assertEquals(documentVersion.getId(), key);
    }

    @Test
    void testGetSetUser() {
        int version = 4;

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setVersion(version);

        assertEquals(documentVersion.getVersion(), version);
        assertEquals(documentVersion.getId().getVersion(), documentVersion.getVersion());
    }

    @Test
    void testGetSetDocument() {
        Document document = new Document();

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setDocument(document);

        assertEquals(documentVersion.getDocument(), document);
        assertEquals(documentVersion.getDocument(), documentVersion.getId().getDocument());
    }

    @Test
    void testGetSetFilename() {
        String fileName = "test-file";

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setFileName(fileName);

        assertEquals(documentVersion.getFileName(), fileName);
    }

    @Test
    void testGetSetHash() {
        String hash = "hash";

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setHashString(hash);

        assertEquals(documentVersion.getHashString(), hash);
    }

    @Test
    void testGetSetCreatedDate() {
        LocalDateTime createdDate = LocalDateTime.now();

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setCreatedDate(createdDate);

        assertEquals(documentVersion.getCreatedDate(), createdDate);
    }

    @Test
    void  testDocumentVersionKeyConstructor() {
        Document document = new Document();
        int version = 4;
        DocumentVersionKey key = new DocumentVersionKey(document, version);

        assertEquals(key.getDocument(), document);
        assertEquals(key.getVersion(), version);
    }
}
