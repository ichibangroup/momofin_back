package ppl.momofin.momofinbackend.model;

import model.Organization;
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
}
