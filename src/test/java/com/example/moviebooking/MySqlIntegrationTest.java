package com.example.moviebooking;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.containers.MySQLContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
@Testcontainers(disabledWithoutDocker=true)
class MySqlIntegrationTest extends AuthIntegrationTest {
 @Container static MySQLContainer<?> mysql=new MySQLContainer<>("mysql:8.0");
 @DynamicPropertySource static void database(DynamicPropertyRegistry registry){registry.add("spring.datasource.url",mysql::getJdbcUrl);registry.add("spring.datasource.username",mysql::getUsername);registry.add("spring.datasource.password",mysql::getPassword);registry.add("spring.datasource.driver-class-name",()->"com.mysql.cj.jdbc.Driver");}
}
