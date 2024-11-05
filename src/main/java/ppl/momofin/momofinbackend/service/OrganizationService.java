package ppl.momofin.momofinbackend.service;

import ppl.momofin.momofinbackend.dto.UserDTO;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;

import java.util.List;
import java.util.UUID;

public interface OrganizationService {
    Organization updateOrganization(UUID orgId, String name, String description, String industry, String location);
    List<UserDTO> getUsersInOrganization(UUID orgId);
    UserDTO updateUserInOrganization(UUID orgId, UUID userId, UserDTO updatedUserDTO);
    Organization findOrganizationById(UUID orgId);
    List<Organization> getAllOrganizations();
    Organization createOrganization(String name, String description, String industry, String location);
    void deleteUser(UUID orgId, UUID userId, User requestingUser);
}