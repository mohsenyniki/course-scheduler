package niki.com.Course.Scheduler.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/enroll/**").authenticated()  // must be logged in
                .anyRequest().permitAll()                       // everything else is public
            )
            .formLogin(form -> form
                .loginPage("/login")  // custom login page (we’ll add this next)
                .permitAll()
            )
            .logout(logout -> logout
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // disable for now

        return http.build();
    }
}
