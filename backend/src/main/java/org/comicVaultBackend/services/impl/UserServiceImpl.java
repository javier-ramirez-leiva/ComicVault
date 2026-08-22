package org.comicVaultBackend.services.impl;

import org.comicVaultBackend.annotations.CVReadTransaction;
import org.comicVaultBackend.annotations.CVWriteTransaction;
import org.comicVaultBackend.domain.dto.UserDTO;
import org.comicVaultBackend.domain.entities.ProgressEntity;
import org.comicVaultBackend.domain.entities.UserEntity;
import org.comicVaultBackend.domain.regular.Role;
import org.comicVaultBackend.domain.regular.UserInfoResponse;
import org.comicVaultBackend.repositories.ProgressRepository;
import org.comicVaultBackend.repositories.RefreshTokenRepository;
import org.comicVaultBackend.repositories.UserRepository;
import org.comicVaultBackend.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ProgressRepository progressRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    @CVWriteTransaction
    public void createUser(UserEntity user) {
        logger.info("Created user : {}, {}, {}", user.getUsername(), user.getRole(), user.getColor());
        userRepository.save(user);
    }

    private String _createHasFromString(String inputString) throws NoSuchAlgorithmException {
        byte[] encodedBytes = inputString.getBytes();

        // Create a SHA-256 hash object
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Update the hash object with the encoded bytes
        byte[] hashBytes = digest.digest(encodedBytes);

        // Encode the digest using Base64
        String base64Encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);

        // Truncate to the first 10 characters
        return base64Encoded.length() > 10 ? base64Encoded.substring(0, 10) : base64Encoded;
    }

    @Override
    @CVReadTransaction
    public List<UserEntity> listAll() {
        return new ArrayList<>(userRepository.findAllByOrderByCreatedAtDesc());
    }


    @Override
    @CVReadTransaction
    public boolean adminUserExists() {
        Optional<UserEntity> adminuser = userRepository.findFirstByRole(Role.ADMIN);

        return adminuser.isPresent();
    }

    @Override
    @CVReadTransaction
    public int howManyAdmins() {
        List<UserEntity> adminUsers = userRepository.findAllByRole(Role.ADMIN);
        return adminUsers.size();
    }


    @Override
    @CVReadTransaction
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @CVWriteTransaction
    public void save(UserEntity user) {
        userRepository.save(user);
        logger.info("Saved user : {}, {}, {}", user.getUsername(), user.getRole(), user.getColor());
    }

    @Override
    @CVWriteTransaction
    public void delete(UserEntity user) {
        String username = user.getUsername();
        List<ProgressEntity> progressEntityList = user.getProgress();
        for (ProgressEntity progress : progressEntityList) {
            progressRepository.delete(progress);
        }
        refreshTokenRepository.deleteByUsername(username);
        userRepository.delete(user);
        logger.info("Deleted user : {}", user.getUsername());
    }

    @Override
    public UserInfoResponse fromUserToInfo(UserDTO userDTO) {
        return new UserInfoResponse(userDTO.getUsername(), userDTO.getRole(), userDTO.getColor());
    }
}
