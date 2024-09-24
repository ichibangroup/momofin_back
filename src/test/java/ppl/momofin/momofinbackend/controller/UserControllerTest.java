package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.UserService;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(LoginController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testLoginSuccess() throws Exception {
        String identifier = "test@email.com";
        String password = "password";

        User mockUser = new User("testCompany", identifier, password);
        when(userService.findByEmailOrCompanyName(identifier)).thenReturn(Optional.of(mockUser));
        when(userService.validatePassword(mockUser, password)).thenReturn(true);

        mockMvc.perform(post("/login")
                        .param("identifier", identifier)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/")); // Expecting a redirect to home page
    }

    @Test
    public void testLoginFailure() throws Exception {
        String identifier = "wrong@email.com";
        String password = "wrongpassword";

        when(userService.findByEmailOrCompanyName(identifier)).thenReturn(Optional.empty());

        mockMvc.perform(post("/login")
                        .param("identifier", identifier)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attributeExists("error"))
                .andExpect(MockMvcResultMatchers.model().attribute("error", "Invalid Credentials"))
                .andExpect(MockMvcResultMatchers.view().name("login"));
    }
}
