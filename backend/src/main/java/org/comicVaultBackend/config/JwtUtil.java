package org.comicVaultBackend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.comicVaultBackend.domain.dto.AuthentificationRequestDTO;
import org.comicVaultBackend.domain.entities.RefreshTokenEntity;
import org.comicVaultBackend.domain.entities.UserEntity;
import org.comicVaultBackend.domain.regular.Role;
import org.comicVaultBackend.exceptions.EntityNotFoundException;
import org.comicVaultBackend.repositories.RefreshTokenRepository;
import org.comicVaultBackend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

import static org.comicVaultBackend.exceptions.EntityNotFoundException.Entity.REFRESH_TOKEN;

@Component
public class JwtUtil {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${JWT_ACCESS_SECRET}")
    private String accessSecret;

    @Value("${JWT_REFRESH_SECRET}")
    private String refreshSecret;

    private SecretKey ACCESS_SECRET_KEY;
    private SecretKey REFRESH_SECRET_KEY;

    private final long ACCESS_TOKEN_EXPIRY = 15 * 60 * 1000;       // 15 minutes
    //private final long ACCESS_TOKEN_EXPIRY = 15 * 1000;
    private final long REFRESH_TOKEN_EXPIRY = 30L * 24 * 60 * 60 * 1000; // 30 days

    @PostConstruct
    public void init() {
        this.ACCESS_SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(accessSecret));
        this.REFRESH_SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(refreshSecret));
    }

    public long getRefreshTtlMs() {
        return REFRESH_TOKEN_EXPIRY;
    }

    public long getAccessTtlMs() {
        return ACCESS_TOKEN_EXPIRY;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Role extractRole(String token) {
        String username = extractClaim(token, Claims::getSubject);
        return userRepository.findByUsername(username)
                .map(UserEntity::getRole)
                .orElse(Role.NONE);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            return null;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(ACCESS_SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Claims extractAllClaimsRefresh(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(REFRESH_SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createAccessToken(claims, userDetails.getUsername());
    }

    private String createAccessToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
                .signWith(ACCESS_SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails, AuthentificationRequestDTO authenticationRequest, String clientIp) {
        String jti = UUID.randomUUID().toString();

        Date expiryDate = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY);

        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setId(jti)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(REFRESH_SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();


        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder().id(jti)
                .username(authenticationRequest.getUsername())
                .expiryDate(new Date(System.currentTimeMillis() + getRefreshTtlMs()))
                .userAgent(authenticationRequest.getUserAgent())
                .os(authenticationRequest.getOs())
                .browser(authenticationRequest.getBrowser())
                .device(authenticationRequest.getDevice())
                .osVersion(authenticationRequest.getOsVersion())
                .browserVersion(authenticationRequest.getBrowserVersion())
                .orientation(authenticationRequest.getOrientation())
                .ip(clientIp)
                .build();

        refreshTokenRepository.save(tokenEntity);


        Claims claims = Jwts.parserBuilder()
                .setSigningKey(REFRESH_SECRET_KEY)   // must be refresh secret
                .build()
                .parseClaimsJws(token)
                .getBody();


        return token;
    }

    public String renewRefreshToken(RefreshTokenEntity tokenEntity) {
        Date expiryDate = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY);

        String token = Jwts.builder()
                .setSubject(tokenEntity.getUsername())
                .setId(tokenEntity.getId())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(REFRESH_SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();

        tokenEntity.setExpiryDate(new Date(System.currentTimeMillis() + getRefreshTtlMs()));
        refreshTokenRepository.save(tokenEntity);
        return token;
    }

    public Boolean validateAccessToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean validateRefreshToken(String token) {
        try {
            getValidRefreshToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public RefreshTokenEntity getValidRefreshToken(String token) throws EntityNotFoundException {
        try {
            Claims claims = extractAllClaimsRefresh(token);

            String jti = claims.getId();
            Optional<RefreshTokenEntity> storedToken = refreshTokenRepository.findById(jti);

            if (storedToken.isEmpty()) {
                throw new EntityNotFoundException("", REFRESH_TOKEN);
            }

            if (storedToken.get().getExpiryDate().before(new Date())) {
                refreshTokenRepository.delete(storedToken.get());
                throw new EntityNotFoundException("", REFRESH_TOKEN);
            }

            return storedToken.get();

        } catch (Exception e) {
            throw new EntityNotFoundException("", REFRESH_TOKEN);
        }
    }

    public void deleteTokenTokenIfExisting(String token) {
        try {
            RefreshTokenEntity tokenEntity = getValidRefreshToken(token);
            refreshTokenRepository.delete(tokenEntity);
        } catch (Exception e) {
            //Nevermind
        }
    }

}
