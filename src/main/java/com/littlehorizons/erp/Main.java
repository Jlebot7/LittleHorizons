package com.littlehorizons.erp;

import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.view.MainMenuView;

public class Main {

    public static void main(String[] args) {
        LoggerUtil.info("Starting Little Horizons ERP Educational Management System...");
        try {
            MainMenuView mainMenuView = new MainMenuView();
            mainMenuView.start();
        } catch (Exception e) {
            LoggerUtil.error("Uncaught exception in application main thread", e);
            System.err.println("\n[ERROR CRÍTICO] Se ha producido un error inesperado. Consulte 'error_log.txt'.");
        } finally {
            LoggerUtil.info("Little Horizons ERP System shut down.");
        }
    }
}
