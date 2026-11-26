package ar.edu.utn.frsf.gestionevaluaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "puesto")
public class Puesto {

    @Id
    @Column(name = "codigo_puesto", length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "eliminado", nullable = false)
    private boolean eliminado;

    /** Empresa "1" -- "0..*" Puesto : solicita */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    /** Puesto "1" *-- "1..*" PuestoCompetencia : tiene */
    @OneToMany(mappedBy = "puesto")
    private List<PuestoCompetencia> competencias = new ArrayList<>();

    protected Puesto() {
    }

    public List<PuestoCompetencia> getCompetencias() {
        return competencias;
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

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public Empresa getEmpresa() {
        return empresa;
    }
}
