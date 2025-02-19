package ru.javabegin.micro.planner.users.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import ru.javabegin.micro.planner.utils.converter.KCRoleConverter;

@Configuration //конфиг для spring контейнера
@EnableWebSecurity // механизм защиты адресов, которые настраиваются в SecurityFilterChain
@EnableGlobalMethodSecurity(prePostEnabled = true) // включение механизма для защиты методов по ролям
public class SpringSecurityConfig {

    // создается спец. бин, который отвечает за настройки запросов по http (метод вызывается автоматически) Spring контейнером
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // конвертер для настройки spring security
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        // подключаем конвертер ролей
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KCRoleConverter());


        return http.authorizeHttpRequests(auth ->
                auth
//                        .requestMatchers("/test/login").permitAll()

//                          .requestMatchers("/**").permitAll()

                        .requestMatchers("/admin/*").hasRole("admin")
                        .requestMatchers("/auth/*").hasRole("user")

//                        .requestMatchers("/user/*").hasAuthority("ROLE_user")
//                        .requestMatchers("/admin/*").hasRole("admin")

                        .anyRequest().authenticated())  //без access tokena не работают
                        .csrf(csrf -> csrf.disable())
                        .oauth2ResourceServer(oauth2 -> oauth2 // добавляем конвертер ролей из JWT в Authority (Role)
                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
        ).build();
    }

}
