package com.example.repository;

import com.example.model.Reservation;
import com.example.model.Salle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalleRepositoryImplTest {
    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static SalleRepository repository;
    private static LocalDateTime day;

    @BeforeAll
    static void setUp() {
        Map<String, String> testProperties = new HashMap<>();
        testProperties.put("javax.persistence.jdbc.url", "jdbc:h2:mem:salle_test;DB_CLOSE_DELAY=-1");
        testProperties.put("hibernate.hbm2ddl.auto", "create-drop");
        testProperties.put("hibernate.show_sql", "false");
        emf = Persistence.createEntityManagerFactory("SallePU", testProperties);
        em = emf.createEntityManager();
        repository = new SalleRepositoryImpl(em);
        day = LocalDateTime.of(2024, 6, 1, 0, 0);

        Salle amphiA = new Salle("Amphi A", 80, "Bloc Nord", 0, "Amphithéâtre", true);
        Salle laboJava = new Salle("Labo Java", 20, "Bloc Tech", 2, "TP", true);
        Salle conseil = new Salle("Conseil", 30, "Administration", 1, "Réunion", false);
        Salle multimedia = new Salle("Multimédia", 40, "Médiathèque", 1, "Formation", true);
        laboJava.addReservation(new Reservation(day.withHour(9), day.withHour(11),
                "TP JPA", "Enseignant", 18, "CONFIRMEE"));
        conseil.addReservation(new Reservation(day.withHour(11), day.withHour(12),
                "Réunion", "Direction", 12, "PLANIFIEE"));

        em.getTransaction().begin();
        em.persist(amphiA);
        em.persist(laboJava);
        em.persist(conseil);
        em.persist(multimedia);
        em.getTransaction().commit();
    }

    @AfterAll
    static void tearDown() {
        em.close();
        emf.close();
    }

    @Test
    void findsRoomsWithoutOverlappingReservation() {
        List<String> names = names(repository.findAvailableRooms(day.withHour(9), day.withHour(11)));
        assertEquals(Arrays.asList("Amphi A", "Conseil", "Multimédia"), names);
    }

    @Test
    void acceptsReservationTouchingSearchBoundary() {
        List<String> names = names(repository.findAvailableRooms(day.withHour(8), day.withHour(9)));
        assertEquals(Arrays.asList("Amphi A", "Conseil", "Labo Java", "Multimédia"), names);
    }

    @Test
    void combinesCriteriaAndCountsWithTheSameFilters() {
        List<Salle> rooms = repository.searchRooms("i", 25,
                day.withHour(9), day.withHour(11), 1, 10);
        long count = repository.countRooms("i", 25, day.withHour(9), day.withHour(11));

        assertEquals(Arrays.asList("Amphi A", "Conseil", "Multimédia"), names(rooms));
        assertEquals(3, count);
    }

    @Test
    void paginatesResultsUsingOneBasedPageNumbers() {
        assertEquals(Arrays.asList("Amphi A", "Conseil"),
                names(repository.searchRooms(null, null, null, null, 1, 2)));
        assertEquals(Arrays.asList("Labo Java", "Multimédia"),
                names(repository.searchRooms(null, null, null, null, 2, 2)));
    }

    @Test
    void rejectsIncompleteOrInvalidIntervals() {
        assertThrows(IllegalArgumentException.class,
                () -> repository.searchRooms(null, null, day, null, 1, 10));
        assertThrows(IllegalArgumentException.class,
                () -> repository.findAvailableRooms(day.withHour(11), day.withHour(9)));
    }

    private static List<String> names(List<Salle> rooms) {
        return rooms.stream().map(Salle::getNom).collect(Collectors.toList());
    }
}
