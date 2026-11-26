package ar.edu.utn.frsf.gestionevaluaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Usuario abstracto (diagrama de clases). En el DER no existe una tabla Usuario:
 * cada subclase (Consultor, Candidato) tiene su propia tabla, por eso se mapea
 * como superclase sin tabla. Cada subclase redefine la columna de "id".
 */
@MappedSuperclass
public abstract class Usuario {

    @Id
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    protected Usuario() {
    }

    protected Usuario(String nombre, String apellido) {
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }
}
