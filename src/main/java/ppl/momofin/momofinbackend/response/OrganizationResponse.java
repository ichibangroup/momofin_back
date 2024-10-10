package ppl.momofin.momofinbackend.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ppl.momofin.momofinbackend.model.Organization;

@Getter @Setter @AllArgsConstructor
public class OrganizationResponse {
    private Long organizationId;
    private String name;
    private String description;

    public static OrganizationResponse fromOrganization(Organization organization) {
        return new OrganizationResponse(
                organization.getOrganizationId(),
                organization.getName(),
                organization.getDescription()
        );
    }
}
