package ar.edu.utn.frsf.gestionevaluaciones.interfaces;

/**
 * Usuario que valida sus propias credenciales (diagrama de clases, "Servicios externos, interfaces y patrones").
 * La implementa Candidato (CU001 3.A.2). El Consultor no la implementa: lo valida el LDAP de la organización.
 */
public interface IAutenticable {

    boolean validarCredenciales(String clave);
}
