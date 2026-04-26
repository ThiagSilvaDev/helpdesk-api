package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.UserDTO;
import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class UserControllerTest {

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        mockMvc = standaloneSetup(new UserController(userService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldCreateUserAndReturnLocationHeader() throws Exception {
        UserDTO.Create.Request request = new UserDTO.Create.Request(
                "Jane User",
                "jane@helpdesk.local",
                "StrongPass@123",
                Roles.ROLE_USER
        );
        given(userService.create(any(UserDTO.Create.Request.class)))
                .willReturn(new UserDTO.Response(42L, request.name(), request.email(), request.role(), true));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Jane User",
                                  "email": "jane@helpdesk.local",
                                  "password": "StrongPass@123",
                                  "role": "ROLE_USER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/users/42")))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.email").value("jane@helpdesk.local"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldGetUserById() throws Exception {
        given(userService.getUserResponseById(7L))
                .willReturn(new UserDTO.Response(7L, "Tech User", "tech@helpdesk.local", Roles.ROLE_TECHNICIAN, true));

        mockMvc.perform(get("/api/users/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.role").value("ROLE_TECHNICIAN"));
    }

    @Test
    void shouldListUsersWithPageable() throws Exception {
        given(userService.findAll(PageRequest.of(1, 2)))
                .willReturn(new PageImpl<>(
                        List.of(new UserDTO.Response(1L, "Admin", "admin@helpdesk.local", Roles.ROLE_ADMIN, true)),
                        PageRequest.of(1, 2),
                        3
                ));

        mockMvc.perform(get("/api/users").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("admin@helpdesk.local"))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void shouldUpdateUserName() throws Exception {
        given(userService.update(any(Long.class), any(UserDTO.Update.Request.class)))
                .willReturn(new UserDTO.Response(5L, "New Name", "user@helpdesk.local", Roles.ROLE_USER, true));

        mockMvc.perform(patch("/api/users/5/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));

        then(userService).should().update(any(Long.class), any(UserDTO.Update.Request.class));
    }

    @Test
    void shouldChangeUserRole() throws Exception {
        given(userService.changeRole(any(Long.class), any(UserDTO.ChangeRole.Request.class)))
                .willReturn(new UserDTO.Response(5L, "Jane User", "jane@helpdesk.local", Roles.ROLE_ADMIN, true));

        mockMvc.perform(patch("/api/users/5/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void shouldDeactivateUser() throws Exception {
        mockMvc.perform(delete("/api/users/5"))
                .andExpect(status().isNoContent());

        then(userService).should().deactivate(5L);
    }
}
