package ppl.momofin.momofinbackend.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ppl.momofin.momofinbackend.model.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FetchAllUserResponse {
    private Long userId;
    private String username;
    private String name;
    private String email;
    private String organization;

    public static FetchAllUserResponse fromUser(User user) {
        return new FetchAllUserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getOrganization().getName()
        );
    }
}
