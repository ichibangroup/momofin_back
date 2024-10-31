package ppl.momofin.momofinbackend.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
        User user = new User();

        EditRequest request = new EditRequest();
        request.setUser(user);

        assertEquals(request.getUser(), user);
        assertEquals(request.getUser(), request.getId().getUser());
    }

    @Test
    void testGetSetDocument() {
        Document document = new Document();

        EditRequest request = new EditRequest();
        request.setDocument(document);

        assertEquals(request.getDocument(), document);
        assertEquals(request.getDocument(), request.getId().getDocument());
    }
}

