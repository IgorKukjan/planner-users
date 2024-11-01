package ru.javabegin.micro.planner.users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
//с каких пакетов начинать искать spring beans
@ComponentScan(basePackages = {"ru.javabegin.micro.planner"})
//шде искать jpa repositories
@EnableJpaRepositories(basePackages = {"ru.javabegin.micro.planner.users"})
public class PlannerUsersApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlannerUsersApplication.class, args);
    }

}
