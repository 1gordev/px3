package com.id.px3.rest.security;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.id.px3.error.PxException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class JwtService {

    private static final String CLAIM_ROLES = "roles";
    private static final String HEADER_TYP = "typ";
    private static final String JWT = "JWT";

    @Value("${px3.auth.jwt.secret:culoecamicia}")
    private String secret;

    @Value("${px3.auth.jwt.issuer:px3}")
    private String issuer;

    /**
     * Generates a JWT token.
     *
     * @param userId The user ID to include in the token.
     * @param roles Array of roles to include in the token.
     * @param expiration Specify how much time until the token expires.
     * @return A signed JWT token.
     */
    public String generateToken(String userId, Set<String> roles, Duration expiration) {
        return com.auth0.jwt.JWT.create()
                .withHeader(Map.of(HEADER_TYP, JWT))
                .withSubject(userId)
                .withIssuer(issuer)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(expiration))
                .withClaim(CLAIM_ROLES, new ArrayList<>(roles))
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * Verifies the validity of a JWT token and checks for specific roles.
     *
     * @param token         The JWT token to verify.
     * @param requiredRoles The roles required to be present in the token (at least one must match).
     * @throws JWTVerificationException if the token is expired, invalid, or does not contain the required roles.
     */
    public String validateTokenWithRoles(String token, Set<String> requiredRoles) throws JWTVerificationException {
        try {
            //  check signature and expiration
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = com.auth0.jwt.JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();

            DecodedJWT jwt = verifier.verify(token);

            //  check roles
            if (!requiredRoles.isEmpty()) {
                List<String> tokenRoles = jwt.getClaim(CLAIM_ROLES).asList(String.class);
                if (tokenRoles.stream().noneMatch(requiredRoles::contains)) {
                    throw new JWTVerificationException("Token does not contain the required roles.");
                }
            }

            //  return the username
            return jwt.getSubject();
        } catch (TokenExpiredException expiredException) {
            throw new PxException(HttpStatus.UNAUTHORIZED, "Token has expired");
        } catch (JWTVerificationException exception) {
            throw new PxException(HttpStatus.FORBIDDEN, "Invalid token or roles");
        }
    }

    /**
     * Verifies the validity of a JWT token and checks for the subject.
     *
     * @param token The JWT token to verify.
     * @return The subject of the token.
     * @throws JWTVerificationException if the token is expired or invalid.
     */
    public String validateTokenAndGetSubject(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = com.auth0.jwt.JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();

            DecodedJWT jwt = verifier.verify(token);
            return jwt.getSubject();
        } catch (TokenExpiredException expiredException) {
            throw new PxException(HttpStatus.UNAUTHORIZED, "Token has expired");
        } catch (JWTVerificationException exception) {
            throw new PxException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }
}
