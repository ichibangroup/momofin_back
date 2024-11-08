package ppl.momofin.momofinbackend.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EditRequestTest {
    @Test
    void testEditRequestEmptyConstructor() {
        EditRequest request = new EditRequest();

        assertNotNull(request.getId());
        assertNull(request.getUser());
        assertNull(request.getDocument());
    }

    @Test
    void testEditRequestConstructor() {
        Document document = new Document();
        User user = new User();

        EditRequest request = new EditRequest(user,document);

        assertEquals(request.getUser(), user);
        assertEquals(request.getDocument(), document);
    }

    @Test
    void testGetSetId() {
        EditRequestKey key = new EditRequestKey();

        EditRequest request = new EditRequest();
        request.setId(key);

        assertEquals(request.getId(), key);
    }

    @Test
    void testGetSetUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);

        EditRequest request = new EditRequest();
        request.setUser(user);

        assertEquals(request.getUser(), user);
        assertEquals(userId, request.getId().getUserId());
    }

    @Test
    void testGetSetDocument() {
        UUID documentId = UUID.randomUUID();
        Document document = new Document();
        document.setDocumentId(documentId);

        EditRequest request = new EditRequest();
        request.setDocument(document);

        assertEquals(request.getDocument(), document);
        assertEquals(documentId, request.getId().getDocumentId());
    }

    @Test
    void testGetSetDocumentId() {
        UUID documentId = UUID.randomUUID();

        EditRequest request = new EditRequest();
        request.setDocumentId(documentId);

        assertEquals(request.getDocumentId(), documentId);
    }

    @Test
    void testGetSetUserId() {
        UUID userId = UUID.randomUUID();

        EditRequest request = new EditRequest();
        request.setDocumentId(userId);

        assertEquals(request.getDocumentId(), userId);
    }
}

