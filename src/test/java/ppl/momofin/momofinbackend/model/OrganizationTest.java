package ppl.momofin.momofinbackend.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrganizationTest {
    @Test
    void testEmptyConstructor() {
        Organization organization = new Organization();
        assertNotNull(organization);
        assertNull(organization.getOrganizationId());
        assertNull(organization.getName());
    }

    @Test
    void testConstructor() {
        String name = "test organization name";
        Organization organization = new Organization(name);
        assertNotNull(organization);
        assertNull(organization.getOrganizationId());
        assertEquals(name, organization.getName());
    }

    @Test
    void testGetSetOrganizationId() {
        Long organizationId = 1L;
        Organization organization = new Organization();
        organization.setOrganizationId(organizationId);
        assertEquals(organizationId, organization.getOrganizationId());
    }

    @Test
    void testGetSetName() {
        String name = "test organization name";
        Organization organization = new Organization();
        organization.setName(name);
        assertEquals(name, organization.getName());
    }
    @Test
    void testOrganizationCreation() {
        Organization org = new Organization("Test Org");
        assertEquals("Test Org", org.getName());
        assertEquals("Default organization description", org.getDescription());
    }

    @Test
    void testOrganizationCreationWithDescription() {
        Organization org = new Organization("Test Org", "Custom Description");
        assertEquals("Test Org", org.getName());
        assertEquals("Custom Description", org.getDescription());
    }
}
