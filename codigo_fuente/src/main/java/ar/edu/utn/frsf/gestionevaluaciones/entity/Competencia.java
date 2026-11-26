package ar.edu.utn.frsf.gestionevaluaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "competencia")
public class Competencia {

    @Id
    @Column(name = "codigo_competencia", length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "tipo", length = 50)
    private String tipo;

    @Column(name = "eliminada", nullable = false)
    private boolean eliminada;

    /** Competencia "1" *-- "1..*" Factor : compone */
    @OneToMany(mappedBy = "competencia")
    @OrderBy("nroOrden")
    private List<Factor> factores = new ArrayList<>();

    protected Competencia() {
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public boolean isEliminada() {
        return eliminada;
    }

    public void setEliminada(boolean eliminada) {
        this.eliminada = eliminada;
    }

    public List<Factor> getFactores() {
        return factores;
    }
}
