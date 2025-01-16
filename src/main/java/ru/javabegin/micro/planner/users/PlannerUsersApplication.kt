package ru.javabegin.micro.planner.users

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableDiscoveryClient //с каких пакетов начинать искать spring beans
@ComponentScan(basePackages = ["ru.javabegin.micro.planner"]) //шде искать jpa repositories
@EnableJpaRepositories(basePackages = ["ru.javabegin.micro.planner.users"]) //динамически изменяет состав бина(динамически считает новые значения из properties)
@RefreshScope
open class  PlannerUsersApplication

fun main(args: Array<String>) {
    runApplication<PlannerUsersApplication>(*args)
}

