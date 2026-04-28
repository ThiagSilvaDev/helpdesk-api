package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.user.ChangeUserRoleRequest;
import com.thiagsilvadev.helpdesk.dto.user.CreateUserRequest;
import com.thiagsilvadev.helpdesk.dto.user.UpdateUserNameRequest;
import com.thiagsilvadev.helpdesk.dto.user.UserResponse;
import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.entity.User;
import com.thiagsilvadev.helpdesk.exception.EmailAlreadyExistsException;
import com.thiagsilvadev.helpdesk.exception.NotFoundException;
import com.thiagsilvadev.helpdesk.mapper.UserMapper;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long MISSING_USER_ID = 99L;
    private static final String RAW_PASSWORD = "StrongPass@123";
    private static final String ENCODED_PASSWORD = "encoded-password";

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Nested
    class Create {

        @Test
        void shouldCreateUserWithEncodedPassword() {
            CreateUserRequest request = createUserRequest();

            given(userRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn(ENCODED_PASSWORD);
            given(userRepository.save(any(User.class))).willAnswer(invocation -> persist(invocation.getArgument(0), USER_ID));

            UserResponse response = userService.create(request);

            assertThat(response)
                    .returns(USER_ID, UserResponse::id)
                    .returns(request.name(), UserResponse::name)
                    .returns(request.email(), UserResponse::email)
                    .returns(request.role(), UserResponse::role)
                    .returns(true, UserResponse::active);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            then(userRepository).should().save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser)
                    .returns(request.name(), User::getName)
                    .returns(request.email(), User::getEmail)
                    .returns(ENCODED_PASSWORD, User::getPassword)
                    .returns(request.role(), User::getRole)
                    .returns(true, User::isActive);
        }

        @Test
        void shouldThrowWhenEmailAlreadyExists() {
            CreateUserRequest request = createUserRequest();
            given(userRepository.existsByEmail(request.email())).willReturn(true);

            assertThatExceptionOfType(EmailAlreadyExistsException.class)
                    .isThrownBy(() -> userService.create(request))
                    .withMessage("Email already exists: " + request.email());

            then(userRepository).should().existsByEmail(request.email());
            then(userRepository).should(never()).save(any());
            verifyNoInteractions(passwordEncoder);
        }
    }

    @Nested
    class GetUserById {

        @Test
        void shouldReturnUserWhenFound() {
            User user = persistedUser(USER_ID, "Jane User", "jane@helpdesk.local", Roles.ROLE_USER);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

            User found = userService.getUserById(USER_ID);

            assertThat(found).isSameAs(user);
        }

        @Test
        void shouldThrowWhenUserDoesNotExist() {
            given(userRepository.findById(MISSING_USER_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> userService.getUserById(MISSING_USER_ID))
                    .withMessage("User not found with id: " + MISSING_USER_ID);
        }
    }

    @Nested
    class GetUserResponseById {

        @Test
        void shouldReturnMappedUserResponse() {
            User user = persistedUser(USER_ID, "Jane User", "jane@helpdesk.local", Roles.ROLE_USER);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

            UserResponse response = userService.getUserResponseById(USER_ID);

            assertThat(response)
                    .returns(USER_ID, UserResponse::id)
                    .returns(user.getName(), UserResponse::name)
                    .returns(user.getEmail(), UserResponse::email)
                    .returns(user.getRole(), UserResponse::role)
                    .returns(true, UserResponse::active);
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldReturnMappedPage() {
            Pageable pageable = PageRequest.of(0, 20);
            User first = persistedUser(1L, "Jane User", "jane@helpdesk.local", Roles.ROLE_USER);
            User second = persistedUser(2L, "Tech User", "tech@helpdesk.local", Roles.ROLE_TECHNICIAN);
            Page<User> users = new PageImpl<>(List.of(first, second), pageable, 2);

            given(userRepository.findAll(pageable)).willReturn(users);

            Page<UserResponse> response = userService.findAll(pageable);

            assertThat(response.getTotalElements()).isEqualTo(2);
            assertThat(response.getContent())
                    .extracting(UserResponse::email)
                    .containsExactly("jane@helpdesk.local", "tech@helpdesk.local");
        }
    }

    @Nested
    class Update {

        @Test
        void shouldRenameUserWithoutChangingEmailOrRole() {
            User existingUser = persistedUser(USER_ID, "Old Name", "old@helpdesk.local", Roles.ROLE_USER);
            UpdateUserNameRequest request = new UpdateUserNameRequest("New Name");

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(existingUser));
            given(userRepository.save(existingUser)).willReturn(existingUser);

            UserResponse response = userService.update(USER_ID, request);

            assertThat(response)
                    .returns("New Name", UserResponse::name)
                    .returns("old@helpdesk.local", UserResponse::email)
                    .returns(Roles.ROLE_USER, UserResponse::role);
            then(userRepository).should().save(existingUser);
        }

        @Test
        void shouldThrowWhenUserDoesNotExist() {
            UpdateUserNameRequest request = new UpdateUserNameRequest("New Name");
            given(userRepository.findById(MISSING_USER_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> userService.update(MISSING_USER_ID, request))
                    .withMessage("User not found with id: " + MISSING_USER_ID);

            then(userRepository).should(never()).save(any());
        }
    }

    @Nested
    class ChangeRole {

        @Test
        void shouldChangeUserRoleWithoutChangingIdentity() {
            User existingUser = persistedUser(USER_ID, "Jane User", "jane@helpdesk.local", Roles.ROLE_USER);
            ChangeUserRoleRequest request = new ChangeUserRoleRequest(Roles.ROLE_TECHNICIAN);

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(existingUser));
            given(userRepository.save(existingUser)).willReturn(existingUser);

            UserResponse response = userService.changeRole(USER_ID, request);

            assertThat(response)
                    .returns("Jane User", UserResponse::name)
                    .returns("jane@helpdesk.local", UserResponse::email)
                    .returns(Roles.ROLE_TECHNICIAN, UserResponse::role);
            then(userRepository).should().save(existingUser);
        }

        @Test
        void shouldThrowWhenUserDoesNotExist() {
            ChangeUserRoleRequest request = new ChangeUserRoleRequest(Roles.ROLE_ADMIN);
            given(userRepository.findById(MISSING_USER_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> userService.changeRole(MISSING_USER_ID, request))
                    .withMessage("User not found with id: " + MISSING_USER_ID);

            then(userRepository).should(never()).save(any());
        }
    }

    @Nested
    class Deactivate {

        @Test
        void shouldDeactivateUser() {
            User existingUser = persistedUser(USER_ID, "Jane User", "jane@helpdesk.local", Roles.ROLE_USER);

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(existingUser));
            given(userRepository.save(existingUser)).willReturn(existingUser);

            userService.deactivate(USER_ID);

            assertThat(existingUser)
                    .returns(false, User::isActive);
            then(userRepository).should().save(existingUser);
        }

        @Test
        void shouldThrowWhenUserDoesNotExist() {
            given(userRepository.findById(MISSING_USER_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> userService.deactivate(MISSING_USER_ID))
                    .withMessage("User not found with id: " + MISSING_USER_ID);

            then(userRepository).should(never()).save(any());
        }
    }

    private CreateUserRequest createUserRequest() {
        return new CreateUserRequest(
                "Jane User",
                "jane@helpdesk.local",
                RAW_PASSWORD,
                Roles.ROLE_USER
        );
    }

    private User persistedUser(Long id, String name, String email, Roles role) {
        User user = new User(name, email, ENCODED_PASSWORD, role);
        return persist(user, id);
    }

    private User persist(User user, Long id) {
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
