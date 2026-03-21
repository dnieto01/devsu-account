package com.ds.devsuaccount.infraestructure.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component
@Order
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.database.seed.enabled", havingValue = "true", matchIfMissing = true)
public class BaseDatosInitializer implements ApplicationRunner {

    private static final String TABLE = "client_account";

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        if (!isTableEmpty()) {
            return;
        }
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("BaseDatos.sql"));
            populator.setSeparator(";");
            try (var c = dataSource.getConnection()) {
                populator.populate(c);
            }
            log.info("BaseDatos.sql aplicado ({} vacía).", TABLE);
        } catch (Exception e) {
            log.error("No se pudo ejecutar BaseDatos.sql", e);
            throw new IllegalStateException("Fallo al popular la base de datos inicial", e);
        }
    }

    private boolean isTableEmpty() {
        try {
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + TABLE, Long.class);
            if (count != null && count > 0) {
                log.debug("BaseDatos.sql omitido: {} ya tiene {} filas.", TABLE, count);
                return false;
            }
            return true;
        } catch (DataAccessException e) {
            log.warn("BaseDatos.sql omitido: no se pudo leer {} ({})", TABLE, e.getMessage());
            return false;
        }
    }
}
