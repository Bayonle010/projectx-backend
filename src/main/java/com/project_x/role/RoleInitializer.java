package com.project_x.role;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class RoleInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RoleInitializer.class);
    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Seeding roles into database…");

        Arrays.stream(RoleType.values())
                // prefix each enum name with “ROLE_”
                .map(rt -> "ROLE_" + rt.name())
                .forEach(authority -> {
                    if (roleRepository.findByAuthority(authority).isEmpty()) {
                        roleRepository.save(new Role(authority));
                        logger.info("  • created role {}", authority);
                    }
                });
    }
}
