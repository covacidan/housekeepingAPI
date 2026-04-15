package housekeeping.tineretului.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import housekeeping.tineretului.dto.IndexTotal;
import housekeeping.tineretului.model.IndexApa;
import housekeeping.tineretului.service.IndexApaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = IndexApaController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
    }
)
class IndexApaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IndexApaService indexApaService;

    private IndexApa sampleRecord() {
        IndexApa r = new IndexApa();
        r.setId(1L);
        r.setAn(2025);
        r.setLuna(3);
        r.setBucatarieRece(100.0);
        r.setBucatarieCald(50.0);
        r.setBaieRece(80.0);
        r.setBaieCald(40.0);
        r.setBaieServiciuRece(20.0);
        r.setBaieServiciuCald(10.0);
        return r;
    }

    @Test
    void getAll_returnsListOfRecords() throws Exception {
        when(indexApaService.findAll()).thenReturn(List.of(sampleRecord()));

        mockMvc.perform(get("/housekeeping/waterIndex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].an").value(2025))
                .andExpect(jsonPath("$[0].luna").value(3));
    }

    @Test
    void addRecord_returns201WhenNew() throws Exception {
        IndexApa input = sampleRecord();
        input.setId(null);

        when(indexApaService.existsByMonthAndYear(3, 2025)).thenReturn(false);
        when(indexApaService.createIndexApa(any())).thenReturn(sampleRecord());

        mockMvc.perform(post("/housekeeping/waterIndex")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addRecord_returns409WhenDuplicate() throws Exception {
        when(indexApaService.existsByMonthAndYear(anyInt(), anyInt())).thenReturn(true);

        mockMvc.perform(post("/housekeeping/waterIndex")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRecord())))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteRecord_returns204() throws Exception {
        doNothing().when(indexApaService).deleteIndexApa(1L);

        mockMvc.perform(delete("/housekeeping/waterIndex/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getTotal_returnsIndexTotal() throws Exception {
        IndexTotal total = new IndexTotal();
        total.setTotalRece(30.0);
        total.setTotalCald(15.0);
        when(indexApaService.getTotal()).thenReturn(total);

        mockMvc.perform(get("/housekeeping/waterIndex/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRece").value(30.0))
                .andExpect(jsonPath("$.totalCald").value(15.0));
    }
}
