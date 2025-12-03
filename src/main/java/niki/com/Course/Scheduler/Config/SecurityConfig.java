package niki.com.Course.Scheduler.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth", "/register", "/login", "/css/**", "/js/**", "/admin/**").permitAll() // allow auth page, admin, and static resources
                .requestMatchers("/enroll/**", "/schedule").authenticated() // must be logged in to enroll or view schedule
                .anyRequest().permitAll() // everything else is public
            )
            .formLogin(form -> form
                .loginPage("/auth") // modern authentication page
                .loginProcessingUrl("/login") // where to submit the login form
                .failureUrl("/auth?error=true") // redirect here on login failure
                .successHandler(enrollmentSuccessHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/auth?logout=true")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // disable for now, can enable later

        return http.build();
    }

    // For production, use BCryptPasswordEncoder for password encoding
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private AuthenticationSuccessHandler enrollmentSuccessHandler;
}
