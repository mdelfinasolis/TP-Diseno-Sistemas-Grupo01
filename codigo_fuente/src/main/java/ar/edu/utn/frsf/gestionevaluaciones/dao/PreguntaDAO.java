package ar.edu.utn.frsf.gestionevaluaciones.dao;

import ar.edu.utn.frsf.gestionevaluaciones.entity.Pregunta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PreguntaDAO implements IDAO<Pregunta, String> {

    @PersistenceContext
    private EntityManager em;

    /** Si la pregunta no tiene código, se le asigna el siguiente disponible (P001, P002, ...). */
    @Override
    public void guardar(Pregunta entidad) {
        if (entidad.getCodigo() == null) {
            entidad.setCodigo(siguienteCodigo());
        }
        em.persist(entidad);
    }

    private String siguienteCodigo() {
        int maximo = em.createQuery("SELECT p.codigo FROM Pregunta p WHERE p.codigo LIKE 'P%'", String.class)
                .getResultStream()
                .map(codigo -> codigo.substring(1))
                .filter(numero -> numero.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);
        return String.format("P%03d", maximo + 1);
    }

    /** Preguntas vigentes de un factor. */
    public List<Pregunta> buscarPorFactor(String factor) {
        return em.createQuery("SELECT p FROM Pregunta p WHERE p.factor.codigo = :factor AND p.eliminada = false"
                        + " ORDER BY p.codigo", Pregunta.class)
                .setParameter("factor", factor)
                .getResultList();
    }

    /**
     * CU021 2.C: ya existe en el factor una pregunta vigente con el mismo nombre o con el mismo texto
     * (la validación contempla tanto el nombre como el contenido de la pregunta).
     */
    public boolean existeEnFactor(String factor, String nombre, String enunciado) {
        Long cantidad = em.createQuery("SELECT COUNT(p) FROM Pregunta p WHERE p.factor.codigo = :factor"
                        + " AND p.eliminada = false"
                        + " AND (UPPER(p.nombre) = :nombre OR UPPER(p.enunciado) = :enunciado)", Long.class)
                .setParameter("factor", factor)
                .setParameter("nombre", nombre == null ? "" : nombre.trim().toUpperCase())
                .setParameter("enunciado", enunciado == null ? "" : enunciado.trim().toUpperCase())
                .getSingleResult();
        return cantidad > 0;
    }

    /** Cantidad de preguntas vigentes por código de factor (los factores sin preguntas quedan en 0). */
    public Map<String, Integer> contarPorFactores(List<String> codigosFactor) {
        Map<String, Integer> cantidades = new HashMap<>();
        if (codigosFactor == null || codigosFactor.isEmpty()) {
            return cantidades;
        }
        codigosFactor.forEach(codigo -> cantidades.put(codigo, 0));
        List<Object[]> filas = em.createQuery("SELECT p.factor.codigo, COUNT(p) FROM Pregunta p"
                        + " WHERE p.factor.codigo IN :codigos AND p.eliminada = false"
                        + " GROUP BY p.factor.codigo", Object[].class)
                .setParameter("codigos", codigosFactor)
                .getResultList();
        for (Object[] fila : filas) {
            cantidades.put((String) fila[0], ((Number) fila[1]).intValue());
        }
        return cantidades;
    }

    @Override
    public void modificar(Pregunta entidad) {
        em.merge(entidad);
    }

    @Override
    public void eliminarLogico(String codigo) {
        Pregunta pregunta = em.find(Pregunta.class, codigo);
        if (pregunta != null) {
            pregunta.setEliminada(true);
        }
    }

    @Override
    public Pregunta buscarPorId(String codigo) {
        return em.find(Pregunta.class, codigo);
    }

    @Override
    public List<Pregunta> buscarTodos() {
        return buscarPorFiltros(null, null, null);
    }

    /**
     * Preguntas no eliminadas. competencia y factor son códigos (igualdad);
     * nombre se busca con criterio "contiene". Sin filtros devuelve todas.
     */
    public List<Pregunta> buscarPorFiltros(String competencia, String factor, String nombre) {
        StringBuilder jpql = new StringBuilder(
                "SELECT p FROM Pregunta p JOIN FETCH p.factor f JOIN FETCH f.competencia c"
                        + " WHERE p.eliminada = false");
        if (tieneValor(competencia)) {
            jpql.append(" AND c.codigo = :competencia");
        }
        if (tieneValor(factor)) {
            jpql.append(" AND f.codigo = :factor");
        }
        if (tieneValor(nombre)) {
            jpql.append(" AND UPPER(p.nombre) LIKE :nombre");
        }
        jpql.append(" ORDER BY c.nombre, f.nroOrden, p.nombre");

        TypedQuery<Pregunta> query = em.createQuery(jpql.toString(), Pregunta.class);
        if (tieneValor(competencia)) {
            query.setParameter("competencia", competencia);
        }
        if (tieneValor(factor)) {
            query.setParameter("factor", factor);
        }
        if (tieneValor(nombre)) {
            query.setParameter("nombre", "%" + nombre.toUpperCase() + "%");
        }
        return query.getResultList();
    }

    private static boolean tieneValor(String valor) {
        return valor != null && !valor.isBlank();
    }
}
