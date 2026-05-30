package com.peopleinfo.config;

import com.peopleinfo.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/", "/login", "/register", "/api/auth/**").permitAll()
                .requestMatchers("/hr/**").hasRole("HR")
                .requestMatchers("/employee/**").hasAnyRole("HR", "EMPLOYEE")
                .anyRequest().authenticated()
            )
            // Spring Security's formLogin automatically creates an HttpSession upon successful 
            // authentication. It then sends a 'Set-Cookie' header back to the browser 
            // containing the JSESSIONID. The browser stores this cookie and sends it 
            // with subsequent requests to maintain the user's authenticated state.
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((req, res, auth) -> {
                    var authorities = auth.getAuthorities();
                    String role = authorities.iterator().next().getAuthority();
                    if (role.equals("ROLE_HR")) {
                        res.sendRedirect("/hr/dashboard");
                    } else {
                        res.sendRedirect("/employee/dashboard");
                    }
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true) // Invalidates the session on the server
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID") // Explicitly instruct the browser to delete the session cookie
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("uniqueRememberMeKeyForPeopleInfo")
                .tokenValiditySeconds(86400) // 24 hours
                .userDetailsService(userDetailsService)
            )
            .userDetailsService(userDetailsService);

        return http.build();
    }
}
