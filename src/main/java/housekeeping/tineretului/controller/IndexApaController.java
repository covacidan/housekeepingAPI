package housekeeping.tineretului.controller;

import housekeeping.tineretului.model.IndexApa;
import housekeeping.tineretului.service.IndexApaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/housekeeping/waterIndex")
public class IndexApaController {

    private final IndexApaService indexApaService;

    public IndexApaController(IndexApaService indexApaService) {
        this.indexApaService = indexApaService;
    }

    @PostMapping
    public ResponseEntity<IndexApa> addIndexApa(@RequestBody IndexApa indexApa) {
        if (indexApaService.existsByMonthAndYear(indexApa.getLuna(), indexApa.getAn())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(indexApaService.createIndexApa(indexApa), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<IndexApa> updateIndexApa(@RequestBody IndexApa indexApa) {
        return new ResponseEntity<>(indexApaService.updateIndexApa(indexApa), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndexApa(@PathVariable Long id) {
        indexApaService.deleteIndexApa(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<?> getAllIndexes() {
        return new ResponseEntity<>(indexApaService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/total")
    public ResponseEntity<?> getTotal() {
        return new ResponseEntity<>(indexApaService.getTotal(), HttpStatus.OK);
    }
}
