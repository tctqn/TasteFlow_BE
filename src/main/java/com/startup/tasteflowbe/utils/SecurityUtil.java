//package com.startup.tasteflowbe.utils;
//
//import com.startup.tasteflowbe.model.User;
//import com.startup.tasteflowbe.repository.UserRepository;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//public class SecurityUtil {
//    public static User getCurrentUser(UserRepository userRepository) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
//            String username = (String) authentication.getPrincipal();
//            return userRepository.findByUsername(username).orElse(null);
//        }
//        return null;
//    }
//}