package org.comicVaultBackend.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.comicVaultBackend.config.ApiConfig;
import org.comicVaultBackend.config.JwtUtil;
import org.comicVaultBackend.domain.dto.AuthenticationResponseDTO;
import org.comicVaultBackend.domain.dto.AuthentificationRequestDTO;
import org.comicVaultBackend.domain.dto.RegisterRequestDTO;
import org.comicVaultBackend.domain.dto.UserDTO;
import org.comicVaultBackend.domain.entities.RefreshTokenEntity;
import org.comicVaultBackend.domain.entities.UserEntity;
import org.comicVaultBackend.domain.regular.Role;
import org.comicVaultBackend.domain.regular.UserInfoResponse;
import org.comicVaultBackend.exceptions.AtLeastOneAdminException;
import org.comicVaultBackend.exceptions.EntityAlreadyExistringException;
import org.comicVaultBackend.exceptions.EntityNotFoundException;
import org.comicVaultBackend.exceptions.ForbiddenAuthException;
import org.comicVaultBackend.mappers.Mapper;
import org.comicVaultBackend.services.ProgressService;
import org.comicVaultBackend.services.UserService;
import org.comicVaultBackend.services.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.version}/users")
public class UserController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private Mapper<UserEntity, UserDTO> userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${api.version}")
    private String apiVersion;

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping("/session")
    public UserInfoResponse session() throws ForbiddenAuthException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserEntity> userEntity = userService.findByUsername(username);
        if (userEntity.isEmpty()) {
            throw new ForbiddenAuthException();
        }
        UserEntity user = userEntity.get();
        Role role = user.getRole();
        return new UserInfoResponse(username, role, user.getColor());
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthentificationRequestDTO authenticationRequest, HttpServletRequest request, HttpServletResponse response) throws UsernameNotFoundException, EntityNotFoundException, BadRequestException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final Optional<UserEntity> optionalUserEntity = userService.findByUsername(authenticationRequest.getUsername());
        if (optionalUserEntity.isEmpty()) {
            throw new EntityNotFoundException(authenticationRequest.getUsername(), EntityNotFoundException.Entity.USER);
        }
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }

        final Role role = optionalUserEntity.get().getRole();
        final String accessToken = jwtUtil.generateAccessToken(userDetails);
        final String refreshToken = jwtUtil.generateRefreshToken(userDetails, authenticationRequest, clientIp);
        final String color = optionalUserEntity.get().getColor();

        ResponseCookie accessCookie = createCookie(request, "ACCESS_TOKEN", accessToken, jwtUtil.getAccessTtlMs(), apiVersion, true);
        ResponseCookie refreshCookie = createCookie(request, "REFRESH_TOKEN", refreshToken, jwtUtil.getRefreshTtlMs(), apiVersion + "/users/refresh", true);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        AuthenticationResponseDTO authenticationResponseDTO = AuthenticationResponseDTO.builder().role(role).username(authenticationRequest.getUsername()).color(color).build();

        // Add cookies to response headers
        return ResponseEntity.ok()
                .headers(headers)
                .body(authenticationResponseDTO);
    }

    private ResponseCookie createCookie(
            HttpServletRequest request,
            String name,
            String value,
            long maxAge,
            String path,
            boolean httpOnly
    ) {
        boolean secure = request.isSecure();
        String sameSite = secure ? "Strict" : "Lax";

        return ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(secure)
                .path(path)
                .maxAge(Duration.ofMillis(maxAge))
                .sameSite(sameSite)
                .build();
    }

    // This method does not return anything because the result is not relevant
    @GetMapping("/forgetMe")
    public void forgetMe(HttpServletRequest request) throws EntityNotFoundException {
        String refreshToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(c -> "REFRESH_TOKEN".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new EntityNotFoundException("Refresh token not found", EntityNotFoundException.Entity.REFRESH_TOKEN));

        jwtUtil.deleteTokenTokenIfExisting(refreshToken);
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) throws EntityNotFoundException {
        // 1️⃣ Get refresh token from cookie
        String refreshToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(c -> "REFRESH_TOKEN".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new EntityNotFoundException("Refresh token not found", EntityNotFoundException.Entity.REFRESH_TOKEN));

        // 2️⃣ Validate refresh token exists in DB
        RefreshTokenEntity tokenEntity = jwtUtil.getValidRefreshToken(refreshToken);

        // 3️⃣ Load user
        UserDetails userDetails = userDetailsService.loadUserByUsername(tokenEntity.getUsername());

        // 4️⃣ Generate new tokens
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtil.renewRefreshToken(tokenEntity);

        // 6️⃣ Create cookies dynamically
        ResponseCookie accessCookie = createCookie(request, "ACCESS_TOKEN", newAccessToken, jwtUtil.getAccessTtlMs(), apiVersion, true);
        ResponseCookie refreshCookie = createCookie(request, "REFRESH_TOKEN", newRefreshToken, jwtUtil.getRefreshTtlMs(), apiVersion + "/users/refresh", true);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 7️⃣ Return cookies in response headers
        return ResponseEntity.ok()
                .headers(headers)
                .body(Map.of("success", true));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/register")
    public void registerUser(@RequestBody RegisterRequestDTO registerRequest) throws EntityAlreadyExistringException {
        // Check if the username already exists
        if (userService.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new EntityAlreadyExistringException(registerRequest.getUsername(), EntityNotFoundException.Entity.USER);
        }

        // Encode the password and create the user
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        UserDTO userDTO = UserDTO.builder()
                .password(encodedPassword)
                .username(registerRequest.getUsername())
                .role(registerRequest.getRole())
                .color(registerRequest.getColor())
                .build();
        UserEntity userEntity = userMapper.mapFrom(userDTO);
        userService.createUser(userEntity);

    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping(path = "{username}")
    public void deleteUser(@PathVariable String username) throws EntityNotFoundException, AtLeastOneAdminException {
        Optional<UserEntity> userEntityOptional = userService.findByUsername(username);

        if (userEntityOptional.isEmpty()) {
            throw new EntityNotFoundException(username, EntityNotFoundException.Entity.USER);
        }
        if (userEntityOptional.get().getRole() == Role.ADMIN && userService.howManyAdmins() == 1) {
            throw new AtLeastOneAdminException();
        }
        userService.delete(userEntityOptional.get());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("{username}")
    public void editUser(@RequestBody RegisterRequestDTO registerRequest, @PathVariable String username) throws EntityNotFoundException, AtLeastOneAdminException {
        Optional<UserEntity> userEntity = userService.findByUsername(username);
        if (userEntity.isEmpty()) {
            throw new EntityNotFoundException(username, EntityNotFoundException.Entity.USER);
        }
        if (registerRequest.getRole() != Role.ADMIN && userEntity.get().getRole() == Role.ADMIN && userService.howManyAdmins() == 1) {
            throw new AtLeastOneAdminException();
        }
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        userEntity.get().setPassword(encodedPassword);
        userEntity.get().setRole(registerRequest.getRole());
        userEntity.get().setColor(registerRequest.getColor());
        userService.save(userEntity.get());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("")
    public List<UserInfoResponse> getUsers() {
        List<UserEntity> users = userService.listAll();
        return users.stream()
                .map(userMapper::mapTo)
                .map(userService::fromUserToInfo)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMeUser() throws EntityNotFoundException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserEntity> user = userService.findByUsername(username);
        if (user.isEmpty()) {
            throw new EntityNotFoundException(username, EntityNotFoundException.Entity.USER);
        }
        return user.map(userEntity -> ResponseEntity.ok(userService.fromUserToInfo(userMapper.mapTo(userEntity)))).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','OWNER', 'CONTRIBUTOR', 'REQUESTER', 'VIEWER')")
    @PutMapping("/me")
    public void editMe(@RequestBody RegisterRequestDTO registerRequest) throws EntityNotFoundException, AtLeastOneAdminException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserEntity> userEntity = userService.findByUsername(username);
        if (userEntity.isEmpty()) {
            throw new EntityNotFoundException(username, EntityNotFoundException.Entity.USER);
        }
        if (registerRequest.getRole() != Role.ADMIN && userEntity.get().getRole() == Role.ADMIN && userService.howManyAdmins() == 1) {
            throw new AtLeastOneAdminException();
        }
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        userEntity.get().setPassword(encodedPassword);
        userEntity.get().setRole(registerRequest.getRole());
        userEntity.get().setColor(registerRequest.getColor());
        userService.save(userEntity.get());
    }

    @GetMapping("/adminUserExists")
    public ResponseEntity<Boolean> adminUserExists() {
        return ResponseEntity.ok(userService.adminUserExists());
    }

    @PostMapping("/registerFirstAdmin")
    public void registerFirstAdmin(@RequestBody RegisterRequestDTO registerRequest) {
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        UserDTO userDTO = UserDTO.builder().password(encodedPassword).username(registerRequest.getUsername()).role(Role.ADMIN).color(registerRequest.getColor()).build();
        UserEntity userEntity = userMapper.mapFrom(userDTO);
        userService.createUser(userEntity);
    }

}
