package ar.edu.utn.frsf.gestionevaluaciones.dao;

import ar.edu.utn.frsf.gestionevaluaciones.entity.Candidato;
import ar.edu.utn.frsf.gestionevaluaciones.entity.TIPODOCUMENTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CandidatoDAO implements IDAO<Candidato, Integer> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void guardar(Candidato entidad) {
        em.persist(entidad);
    }

    @Override
    public void modificar(Candidato entidad) {
        em.merge(entidad);
    }

    @Override
    public void eliminarLogico(Integer nroCandidato) {
        Candidato candidato = em.find(Candidato.class, nroCandidato);
        if (candidato != null) {
            candidato.setEliminado(true);
        }
    }

    @Override
    public Candidato buscarPorId(Integer nroCandidato) {
        return em.find(Candidato.class, nroCandidato);
    }

    @Override
    public List<Candidato> buscarTodos() {
        return buscarPorFiltros(null, null, null);
    }

    /**
     * Candidatos no eliminados. Apellido y nombre con criterio "contiene"; número de candidato por igualdad.
     * Los filtros vacíos no se aplican.
     */
    public List<Candidato> buscarPorFiltros(String apellido, String nombre, Integer nroCandidato) {
        StringBuilder jpql = new StringBuilder("SELECT c FROM Candidato c WHERE c.eliminado = false");
        if (tieneValor(apellido)) {
            jpql.append(" AND UPPER(c.apellido) LIKE :apellido");
        }
        if (tieneValor(nombre)) {
            jpql.append(" AND UPPER(c.nombre) LIKE :nombre");
        }
        if (nroCandidato != null) {
            jpql.append(" AND c.nroCandidato = :nroCandidato");
        }
        jpql.append(" ORDER BY c.apellido, c.nombre, c.nroCandidato");

        TypedQuery<Candidato> query = em.createQuery(jpql.toString(), Candidato.class);
        if (tieneValor(apellido)) {
            query.setParameter("apellido", "%" + apellido.toUpperCase() + "%");
        }
        if (tieneValor(nombre)) {
            query.setParameter("nombre", "%" + nombre.toUpperCase() + "%");
        }
        if (nroCandidato != null) {
            query.setParameter("nroCandidato", nroCandidato);
        }
        return query.getResultList();
    }

    public Candidato buscarPorTipoYNroDocumento(TIPODOCUMENTO tipo, String nroDoc) {
        return em.createQuery("SELECT c FROM Candidato c WHERE c.tipoDocumento = :tipo AND c.nroDocumento = :nro",
                        Candidato.class)
                .setParameter("tipo", tipo)
                .setParameter("nro", nroDoc)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private static boolean tieneValor(String valor) {
        return valor != null && !valor.isBlank();
    }
}
