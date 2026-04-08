package housekeeping.tineretului.repository;

import housekeeping.tineretului.model.IndexApa;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexApaRepository extends CrudRepository<IndexApa, Long> {

    @Query("SELECT i FROM IndexApa i WHERE i.luna = :month AND i.an = :year")
    Optional<IndexApa> findByMonthAndYear(int month, int year);

    List<IndexApa> findAll();
}
