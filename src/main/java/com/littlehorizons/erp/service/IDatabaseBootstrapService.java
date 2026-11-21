package com.littlehorizons.erp.service;

public interface IDatabaseBootstrapService {
    boolean bootstrapDatabase();
    boolean ensureSchemaAndTablesExist();
}
