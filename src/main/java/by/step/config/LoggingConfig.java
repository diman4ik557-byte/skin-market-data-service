package by.step.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Конфигурация для логирования.
 * Позволяет внедрять логгеры через @Autowired.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Configuration
public class LoggingConfig {

    /**
     * Создает логгер для класса, в который внедряется.
     * Использование: @Autowired private Logger log;
     *
     * @param injectionPoint точка входа
     * @return логгер для класса
     */
    @Bean
    @Scope("prototype")
    public Logger logger(InjectionPoint injectionPoint) {
        Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();
        return LoggerFactory.getLogger(targetClass);
    }
}