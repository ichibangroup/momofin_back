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
    private String errorMessage;

    public static OrganizationResponse fromOrganization(Organization organization) {
        return new OrganizationResponse(
                organization.getOrganizationId(),
                organization.getName(),
                organization.getDescription(),
                null
        );
    }

    public OrganizationResponse(Long organizationId, String errorMessage, String description) {
        this.organizationId = organizationId;
        this.errorMessage = errorMessage;
        this.description = description;
    }
}
