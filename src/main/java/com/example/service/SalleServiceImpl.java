package com.example.service;

import com.example.model.Salle;
import com.example.repository.SalleRepository;
import com.example.repository.SalleRepositoryImpl;
import com.example.util.PaginationResult;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class SalleServiceImpl implements SalleService {

    @EJB
    private SalleRepository repository;

    public SalleServiceImpl() {
    }

    public SalleServiceImpl(SalleRepository repository) {
        this.repository = repository;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.repository = new SalleRepositoryImpl(entityManager);
    }

    @Override
    public List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        return repository.findAvailableRooms(debut, fin);
    }

    @Override
    public PaginationResult<Salle> searchRooms(String nom, Integer capaciteMin,
                                               LocalDateTime debut, LocalDateTime fin,
                                               int page, int size) {
        List<Salle> items = repository.searchRooms(nom, capaciteMin, debut, fin, page, size);
        long totalItems = repository.countRooms(nom, capaciteMin, debut, fin);
        return new PaginationResult<>(items, page, size, totalItems);
    }
}
