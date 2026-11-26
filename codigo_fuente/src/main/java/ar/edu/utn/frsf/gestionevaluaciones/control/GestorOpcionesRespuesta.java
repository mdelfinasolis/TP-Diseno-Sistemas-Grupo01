package ar.edu.utn.frsf.gestionevaluaciones.control;

import ar.edu.utn.frsf.gestionevaluaciones.dao.OpcionRespuestaDAO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.ItemOpcionDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.OpcionRespuestaDetalleDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.OpcionRespuestaListadoDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.OpcionRespuesta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GestorOpcionesRespuesta {

    private final OpcionRespuestaDAO opcionRespuestaDAO;

    public GestorOpcionesRespuesta(OpcionRespuestaDAO opcionRespuestaDAO) {
        this.opcionRespuestaDAO = opcionRespuestaDAO;
    }

    /** CU021: carga de la lista de opciones de respuesta (solo las no eliminadas). */
    public List<OpcionRespuestaListadoDTO> listarOpciones() {
        return opcionRespuestaDAO.buscarTodos().stream()
                .filter(o -> !o.isEliminada())
                .map(o -> new OpcionRespuestaListadoDTO(o.getNombre(), o.getDescripcion()))
                .toList();
    }

    /** CU021: ítems de la opción seleccionada, ordenados por orden, para la grilla de ponderaciones. */
    public OpcionRespuestaDetalleDTO obtenerDetalleOpcion(String nombre) {
        OpcionRespuesta opcion = opcionRespuestaDAO.buscarPorId(nombre);
        if (opcion == null || opcion.isEliminada()) {
            return null;
        }
        List<ItemOpcionDTO> items = opcion.getItems().stream()
                .map(i -> new ItemOpcionDTO(i.getOrden(), i.getDescripcion()))
                .toList();
        return new OpcionRespuestaDetalleDTO(opcion.getNombre(), opcion.getDescripcion(), items);
    }

    public OpcionRespuesta obtenerOpcionRespuesta(String nombre) {
        return nombre == null ? null : opcionRespuestaDAO.buscarPorId(nombre);
    }
}
