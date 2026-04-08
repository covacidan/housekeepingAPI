package housekeeping.tineretului.service;

import housekeeping.tineretului.dto.IndexTotal;
import housekeeping.tineretului.model.IndexApa;
import housekeeping.tineretului.repository.IndexApaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class IndexApaServiceImpl implements IndexApaService {

    private final IndexApaRepository indexApaRepository;

    public IndexApaServiceImpl(IndexApaRepository indexApaRepository) {
        this.indexApaRepository = indexApaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByMonthAndYear(int month, int year) {
        return indexApaRepository.findByMonthAndYear(month, year).isPresent();
    }

    @Override
    public IndexApa createIndexApa(IndexApa indexApa) {
        return indexApaRepository.save(indexApa);
    }

    @Override
    public IndexApa updateIndexApa(IndexApa indexApa) {
        if (!indexApaRepository.existsById(indexApa.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
        }
        return indexApaRepository.save(indexApa);
    }

    @Override
    public void deleteIndexApa(Long id) {
        if (!indexApaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
        }
        indexApaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IndexApa> findAll() {
        return indexApaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public IndexTotal getTotal() {
        List<IndexApa> listaApa = indexApaRepository.findAll();
        if (listaApa.size() < 2) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "At least 2 records are required to calculate totals");
        }
        Collections.sort(listaApa);

        IndexApa current = listaApa.get(listaApa.size() - 1);
        IndexApa previous = listaApa.get(listaApa.size() - 2);

        double bucatarieRece = current.getBucatarieRece() - previous.getBucatarieRece();
        double bucatarieCald = current.getBucatarieCald() - previous.getBucatarieCald();
        double baieRece = current.getBaieRece() - previous.getBaieRece();
        double baieCald = current.getBaieCald() - previous.getBaieCald();
        double baieServRece = current.getBaieServiciuRece() - previous.getBaieServiciuRece();
        double baieServCald = current.getBaieServiciuCald() - previous.getBaieServiciuCald();

        IndexTotal indexTotal = new IndexTotal();
        indexTotal.setBucatarieRece(round(bucatarieRece, 3));
        indexTotal.setBucatarieCald(round(bucatarieCald, 3));
        indexTotal.setBaieRece(round(baieRece, 3));
        indexTotal.setBaieCald(round(baieCald, 3));
        indexTotal.setBaieServiciuRece(round(baieServRece, 3));
        indexTotal.setBaieServiciuCald(round(baieServCald, 3));
        indexTotal.setTotalRece(round(bucatarieRece + baieRece + baieServRece, 3));
        indexTotal.setTotalCald(round(bucatarieCald + baieCald + baieServCald, 3));

        return indexTotal;
    }

    private static double round(double value, int places) {
        long factor = (long) Math.pow(10, places);
        return (double) Math.round(value * factor) / factor;
    }
}
