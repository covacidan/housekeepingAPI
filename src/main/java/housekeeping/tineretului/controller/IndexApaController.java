package housekeeping.tineretului.controller;

import housekeeping.tineretului.dto.IndexApaRequest;
import housekeeping.tineretului.dto.IndexTotal;
import housekeeping.tineretului.model.IndexApa;
import housekeeping.tineretului.service.IndexApaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/housekeeping/waterIndex")
public class IndexApaController {

    private final IndexApaService indexApaService;

    public IndexApaController(IndexApaService indexApaService) {
        this.indexApaService = indexApaService;
    }

    @PostMapping
    public ResponseEntity<IndexApa> addIndexApa(@RequestBody IndexApaRequest request) {
        if (indexApaService.existsByMonthAndYear(request.getLuna(), request.getAn())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(indexApaService.createIndexApa(toEntity(request)), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<IndexApa> updateIndexApa(@RequestBody IndexApaRequest request) {
        return new ResponseEntity<>(indexApaService.updateIndexApa(toEntity(request)), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndexApa(@PathVariable Long id) {
        indexApaService.deleteIndexApa(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<List<IndexApa>> getAllIndexes() {
        return new ResponseEntity<>(indexApaService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/total")
    public ResponseEntity<IndexTotal> getTotal() {
        return new ResponseEntity<>(indexApaService.getTotal(), HttpStatus.OK);
    }

    private IndexApa toEntity(IndexApaRequest request) {
        IndexApa entity = new IndexApa();
        entity.setId(request.getId());
        entity.setAn(request.getAn());
        entity.setLuna(request.getLuna());
        entity.setBucatarieRece(request.getBucatarieRece());
        entity.setBucatarieCald(request.getBucatarieCald());
        entity.setBaieRece(request.getBaieRece());
        entity.setBaieCald(request.getBaieCald());
        entity.setBaieServiciuRece(request.getBaieServiciuRece());
        entity.setBaieServiciuCald(request.getBaieServiciuCald());
        entity.setAddedDate(request.getAddedDate());
        return entity;
    }
}
