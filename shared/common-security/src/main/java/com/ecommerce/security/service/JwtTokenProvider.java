package com.ecommerce.security.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT Token Provider Service
 *
 * Handles creation and validation of JWT tokens for authentication
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${app.jwt.access-token-expiration:900}") // 15 minutes
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration:2592000}") // 30 days
    private long refreshTokenExpiration;

    @Value("${app.jwt.issuer:ecommerce-platform}")
    private String issuer;

    private final RSAKey rsaJWK;

    public JwtTokenProvider() {
        try {
            // Generate RSA key pair for JWT signing
            KeyPair keyPair = generateRSAKeyPair();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            this.rsaJWK = new RSAKey.Builder(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA key pair for JWT signing", e);
        }
    }

    /**
     * Generate RSA key pair for JWT signing
     */
    private KeyPair generateRSAKeyPair() throws JOSEException {
        return new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate();
    }

    /**
     * Generate access token for authenticated user
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .audience(List.of("ecommerce-client"))
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(accessTokenExpiration, ChronoUnit.SECONDS)))
                .claim("username", userDetails.getUsername())
                .claim("authorities", authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .claim("token_type", "access")
                .claim("jti", UUID.randomUUID().toString())
                .build();

        return signToken(claimsSet);
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String username) {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .issuer(issuer)
                .audience(List.of("ecommerce-client"))
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(refreshTokenExpiration, ChronoUnit.SECONDS)))
                .claim("token_type", "refresh")
                .claim("jti", UUID.randomUUID().toString())
                .build();

        return signToken(claimsSet);
    }

    /**
     * Sign JWT claims with RSA private key
     */
    private String signToken(JWTClaimsSet claimsSet) {
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .keyID(rsaJWK.getKeyID())
                    .build();

            JWSSigner signer = new RSASSASigner(rsaJWK);

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to sign JWT token", e);
        }
    }

    /**
     * Get RSA public key for token validation
     */
    public RSAPublicKey getPublicKey() {
        try {
            return rsaJWK.toRSAPublicKey();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to extract public key from RSA JWK", e);
        }
    }

    /**
     * Validate JWT token and return claims
     */
    public JWTClaimsSet validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Validate signature
            JWSVerifier verifier = new RSASSAVerifier(rsaJWK.toRSAPublicKey());
            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("Invalid JWT signature");
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            // Validate expiration
            if (claimsSet.getExpirationTime() != null &&
                claimsSet.getExpirationTime().before(new Date())) {
                throw new RuntimeException("JWT token has expired");
            }

            // Validate issuer
            if (!issuer.equals(claimsSet.getIssuer())) {
                throw new RuntimeException("Invalid JWT issuer");
            }

            return claimsSet;
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to validate JWT token", e);
        }
    }

    /**
     * Extract username from JWT token
     */
    public String getUsernameFromToken(String token) {
        try {
            JWTClaimsSet claimsSet = validateToken(token);
            return claimsSet.getSubject();
        } catch (Exception e) {
            log.error("Failed to extract username from token", e);
            return null;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            JWTClaimsSet claimsSet = validateToken(token);
            return claimsSet.getExpirationTime() != null &&
                   claimsSet.getExpirationTime().before(new Date());
        } catch (Exception e) {
            return true; // If validation fails, consider token expired
        }
    }

    /**
     * Extract authorities from JWT token
     */
    @SuppressWarnings("unchecked")
    public List<String> getAuthoritiesFromToken(String token) {
        try {
            JWTClaimsSet claimsSet = validateToken(token);
            return (List<String>) claimsSet.getClaim("authorities").asList(String.class);
        } catch (Exception e) {
            log.error("Failed to extract authorities from token", e);
            return List.of();
        }
    }
}