package ppl.momofin.momofinbackend.response;

import lombok.Getter;
import lombok.Setter;
import ppl.momofin.momofinbackend.model.Organization;

@Getter @Setter
public class OrganizationResponse {
    private Long organizationId;
    private String name;
    private String description;

    public static OrganizationResponse fromOrganization(Organization organization) {
        OrganizationResponse response = new OrganizationResponse();
        response.setOrganizationId(organization.getOrganizationId());
        response.setName(organization.getName());
        response.setDescription(organization.getDescription());
        return response;
    }
}
