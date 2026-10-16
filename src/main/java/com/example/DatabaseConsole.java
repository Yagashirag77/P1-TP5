package com.example;

import org.h2.tools.Server;

import java.sql.SQLException;

public class DatabaseConsole {

    private DatabaseConsole() {
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        Server webServer = Server.createWebServer("-webPort", "8083").start();
        System.out.println("Console H2 : http://localhost:8083");
        System.out.println("URL JDBC   : jdbc:h2:file:./data/salle_db");
        System.out.println("Utilisateur: sa");
        System.out.println("Mot de passe: laisser vide");
        System.out.println("Utilisez Ctrl+C pour arrêter la console.");

        Runtime.getRuntime().addShutdownHook(new Thread(webServer::stop));
        Thread.currentThread().join();
    }
}
