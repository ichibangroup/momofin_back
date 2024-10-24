package ppl.momofin.momofinbackend.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentTest {
    @Test
    void testEmptyConstructor() {
        Document document = new Document();
        assertNotNull(document);
        assertNull(document.getDocumentId());
        assertNull(document.getHashString());
        assertNull(document.getName());
    }

    @Test
    void testConstructor() {
        String name = "test document name.pdf";
        String hashString = "valid hash string";
        Document document = new Document(hashString, name);
        assertNotNull(document);
        assertNull(document.getDocumentId());
        assertEquals(hashString, document.getHashString());
        assertEquals(name, document.getName());
    }

    @Test
    void testGetSetDocumentId() {
        UUID documentId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        Document document= new Document();
        document.setDocumentId(documentId);
        assertEquals(documentId, document.getDocumentId());
    }

    @Test
    void testGetSetName() {
        String name = "test document name.pdf";
        Document document = new Document();
        document.setName(name);
        assertEquals(name, document.getName());
    }

    @Test
    void testGetSetHashString() {
        String hashString = "valid hash string";
        Document document = new Document();
        document.setHashString(hashString);
        assertEquals(hashString, document.getHashString());
    }

    @Test
    void testGetSetOwner() {
        User user = new User();
        Document document = new Document();
        document.setOwner(user);
        assertEquals(user, document.getOwner());
    }
}
