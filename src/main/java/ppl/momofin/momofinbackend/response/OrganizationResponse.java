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
    private String industry;
    private String location;
    private String errorMessage;

    public static OrganizationResponse fromOrganization(Organization organization) {
        return new OrganizationResponse(
                organization.getOrganizationId(),
                organization.getName(),
                organization.getDescription(),
                organization.getIndustry(),
                organization.getLocation(),
                null
        );
    }

    public OrganizationResponse(Long organizationId, String errorMessage, String description) {
        this.organizationId = organizationId;
        this.errorMessage = errorMessage;
        this.description = description;
    }
}
