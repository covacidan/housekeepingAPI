package housekeeping.tineretului.dto;

import java.time.LocalDate;

public class IndexApaRequest {

    private Long id;
    private int an;
    private int luna;
    private Double bucatarieRece;
    private Double bucatarieCald;
    private Double baieRece;
    private Double baieCald;
    private Double baieServiciuRece;
    private Double baieServiciuCald;
    private LocalDate addedDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getAn() { return an; }
    public void setAn(int an) { this.an = an; }

    public int getLuna() { return luna; }
    public void setLuna(int luna) { this.luna = luna; }

    public Double getBucatarieRece() { return bucatarieRece; }
    public void setBucatarieRece(Double bucatarieRece) { this.bucatarieRece = bucatarieRece; }

    public Double getBucatarieCald() { return bucatarieCald; }
    public void setBucatarieCald(Double bucatarieCald) { this.bucatarieCald = bucatarieCald; }

    public Double getBaieRece() { return baieRece; }
    public void setBaieRece(Double baieRece) { this.baieRece = baieRece; }

    public Double getBaieCald() { return baieCald; }
    public void setBaieCald(Double baieCald) { this.baieCald = baieCald; }

    public Double getBaieServiciuRece() { return baieServiciuRece; }
    public void setBaieServiciuRece(Double baieServiciuRece) { this.baieServiciuRece = baieServiciuRece; }

    public Double getBaieServiciuCald() { return baieServiciuCald; }
    public void setBaieServiciuCald(Double baieServiciuCald) { this.baieServiciuCald = baieServiciuCald; }

    public LocalDate getAddedDate() { return addedDate; }
    public void setAddedDate(LocalDate addedDate) { this.addedDate = addedDate; }
}
