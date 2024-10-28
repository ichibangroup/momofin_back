package ppl.momofin.momofinbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ppl.momofin.momofinbackend.model.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String username;
    private String name;
    private String email;
    private String position;
    private boolean isOrganizationAdmin;
    private boolean isMomofinAdmin;  // New field added

    public static UserDTO fromUser(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPosition(),
                user.isOrganizationAdmin(),
                user.isMomofinAdmin()  // Map the new field
        );
    }

    public User toUser() {
        User user = new User();
        user.setUserId(this.userId);
        user.setUsername(this.username);
        user.setName(this.name);
        user.setEmail(this.email);
        user.setPosition(this.position);
        user.setOrganizationAdmin(this.isOrganizationAdmin);
        user.setMomofinAdmin(this.isMomofinAdmin);  // Set the new field
        return user;
    }
}
