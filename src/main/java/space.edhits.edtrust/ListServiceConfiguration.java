package space.edhits.edtrust;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans!
 */
@Configuration
public class ListServiceConfiguration {

    @Bean
    public UserApiContextFactory apiContextFactory(ApiKeyResolver resolver) {
        return new UserApiContextFactory(resolver);
    }

    @Bean
    public ApiKeyResolver apiKeyResolver() {
        return new ApiKeyResolver();
    }
}
