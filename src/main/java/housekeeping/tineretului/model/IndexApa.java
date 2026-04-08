package housekeeping.tineretului.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "index_apa", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"an", "luna"})
})
public class IndexApa implements Comparable<IndexApa> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "an", nullable = false)
    private int an;

    @Column(name = "luna", nullable = false)
    private int luna;

    @Column(name = "bucatarie_rece")
    private Double bucatarieRece;

    @Column(name = "bucatarie_cald")
    private Double bucatarieCald;

    @Column(name = "baie_rece")
    private Double baieRece;

    @Column(name = "baie_cald")
    private Double baieCald;

    @Column(name = "baie_serviciu_rece")
    private Double baieServiciuRece;

    @Column(name = "baie_serviciu_cald")
    private Double baieServiciuCald;

    @Column(name = "added_date")
    private LocalDate addedDate;

    @Override
    public int compareTo(IndexApa ia) {
        if (this.getAn() != ia.getAn()) {
            return Integer.compare(this.getAn(), ia.getAn());
        }
        return Integer.compare(this.getLuna(), ia.getLuna());
    }
}
