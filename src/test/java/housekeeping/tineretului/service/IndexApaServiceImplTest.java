package housekeeping.tineretului.service;

import housekeeping.tineretului.dto.IndexTotal;
import housekeeping.tineretului.model.IndexApa;
import housekeeping.tineretului.repository.IndexApaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexApaServiceImplTest {

    @Mock
    private IndexApaRepository indexApaRepository;

    @InjectMocks
    private IndexApaServiceImpl service;

    private IndexApa buildRecord(long id, int year, int month, double val) {
        IndexApa r = new IndexApa();
        r.setId(id);
        r.setAn(year);
        r.setLuna(month);
        r.setBucatarieRece(val);
        r.setBucatarieCald(val);
        r.setBaieRece(val);
        r.setBaieCald(val);
        r.setBaieServiciuRece(val);
        r.setBaieServiciuCald(val);
        return r;
    }

    @Test
    void existsByMonthAndYear_returnsTrueWhenFound() {
        when(indexApaRepository.findByMonthAndYear(3, 2025)).thenReturn(Optional.of(new IndexApa()));
        assertThat(service.existsByMonthAndYear(3, 2025)).isTrue();
    }

    @Test
    void existsByMonthAndYear_returnsFalseWhenNotFound() {
        when(indexApaRepository.findByMonthAndYear(3, 2025)).thenReturn(Optional.empty());
        assertThat(service.existsByMonthAndYear(3, 2025)).isFalse();
    }

    @Test
    void createIndexApa_savesAndReturnsRecord() {
        IndexApa input = buildRecord(0, 2025, 3, 100.0);
        IndexApa saved = buildRecord(1, 2025, 3, 100.0);
        when(indexApaRepository.save(input)).thenReturn(saved);

        IndexApa result = service.createIndexApa(input);

        assertThat(result.getId()).isEqualTo(1L);
        verify(indexApaRepository).save(input);
    }

    @Test
    void updateIndexApa_throwsWhenNotFound() {
        IndexApa input = buildRecord(99, 2025, 3, 100.0);
        when(indexApaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.updateIndexApa(input))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateIndexApa_savesWhenFound() {
        IndexApa input = buildRecord(1, 2025, 3, 100.0);
        when(indexApaRepository.existsById(1L)).thenReturn(true);
        when(indexApaRepository.save(input)).thenReturn(input);

        IndexApa result = service.updateIndexApa(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void deleteIndexApa_throwsWhenNotFound() {
        when(indexApaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteIndexApa(99L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deleteIndexApa_deletesWhenFound() {
        when(indexApaRepository.existsById(1L)).thenReturn(true);

        service.deleteIndexApa(1L);

        verify(indexApaRepository).deleteById(1L);
    }

    @Test
    void getTotal_throwsWhenFewerThanTwoRecords() {
        when(indexApaRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.getTotal())
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getTotal_calculatesCorrectDeltas() {
        IndexApa prev = buildRecord(1, 2025, 2, 100.0);
        IndexApa curr = buildRecord(2, 2025, 3, 110.0);
        when(indexApaRepository.findAll()).thenReturn(new ArrayList<>(List.of(prev, curr)));

        IndexTotal total = service.getTotal();

        assertThat(total.getBucatarieRece()).isEqualTo(10.0);
        assertThat(total.getBaieRece()).isEqualTo(10.0);
        assertThat(total.getTotalRece()).isEqualTo(30.0);
        assertThat(total.getTotalCald()).isEqualTo(30.0);
    }

    @Test
    void getTotal_sortsRecordsByYearAndMonth() {
        // Records out of order — service must sort before computing delta
        IndexApa march = buildRecord(2, 2025, 3, 110.0);
        IndexApa feb   = buildRecord(1, 2025, 2, 100.0);
        when(indexApaRepository.findAll()).thenReturn(new ArrayList<>(List.of(march, feb)));

        IndexTotal total = service.getTotal();

        assertThat(total.getBucatarieRece()).isEqualTo(10.0);
    }
}
