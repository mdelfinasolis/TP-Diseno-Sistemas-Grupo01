package ar.edu.utn.frsf.gestionevaluaciones.dao;

import java.util.List;

/**
 * Interfaz genérica de persistencia (diagrama de clases, Capa de Persistencia).
 *
 * @param <T>  entidad persistida
 * @param <ID> tipo del identificador de la entidad
 */
public interface IDAO<T, ID> {

    void guardar(T entidad);

    void modificar(T entidad);

    void eliminarLogico(ID id);

    T buscarPorId(ID id);

    List<T> buscarTodos();
}
