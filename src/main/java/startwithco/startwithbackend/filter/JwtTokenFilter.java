package startwithco.startwithbackend.filter;

import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import startwithco.startwithbackend.exception.NotFoundException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

public class JwtTokenFilter extends OncePerRequestFilter {

    private final String jwtSecret;

    private final List<RequestMatcher> permitAllRequestMatchers;

    public JwtTokenFilter(String jwtSecret, List<String> permitAllEndpoints) {
        this.jwtSecret = jwtSecret;
        this.permitAllRequestMatchers = permitAllEndpoints.stream()
                .map(AntPathRequestMatcher::new)
                .collect(Collectors.toList());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Filter가 적용되고 있는 uri 추출
        String method = request.getMethod();

        // pre-flight 요청일 때, 해당 Filter 건너뜀.
        if (method.equals("OPTIONS")) {
            return;
        }

        // Check if the request matches any permitAll endpoint
        boolean isPermitAllEndpoint = permitAllRequestMatchers.stream()
                .anyMatch(matcher -> matcher.matches(request));

        if (isPermitAllEndpoint) {
            filterChain.doFilter(request, response); // 건너뛰고 다음 필터로 넘어갑니다.
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " from the header value

            try {
                Jws<Claims> claimsJws = Jwts.parser()
                        .setSigningKey(jwtSecret)
                        .parseClaimsJws(token);

                Claims claims = claimsJws.getBody();
                String username = claims.getSubject();

                UserDetails userDetails = User.withUsername(username)
                        .password("")
                        .authorities(new ArrayList<>())
                        .build();

                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                response.setHeader("Authorization", "Bearer " + token); // Add "Bearer " to the header value
                filterChain.doFilter(request, response);

            } catch (ExpiredJwtException e) {
               throw  new NotFoundException(
                        HttpStatus.UNAUTHORIZED.value(),
                        "존재하지 않는 수요 기업입니다.",
                        getCode("존재하지 않는 수요 기업입니다.", ExceptionCodeMapper.ExceptionType.UNAUTHORIZED)
                );
            }

            catch (JwtException e) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
    }
}