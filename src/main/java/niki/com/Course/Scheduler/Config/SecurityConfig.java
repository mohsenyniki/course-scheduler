package niki.com.Course.Scheduler.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/enroll/**").authenticated() // must be logged in to enroll
                .anyRequest().permitAll() // everything else is public
            )
            .formLogin(form -> form
                .loginPage("/login") // your custom login page (or default if not created)
                .defaultSuccessUrl("/schedule", true) // redirect to schedule page after login
                .permitAll()
            )
            .logout(logout -> logout
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
}
