package com.santechture.api.service;

import com.santechture.api.dto.GeneralResponse;
import com.santechture.api.dto.admin.AdminDto;
import com.santechture.api.entity.Admin;
import com.santechture.api.exception.BusinessExceptions;
import com.santechture.api.repository.AdminRepository;
import com.santechture.api.security.jwt.JwtUtils;
import com.santechture.api.security.services.UserDetailsImpl;
import com.santechture.api.validation.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminService {


    private final AdminRepository adminRepository;

    private final JwtUtils jwtUtils;


    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder encoder;

    public AdminService(AdminRepository adminRepository, JwtUtils jwtUtils, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.encoder = passwordEncoder;
    }

    public ResponseEntity<GeneralResponse> login(LoginRequest request) throws BusinessExceptions {

        Admin admin = adminRepository.findByUsernameIgnoreCase(request.getUsername());
        if(Objects.isNull(admin)){
            throw new BusinessExceptions("login.credentials.not.match");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);


        String token = jwtUtils.generateJwtToken(authentication);

        return new GeneralResponse().response(new AdminDto(admin, token));
    }

    public void logout() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        if (authentication != null) {
            SecurityContextHolder.clearContext();
        }
    }

    @PostConstruct
    public void addDefaultAdmin() {
        Admin user = new Admin();
        user.setUsername("admin");
        user.setPassword(encoder.encode("p@ssw0rd"));
        adminRepository.saveAndFlush(user);
    }
}
