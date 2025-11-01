package niki.com.Course.Scheduler.Data;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UserDetailService {

    private final PasswordEncoder passwordEncoder;

    public UserDetailService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user1 = User.withUsername("Zahra")
                .password(passwordEncoder.encode("zahrapassword"))
                .roles("USER")
                .build();

        UserDetails user2 = User.withUsername("Parisa")
                .password(passwordEncoder.encode("parisapassword"))
                .roles("ADMIN")
                .build();
        UserDetails user3 = User.withUsername("Parastoo")
                .password(passwordEncoder.encode("parastoopassword"))
                .roles("USER1")
                .build();
        UserDetails user4 = User.withUsername("Ali")
                .password(passwordEncoder.encode("alipassword"))
                .roles("USER")
                .build();
        UserDetails user5 = User.withUsername("Armin")
                .password(passwordEncoder.encode("arminpassword"))
                .roles("USER")
                .build();
        UserDetails user8 = User.withUsername("Ahmad")
                .password(passwordEncoder.encode("ahmadpassword"))
                .roles("USER")
                .build();
        UserDetails user9 = User.withUsername("Alice")
                .password(passwordEncoder.encode("alicepassword"))
                .roles("USER")
                .build();
        UserDetails user10 = User.withUsername("Max")
                .password(passwordEncoder.encode("maxepassword"))
                .roles("USER")
                .build();
       

        return new InMemoryUserDetailsManager(user1, user2, user3, user4, user5, user8, user9, user10);
    }
}
