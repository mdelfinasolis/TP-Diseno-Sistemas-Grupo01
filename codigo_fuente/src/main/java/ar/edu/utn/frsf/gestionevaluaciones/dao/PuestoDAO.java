package ar.edu.utn.frsf.gestionevaluaciones.dao;

import ar.edu.utn.frsf.gestionevaluaciones.entity.Puesto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PuestoDAO implements IDAO<Puesto, String> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void guardar(Puesto entidad) {
        em.persist(entidad);
    }

    @Override
    public void modificar(Puesto entidad) {
        em.merge(entidad);
    }

    @Override
    public void eliminarLogico(String codigo) {
        Puesto puesto = em.find(Puesto.class, codigo);
        if (puesto != null) {
            puesto.setEliminado(true);
        }
    }

    @Override
    public Puesto buscarPorId(String codigo) {
        return em.find(Puesto.class, codigo);
    }

    @Override
    public List<Puesto> buscarTodos() {
        return em.createQuery("SELECT p FROM Puesto p JOIN FETCH p.empresa ORDER BY p.nombre", Puesto.class)
                .getResultList();
    }

    /**
     * Puestos no eliminados. codigo con criterio "contiene"; nombre y empresa por igualdad
     * (vienen de listas desplegables). Los filtros vacíos no se aplican.
     */
    public List<Puesto> buscarPorFiltros(String codigo, String nombre, String empresa) {
        StringBuilder jpql = new StringBuilder(
                "SELECT p FROM Puesto p JOIN FETCH p.empresa e WHERE p.eliminado = false");
        if (tieneValor(codigo)) {
            jpql.append(" AND UPPER(p.codigo) LIKE :codigo");
        }
        if (tieneValor(nombre)) {
            jpql.append(" AND p.nombre = :nombre");
        }
        if (tieneValor(empresa)) {
            jpql.append(" AND e.nombre = :empresa");
        }
        jpql.append(" ORDER BY p.codigo");

        TypedQuery<Puesto> query = em.createQuery(jpql.toString(), Puesto.class);
        if (tieneValor(codigo)) {
            query.setParameter("codigo", "%" + codigo.toUpperCase() + "%");
        }
        if (tieneValor(nombre)) {
            query.setParameter("nombre", nombre);
        }
        if (tieneValor(empresa)) {
            query.setParameter("empresa", empresa);
        }
        return query.getResultList();
    }

    private static boolean tieneValor(String valor) {
        return valor != null && !valor.isBlank();
    }
}
