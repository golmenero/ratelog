package org.ratelog

import org.ratelog.annotations.CurrentUserArgumentResolver
import org.ratelog.i18n.UserAwareLocaleResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@SpringBootApplication
@EnableScheduling
class RatelogApplication

fun main(args: Array<String>) {
    runApplication<RatelogApplication>(*args)
}

@Configuration
class WebConfig : WebMvcConfigurer {

    @Bean
    fun messageSource(): ResourceBundleMessageSource {
        val source = ResourceBundleMessageSource()
        source.setBasenames("messages")
        source.setDefaultEncoding("UTF-8")
        source.setFallbackToSystemLocale(false)
        return source
    }

    @Bean
    fun localeResolver(): UserAwareLocaleResolver {
        return UserAwareLocaleResolver()
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(CurrentUserArgumentResolver())
    }
}