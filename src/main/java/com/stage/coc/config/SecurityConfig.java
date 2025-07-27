package com.stage.coc.config;

import com.stage.coc.security.JwtAuthenticationEntryPoint;
import com.stage.coc.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeHttpRequests(authz -> authz
                        // Routes publiques
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // Routes importateur/exportateur
                        .requestMatchers("/api/demandes", "/api/demandes/mes-demandes").hasAnyRole("IMPORTATEUR", "EXPORTATEUR")
                        .requestMatchers("/api/importateurs/**").hasRole("IMPORTATEUR")
                        .requestMatchers("/api/exportateur/**").hasRole("EXPORTATEUR")

                        // Routes AGENT seulement (pas superviseur)
                        .requestMatchers("/api/agent/**").hasRole("AGENT")
                        .requestMatchers("/api/demandes/agent").hasRole("AGENT")
                        .requestMatchers("/api/demandes/*/prendre-en-charge").hasRole("AGENT")

                        // Routes SUPERVISEUR seulement
                        .requestMatchers("/api/superviseur/**").hasRole("SUPERVISEUR")
                        .requestMatchers("/api/superviseur/vue-ensemble").hasRole("SUPERVISEUR")
                        .requestMatchers("/api/superviseur/agents").hasRole("SUPERVISEUR")
                        .requestMatchers("/api/superviseur/demandes/*/reaffecter/*").hasRole("SUPERVISEUR")
                        .requestMatchers("/api/superviseur/agents/*/disponibilite").hasRole("SUPERVISEUR")
                        .requestMatchers("/api/superviseur/statistiques").hasRole("SUPERVISEUR")
                        .requestMatchers("/api/superviseur/dashboard").hasRole("SUPERVISEUR")



                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}