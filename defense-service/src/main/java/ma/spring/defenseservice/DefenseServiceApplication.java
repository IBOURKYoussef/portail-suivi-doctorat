package ma.spring.defenseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "ma.spring.defenseservice.client")
public class DefenseServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DefenseServiceApplication.class, args);
    }
}