    package com.laba.ilaba.security;


    import com.laba.ilaba.entity.User;
    import io.jsonwebtoken.Claims;
    import io.jsonwebtoken.Jwts;
    import io.jsonwebtoken.SignatureAlgorithm;
    import io.jsonwebtoken.security.Keys;
    import jakarta.enterprise.context.ApplicationScoped;
    import lombok.extern.slf4j.Slf4j;

    import javax.crypto.SecretKey;
    import java.time.Period;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.Map;

    import static java.time.Instant.now;

    @Slf4j
    @ApplicationScoped
    public class JwtUtil {

        private SecretKey getSecretKey() {
            String secret = "8f858c0f6db6068397b4d80725ea4d0b94170b0f982a566269a8ab94e7f52775a302025f69e05453953542720aa3bfa66f921776e68625c50c7d7a0f61ab4d87b0b49b16fb145347b92530d371bd1dd58a62a2d0f83722f267a81ebc5f054587b8d0eb080187583a250f1ff16952a9b6b3f20d410bd5150ae35b8ebf754fd7fc5a771c942c2ad57340473858712cf9dd5ab1dc410b220d9eaf0e5482b1975c4dae9dedd344aa471c7da570bd9d177bbea80165a298f616f43329ac443495cbab3a76b388508db5c57c9018e823497e153e7bfcd901b5d8925ea7f963b0bdf884f5ce0cd4fce47dc09c774de685eb5947b30ac9a44aa39f73dce06d33b450eeb09329096cb8ea0cf66fd0b7587430a60e0fcebfa10912039aaddcf4f246ac984bf52e9b3a29bb91a669e147eee0d053824612da3b4f28855881a1d7deca0ec5d3cb74129fe0afa6107cda465fc56408310c875fab3892fbaa1054cd4b55ce452f73e42a1443fb80ae96ba3f1a4ea15224de796147164cab6487d249d23fc4e5a88f75a97f6265b8bdcc8226ee55095e27b623cee7ee25826f864c65e3e12c2c7ea0855e531fc865bbbde304da86dc9aaf3880666c6675ec19b60214a78ad569acb4d5aec56be43c7343554a0aae4a63f26f52c62cc394318cc6fe81d1117f58166e354ff7ae1337aa0f4e56cdee98cbc539fbb522d1fe37af5edf27ffb1da95fe";
            return Keys.hmacShaKeyFor(secret.getBytes());
        }

        public String generateToken(User userDetails) {
            Map<String, Object> claims = new HashMap<>();
            String role = userDetails.getRole().toString();
            claims.put("id", userDetails.getId());
            claims.put("email", userDetails.getEmail());
            claims.put("role", role);
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userDetails.getEmail())
                    .setIssuedAt(Date.from(now()))
                    .setExpiration(Date.from(now().plus(Period.ofWeeks(40))))
                    .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                    .compact();
        }

        public String getUsername(String token) {
            return getAllClaimsFromToken(token).getSubject();
        }


        private Claims getAllClaimsFromToken(String token) {
           Claims claims;
            try {

                claims = Jwts.parser()
                        .verifyWith(getSecretKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            }catch (Exception e) {
                log.error("Invalid JWT signature: {}", e.getMessage());
                claims = null;
            }
            return claims;
        }

        public Date getExpirationDateFromToken(String token) {
            Claims allClaimsFromToken = getAllClaimsFromToken(token);
            if (allClaimsFromToken == null) {
                throw new RuntimeException("Invalid JWT Token");
            }
            return allClaimsFromToken.getExpiration();
        }

        public Boolean isTokenExpired(String token) {
            try {

                final Date expiration = getExpirationDateFromToken(token);
                return !expiration.before(new Date());
            }catch (Exception e) {
                log.error("JWT Token is expired: {}", e.getMessage());
                return true;
            }
        }



    }
