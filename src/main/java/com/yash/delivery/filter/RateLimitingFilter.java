package com.yash.delivery.filter;

import com.yash.delivery.service.OrderService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(OrderService.class);

    private static final int MAX_REQUESTS = 40;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, RequestInfo> requests = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = request.getRemoteAddr();

        RequestInfo info = requests.computeIfAbsent(
                ip,
                k -> new RequestInfo(0, Instant.now().getEpochSecond())
        );

        long now = Instant.now().getEpochSecond();

        synchronized (info) {

            if (now - info.windowStart >= WINDOW_SECONDS) {
                info.count = 0;
                info.windowStart = now;
            }

            info.count++;

            if (info.count > MAX_REQUESTS) {

                response.setStatus(429);
                response.getWriter().write("Too many requests");
                log.warn(
                        "Rate limit exceeded for IP {}",
                        request.getRemoteAddr()
                );

                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static class RequestInfo {

        int count;
        long windowStart;

        RequestInfo(int count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator");
    }
}