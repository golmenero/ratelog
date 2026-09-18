package org.ratelog

import org.ratelog.system.RememberMeKeyProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val rememberMeKeyProvider: RememberMeKeyProvider,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/login", "/styles.css", "/lucide.min.js", "/img/**", "/manifest.webmanifest").permitAll()
                    .requestMatchers("/*.css", "/*.js").permitAll()
                    .requestMatchers("/api/health").permitAll()
                    .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form
                    .loginPage("/login")
                    .loginProcessingUrl("/login/process")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/login?error=true")
                    .permitAll()
            }

        http.rememberMe { remember ->
            remember
                .key(rememberMeKeyProvider.key())
                .tokenValiditySeconds(86400 * 30)
                .rememberMeParameter("remember-me")
        }

        http
            .logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            }

        http
            .exceptionHandling { eh ->
                eh.accessDeniedHandler { _, response, _ ->
                    response.sendRedirect("/profile")
                }
            }

        return http.build()
    }
}
