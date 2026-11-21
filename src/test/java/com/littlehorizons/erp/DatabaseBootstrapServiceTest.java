package com.littlehorizons.erp;

import com.littlehorizons.erp.service.DatabaseBootstrapService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DatabaseBootstrapServiceTest {

    @Test
    void testBootstrapDatabaseResilience() {
        DatabaseBootstrapService service = new DatabaseBootstrapService();
        assertNotNull(service);
        assertDoesNotThrow(service::bootstrapDatabase);
    }
}
