package ppl.momofin.momofinbackend.model;

import model.Organization;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrganizationTest {
    @Test
    public void testEmptyConstructor() {
        Organization organization = new Organization();
        assertNotNull(organization);
        assertNull(organization.getOrganizationId);
        assertNull(organization.getName());
    }

    @Test
    public void testConstructor() {
        String name = "test organization name";
        Organization organization = new Organization(name);
        assertNotNull(organization);
        assertNull(organization.getOrganizationId);
        assertEquals(name, organization.getName());
    }

    @Test
    public void testGetSetOrganizationId() {
        Long organizationId = 1L;
        Organization organization = new Organization();
        organization.setOrganizationId(organizationId);
        assertEquals(organizationId, organization.getOrganizationId());
    }

    @Test
    public void testGetSetName() {
        String name = "test organization name";
        Organization organization = new Organization();
        organization.setName(name);
        assertEquals(name, organization.getName());
    }
}
