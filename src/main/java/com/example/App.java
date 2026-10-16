package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

public class App {

    private App() {
    }

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SallePU");
        EntityManager em = emf.createEntityManager();

        try {
            insertDemoData(em);

            SalleServiceImpl serviceImpl = new SalleServiceImpl();
            serviceImpl.setEntityManager(em);
            SalleService salleService = serviceImpl;

            LocalDateTime debut = LocalDateTime.of(2026, 11, 3, 14, 0);
            LocalDateTime fin = LocalDateTime.of(2026, 11, 3, 16, 0);

            List<Salle> disponibles = salleService.getAvailableRooms(debut, fin);
            System.out.println("\nSalles disponibles entre " + debut + " et " + fin + " :");
            printRooms(disponibles);

            PaginationResult<Salle> page = salleService.searchRooms("i", 25, debut, fin, 1, 2);
            System.out.println("\nRecherche personnalisée (nom contient 'i', capacité >= 25) :");
            printRooms(page.getItems());
            System.out.printf("Page %d/%d - %d résultat(s)%n",
                    page.getCurrentPage(), page.getTotalPages(), page.getTotalItems());
        } finally {
            em.close();
            emf.close();
        }
    }

    private static void insertDemoData(EntityManager em) {
        Long roomCount = em.createQuery("SELECT COUNT(s) FROM Salle s", Long.class)
                .getSingleResult();
        if (roomCount > 0) {
            return;
        }

        LocalDateTime jour = LocalDateTime.of(2026, 11, 3, 0, 0);
        Salle ibnBattouta = new Salle("Ibn Battouta", 18,
                "Pôle informatique", 1, "Réunion", true);
        Salle alIdrissi = new Salle("Al Idrissi", 32,
                "Pôle ingénierie", 2, "Cours", true);
        Salle zellige = new Salle("Zellige", 55,
                "Bâtiment central", 0, "Conférence", false);
        Salle toubkal = new Salle("Toubkal", 24,
                "Pôle informatique", 3, "Travaux pratiques", true);

        alIdrissi.addReservation(new Reservation(
                jour.withHour(13).withMinute(30), jour.withHour(15),
                "Soutenance de projet", "Mme El Mansouri", 22, "CONFIRMEE"
        ));
        zellige.addReservation(new Reservation(
                jour.withHour(16), jour.withHour(18),
                "Rencontre des étudiants", "M. Amrani", 45, "PLANIFIEE"
        ));

        em.getTransaction().begin();
        em.persist(ibnBattouta);
        em.persist(alIdrissi);
        em.persist(zellige);
        em.persist(toubkal);
        em.getTransaction().commit();
    }

    private static void printRooms(List<Salle> salles) {
        for (Salle salle : salles) {
            String projecteur = Boolean.TRUE.equals(salle.getProjecteurDisponible()) ? "oui" : "non";
            System.out.printf("- %s | %s, étage %s | %s places | projecteur : %s%n",
                    salle.getNom(), salle.getBatiment(), salle.getEtage(),
                    salle.getCapacite(), projecteur);
        }
    }
}
