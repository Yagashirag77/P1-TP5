package com.example.repository;

import com.example.model.Reservation;
import com.example.model.Salle;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Stateless
public class SalleRepositoryImpl implements SalleRepository {

    @PersistenceContext(unitName = "SallePU")
    private EntityManager entityManager;

    public SalleRepositoryImpl() {
    }

    public SalleRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        validateInterval(debut, fin);
        String jpql = "SELECT s FROM Salle s WHERE s.id NOT IN (" +
                "SELECT r.salle.id FROM Reservation r " +
                "WHERE :debut < r.fin AND :fin > r.debut) ORDER BY s.nom";

        return entityManager.createQuery(jpql, Salle.class)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getResultList();
    }

    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin,
                                   LocalDateTime debut, LocalDateTime fin,
                                   int page, int size) {
        validateSearch(debut, fin, page, size);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Salle> cq = cb.createQuery(Salle.class);
        Root<Salle> salle = cq.from(Salle.class);
        List<Predicate> predicates = buildPredicates(cb, cq, salle, nom, capaciteMin, debut, fin);

        cq.select(salle)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.asc(salle.get("nom")));

        TypedQuery<Salle> query = entityManager.createQuery(cq);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    @Override
    public long countRooms(String nom, Integer capaciteMin,
                           LocalDateTime debut, LocalDateTime fin) {
        validateOptionalInterval(debut, fin);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Salle> salle = cq.from(Salle.class);
        List<Predicate> predicates = buildPredicates(cb, cq, salle, nom, capaciteMin, debut, fin);

        cq.select(cb.count(salle)).where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(cq).getSingleResult();
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb,
                                            CommonAbstractCriteria owner,
                                            Root<Salle> salle,
                                            String nom,
                                            Integer capaciteMin,
                                            LocalDateTime debut,
                                            LocalDateTime fin) {
        List<Predicate> predicates = new ArrayList<>();

        if (nom != null && !nom.trim().isEmpty()) {
            String motif = "%" + nom.trim().toLowerCase(Locale.ROOT) + "%";
            predicates.add(cb.like(cb.lower(salle.get("nom")), motif));
        }
        if (capaciteMin != null) {
            if (capaciteMin < 0) {
                throw new IllegalArgumentException("La capacité minimale ne peut pas être négative");
            }
            predicates.add(cb.ge(salle.get("capacite"), capaciteMin));
        }
        if (debut != null && fin != null) {
            Subquery<Long> subquery = owner.subquery(Long.class);
            Root<Reservation> reservation = subquery.from(Reservation.class);
            subquery.select(reservation.get("salle").get("id"));
            subquery.where(cb.and(
                    cb.lessThan(reservation.get("debut"), fin),
                    cb.greaterThan(reservation.get("fin"), debut)
            ));
            predicates.add(cb.not(salle.get("id").in(subquery)));
        }
        return predicates;
    }

    private void validateSearch(LocalDateTime debut, LocalDateTime fin, int page, int size) {
        validateOptionalInterval(debut, fin);
        if (page < 1) {
            throw new IllegalArgumentException("Le numéro de page doit être supérieur ou égal à 1");
        }
        if (size < 1) {
            throw new IllegalArgumentException("La taille de page doit être supérieure ou égale à 1");
        }
    }

    private void validateOptionalInterval(LocalDateTime debut, LocalDateTime fin) {
        if ((debut == null) != (fin == null)) {
            throw new IllegalArgumentException("Le début et la fin doivent être fournis ensemble");
        }
        if (debut != null) {
            validateInterval(debut, fin);
        }
    }

    private void validateInterval(LocalDateTime debut, LocalDateTime fin) {
        if (debut == null || fin == null || !debut.isBefore(fin)) {
            throw new IllegalArgumentException("Le début doit être strictement antérieur à la fin");
        }
    }
}
