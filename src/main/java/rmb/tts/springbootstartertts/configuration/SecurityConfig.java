package rmb.tts.springbootstartertts.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity()
public class SecurityConfig {
  // TODO Remove this comment when the security strategy for this application has been determined
  @Bean
  protected SecurityFilterChain securityWebConfig(final HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(
            request -> new CorsConfiguration().applyPermitDefaultValues()))
        .httpBasic(AbstractHttpConfigurer::disable);

    return http.build();
  }

}
