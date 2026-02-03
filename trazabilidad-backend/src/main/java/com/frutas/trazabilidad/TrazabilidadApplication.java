package com.frutas.trazabilidad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TrazabilidadApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrazabilidadApplication.class, args);
    }
}