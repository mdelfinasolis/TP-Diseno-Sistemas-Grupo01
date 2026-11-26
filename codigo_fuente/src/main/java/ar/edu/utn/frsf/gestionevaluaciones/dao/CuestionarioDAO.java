package ar.edu.utn.frsf.gestionevaluaciones.dao;

import ar.edu.utn.frsf.gestionevaluaciones.entity.Cuestionario;
import ar.edu.utn.frsf.gestionevaluaciones.entity.ESTADOCUESTIONARIO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CuestionarioDAO implements IDAO<Cuestionario, String> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void guardar(Cuestionario entidad) {
        em.persist(entidad);
    }

    @Override
    public void modificar(Cuestionario entidad) {
        em.merge(entidad);
    }

    /** Cuestionario no tiene atributo de baja lógica en el diagrama de clases ni en el DER. */
    @Override
    public void eliminarLogico(String clave) {
        throw new UnsupportedOperationException("Cuestionario no admite baja lógica en el modelo actual");
    }

    @Override
    public Cuestionario buscarPorId(String clave) {
        return em.find(Cuestionario.class, clave);
    }

    @Override
    public List<Cuestionario> buscarTodos() {
        return em.createQuery("SELECT c FROM Cuestionario c", Cuestionario.class).getResultList();
    }

    public Cuestionario buscarPorClave(String clave) {
        if (clave == null || clave.isBlank()) {
            return null;
        }
        return em.find(Cuestionario.class, clave.trim().toUpperCase());
    }

    /** Cuestionarios ACTIVO o EN_PROCESO de los candidatos indicados (un candidato puede tener uno solo). */
    public List<Cuestionario> buscarActivosOEnProcesoPorCandidatos(List<Integer> nrosCandidatos) {
        if (nrosCandidatos == null || nrosCandidatos.isEmpty()) {
            return List.of();
        }
        return em.createQuery("SELECT c FROM Cuestionario c JOIN FETCH c.candidato cand"
                        + " WHERE cand.nroCandidato IN :nros AND c.detalle.estado IN :estados"
                        + " ORDER BY cand.apellido, cand.nombre", Cuestionario.class)
                .setParameter("nros", nrosCandidatos)
                .setParameter("estados", List.of(ESTADOCUESTIONARIO.ACTIVO, ESTADOCUESTIONARIO.EN_PROCESO))
                .getResultList();
    }

    /** Cuestionarios generados por una evaluación, con su candidato. */
    public List<Cuestionario> buscarPorEvaluacion(int idEvaluacion) {
        return em.createQuery(
                        "SELECT c FROM Cuestionario c JOIN FETCH c.candidato"
                                + " WHERE c.evaluacion.id = :idEvaluacion", Cuestionario.class)
                .setParameter("idEvaluacion", idEvaluacion)
                .getResultList();
    }
}
