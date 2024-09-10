package com.otcbridge.config;

import com.otcbridge.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 요청은 별도로 처리
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }


        // 인증이 필요 없는 경로는 여기서 처리
        if (isExcludedPath(request.getRequestURI())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authentication required");
            return false;
        }

        if (!userService.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return false;
        }
//
//        // 토큰에서 사용자 정보를 추출하여 요청에 추가
//        String username = userService.getUsernameFromToken(token);
//        request.setAttribute("username", username);

        return true;
    }

    private boolean isExcludedPath(String requestURI) {
        // 인증이 필요 없는 경로 목록
        String[] excludedPaths = {"/api/v1/login"};
        for (String path : excludedPaths) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

}
