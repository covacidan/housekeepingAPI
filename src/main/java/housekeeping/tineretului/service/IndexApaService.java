package housekeeping.tineretului.service;

import housekeeping.tineretului.dto.IndexTotal;
import housekeeping.tineretului.model.IndexApa;

import java.util.List;

public interface IndexApaService {
    boolean existsByMonthAndYear(int month, int year);
    IndexApa createIndexApa(IndexApa indexApa);
    IndexApa updateIndexApa(IndexApa indexApa);
    void deleteIndexApa(Long id);
    List<IndexApa> findAll();
    IndexTotal getTotal();
}
