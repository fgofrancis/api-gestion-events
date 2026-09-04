package com.gestion.eventos.api.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

 @Component
 public class EnvInspector {

    @Autowired
    private Environment environment;

    @PostConstruct
    public void logEnvironment() {
        System.out.println("=== Perfiles activos ===");
        System.out.println(Arrays.toString(environment.getActiveProfiles()));

        System.out.println("=== Perfiles por defecto ===");
        System.out.println(Arrays.toString(environment.getDefaultProfiles()));

        System.out.println("=== Propiedades cargadas ===");
        // Ejemplo: imprime algunas claves importantes
        System.out.println("spring.profiles.active = " + environment.getProperty("spring.profiles.active"));
        System.out.println("spring.config.import = " + environment.getProperty("spring.config.import"));
        System.out.println("server.port = " + environment.getProperty("server.port"));
    }
}

