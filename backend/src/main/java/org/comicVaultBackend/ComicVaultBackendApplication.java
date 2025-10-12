package org.comicVaultBackend;

import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import org.comicVaultBackend.domain.entities.SchemaVersionEntity;
import org.comicVaultBackend.repositories.SchemaVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableJpaRepositories("org.comicVaultBackend.repositories")
@EntityScan("org.comicVaultBackend.domain.entities")
public class ComicVaultBackendApplication {

    private static final Logger logger = LoggerFactory.getLogger(ComicVaultBackendApplication.class);
    @Autowired
    SchemaVersionRepository schemaVersionRepository;

    public static void main(String[] args) {
        try {
            SevenZip.initSevenZipFromPlatformJAR();
            logger.info("7-Zip-JBinding library was initialized");
        } catch (SevenZipNativeInitializationException e) {
            e.printStackTrace();
        }
        SpringApplication.run(ComicVaultBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner insertSchemaVersion(SchemaVersionRepository repository) {
        return args -> {
            // Define default schema version
            String defaultVersion = "1.0.0";

            // Check if the schema version already exists
            if (repository.findById(defaultVersion).isEmpty()) {
                SchemaVersionEntity schemaVersion = new SchemaVersionEntity();
                schemaVersion.setSchemaVersionID(defaultVersion);
                repository.save(schemaVersion);
            }
        };
    }
}
