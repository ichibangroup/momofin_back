package ppl.momofin.momofinbackend.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ppl.momofin.momofinbackend.model.User;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FetchAllUserResponse {
    private UUID userId;
    private String username;
    private String name;
    private String email;
    private String organization;
    private boolean isMomofinAdmin;
    private boolean isOrganizationAdmin;

    public static FetchAllUserResponse fromUser(User user) {
        return new FetchAllUserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getOrganization().getName(),
                user.isMomofinAdmin(),
                user.isOrganizationAdmin()
        );
    }
}
