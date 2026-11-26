package ar.edu.utn.frsf.gestionevaluaciones.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pregunta")
public class Pregunta {

    @Id
    @Column(name = "codigo_pregunta", length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "enunciado", nullable = false, length = 500)
    private String enunciado;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "eliminada", nullable = false)
    private boolean eliminada;

    /** Factor "1" *-- "0..*" Pregunta : contiene */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_factor", nullable = false)
    private Factor factor;

    /** Pregunta "0..*" --> "1" OpcionRespuesta : usa */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nombre_opcion_respuesta", nullable = false)
    private OpcionRespuesta opcionRespuesta;

    /** (Pregunta, ItemOpcion) .. PreguntaItemPonderacion */
    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PreguntaItemPonderacion> ponderaciones = new ArrayList<>();

    protected Pregunta() {
    }

    public void agregarPonderacion(PreguntaItemPonderacion ponderacion) {
        ponderaciones.add(ponderacion);
    }

    /** Ponderación que otorga el ítem elegido para esta pregunta (0 si el ítem no tiene ponderación). */
    public double getPonderacion(ItemOpcion item) {
        return ponderaciones.stream()
                .filter(p -> p.getItem().getOrden() == item.getOrden()
                        && p.getItem().getOpcionRespuesta().getNombre().equals(item.getOpcionRespuesta().getNombre()))
                .mapToDouble(PreguntaItemPonderacion::getPonderacion)
                .findFirst()
                .orElse(0.0);
    }

    public List<PreguntaItemPonderacion> getPonderaciones() {
        return ponderaciones;
    }

    /**
     * El código no se ingresa en la pantalla de alta (el constructor del diagrama no lo recibe):
     * lo asigna PreguntaDAO al guardar.
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Pregunta(String nombre, String enunciado, String descripcion, Factor factor, OpcionRespuesta opcionRespuesta) {
        this.nombre = nombre;
        this.enunciado = enunciado;
        this.descripcion = descripcion;
        this.factor = factor;
        this.opcionRespuesta = opcionRespuesta;
    }

    public Factor getFactor() {
        return factor;
    }

    public OpcionRespuesta getOpcionRespuesta() {
        return opcionRespuesta;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isEliminada() {
        return eliminada;
    }

    public void setEliminada(boolean eliminada) {
        this.eliminada = eliminada;
    }
}
