package com.resourcebridge.api_gateway.filters;

import com.resourcebridge.api_gateway.JwtService;
import com.resourcebridge.api_gateway.security.RouteValidator;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Component
public class AuthenticationFilter
        extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {

    private final JwtService jwtService;
    private final RouteValidator routeValidator;

    public AuthenticationFilter(JwtService jwtService,
                                RouteValidator routeValidator) {

        super(NameConfig.class);
        this.jwtService = jwtService;
        this.routeValidator = routeValidator;
    }

    @Override
    public GatewayFilter apply(NameConfig config) {

        return (exchange, chain) -> {

            String path =
                    exchange.getRequest().getURI().getPath();

            if (path.startsWith("/auth")
                    || path.startsWith("/actuator")
                    || path.startsWith("/swagger")
                    || path.startsWith("/v3/api-docs")) {

                log.debug("Public path: {}", path);

                return chain.filter(exchange);
            }

            log.info("Authenticating request: {}", path);


            String header =
                    exchange.getRequest()
                            .getHeaders()
                            .getFirst("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {

                log.warn("Missing Authorization header");

                exchange.getResponse()
                        .setStatusCode(HttpStatus.UNAUTHORIZED);

                return exchange.getResponse().setComplete();
            }

            String token = header.substring(7);

            try {


                String userId =
                        jwtService.getUserIdFromToken(token);

                String role =
                        jwtService.getRolesFromToken(token);

                boolean verified =
                        jwtService.isVerified(token);

                log.debug("Token parsed | id={} | role={}",
                        userId, role);


                if (!verified &&
                        (role.equals("NGO")
                                || role.equals("CLINIC"))) {

                    log.warn("Blocked unverified user | id={}",
                            userId);

                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);

                    return exchange.getResponse().setComplete();
                }


                if (!routeValidator.isAuthorized(role, path)) {

                    log.warn("Access denied | id={} | role={} | path={}",
                            userId, role, path);

                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);

                    return exchange.getResponse().setComplete();
                }

                ServerWebExchange modifiedExchange =
                        exchange.mutate()
                                .request(r -> r
                                        .header("X-User-Id", userId)
                                        .header("X-User-Role", role)
                                        .header("X-User-Verified",
                                                String.valueOf(verified)))
                                .build();

                log.info("Authorized | id={} | role={} | path={}",
                        userId, role, path);

                return chain.filter(modifiedExchange);

            } catch (JwtException e) {

                log.error("Invalid JWT: {}", e.getMessage());

                exchange.getResponse()
                        .setStatusCode(HttpStatus.UNAUTHORIZED);

                return exchange.getResponse().setComplete();

            } catch (Exception e) {

                log.error("Gateway auth error: {}", e.getMessage());

                exchange.getResponse()
                        .setStatusCode(HttpStatus.UNAUTHORIZED);

                return exchange.getResponse().setComplete();
            }
        };
    }
}
