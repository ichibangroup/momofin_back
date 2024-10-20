package ppl.momofin.momofinbackend.request;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddOrganizationRequest {
    private String name;
    private String description;
    private String industry;
    private String location;
    private String adminUsername;
    private String adminPassword;
}
