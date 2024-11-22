package ppl.momofin.momofinbackend.dto;

import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.model.EditRequest;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.model.Organization;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EditRequestDTOTest {

    @Test
    void testConstructorWithAllFields() {
        UUID documentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String organizationName = "Test Organization";
        String username = "Test User";
        String position = "Developer";
        String email = "test@example.com";
        String documentName = "Test Document";

        EditRequestDTO dto = new EditRequestDTO(documentId, userId, organizationName, username, position, email, documentName);

        assertEquals(documentId.toString(), dto.getDocumentId());
        assertEquals(userId.toString(), dto.getUserId());
        assertEquals(organizationName, dto.getOrganizationName());
        assertEquals(username, dto.getUsername());
        assertEquals(position, dto.getPosition());
        assertEquals(email, dto.getEmail());
        assertEquals(documentName, dto.getDocumentName());
    }

    @Test
    void testGettersAndSetters() {
        EditRequestDTO dto = new EditRequestDTO();

        dto.setDocumentId("doc-id");
        dto.setUserId("user-id");
        dto.setOrganizationName("Test Organization");
        dto.setUsername("Test User");
        dto.setPosition("Developer");
        dto.setEmail("test@example.com");
        dto.setDocumentName("Test Document");

        assertEquals("doc-id", dto.getDocumentId());
        assertEquals("user-id", dto.getUserId());
        assertEquals("Test Organization", dto.getOrganizationName());
        assertEquals("Test User", dto.getUsername());
        assertEquals("Developer", dto.getPosition());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("Test Document", dto.getDocumentName());
    }

    @Test
    void testToDTO() {
        UUID documentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String organizationName = "Test Organization";
        String username = "Test User";
        String position = "Developer";
        String email = "test@example.com";
        String documentName = "Test Document";

        Organization organization = new Organization();
        organization.setName(organizationName);

        User user = new User();
        user.setUsername(username);
        user.setPosition(position);
        user.setEmail(email);
        user.setOrganization(organization);

        Document document = new Document();
        document.setDocumentId(documentId);
        document.setName(documentName);
        document.setOwner(user);

        EditRequest editRequest = new EditRequest();
        editRequest.setDocument(document);
        editRequest.setUserId(userId);

        EditRequestDTO dto = EditRequestDTO.toDTO(editRequest);

        assertEquals(documentId.toString(), dto.getDocumentId());
        assertEquals(userId.toString(), dto.getUserId());
        assertEquals(organizationName, dto.getOrganizationName());
        assertEquals(username, dto.getUsername());
        assertEquals(position, dto.getPosition());
        assertEquals(email, dto.getEmail());
        assertEquals(documentName, dto.getDocumentName());
    }
}

