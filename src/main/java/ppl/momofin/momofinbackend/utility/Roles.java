package ppl.momofin.momofinbackend.utility;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Roles {
    private boolean isOrganizationalAdmin;
    private boolean isMomofinAdmin;

    Roles() {}

    public Roles(boolean isOrganizationalAdmin, boolean isMomofinAdmin) {
        this.isOrganizationalAdmin = isOrganizationalAdmin;
        this.isMomofinAdmin = isMomofinAdmin;
    }
}
