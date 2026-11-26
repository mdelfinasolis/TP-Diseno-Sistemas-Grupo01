package ar.edu.utn.frsf.gestionevaluaciones.boundary;

import ar.edu.utn.frsf.gestionevaluaciones.control.GestorAutenticacion;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.LoginCandidatoEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.LoginConsultorEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.SesionConsultorDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.TIPODOCUMENTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.beans.PropertyEditorSupport;
import java.util.Locale;

/**
 * CU001 - Autenticar usuario: pantalla inicial del sistema. El actor elige "Ingresar como usuario consultor"
 * (validación contra el LDAP) o "Realizar cuestionario" (documento y clave; luego se invoca CU026).
 */
@Controller
public class PantallaAutenticacion {

    static final String MENSAJE_CONSULTOR_INVALIDO = "El usuario o la contraseña no son válidos";
    static final String MENSAJE_CANDIDATO_INVALIDO = PantallaCompletarCuestionario.MENSAJE_DATOS_INVALIDOS;

    /** Atributo de sesión con el consultor autenticado (lo verifica InterceptorSesionConsultor). */
    public static final String SESION_CONSULTOR = "cu001.consultor";
    /** Marca que el candidato fue autenticado por CU001 antes de invocar CU026. */
    static final String CANDIDATO_AUTENTICADO = "cu001.candidatoAutenticado";

    private static final String TIPO_CONSULTOR = "consultor";
    private static final String TIPO_CANDIDATO = "candidato";

    private final GestorAutenticacion gestorAutenticacion;

    public PantallaAutenticacion(GestorAutenticacion gestorAutenticacion) {
        this.gestorAutenticacion = gestorAutenticacion;
    }

    /** El identificador de usuario se ingresa SIEMPRE en mayúsculas; la contraseña se respeta tal cual. */
    @InitBinder("datosConsultor")
    public void convertirUsuarioAMayusculas(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, "usuario", editorMayusculas());
    }

    /** Candidato: tipo y número de documento y clave, en mayúsculas (igual que en CU026). */
    @InitBinder("datos")
    public void convertirAMayusculas(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, editorMayusculas());
    }

    // =====================================================================
    // Pasos 1 a 3: selección del tipo de acceso
    // =====================================================================

    @GetMapping("/")
    public String seleccionarTipoAcceso(@RequestParam(required = false) String tipo, Model model, HttpSession session) {
        if (tipo == null && session.getAttribute(SESION_CONSULTOR) != null) {
            return "redirect:/menu"; // ya hay un consultor con la sesión iniciada
        }
        if (TIPO_CANDIDATO.equalsIgnoreCase(tipo)) {
            return mostrarIngresoCandidato(model, new LoginCandidatoEntradaDTO());
        }
        model.addAttribute("tipo", TIPO_CONSULTOR.equalsIgnoreCase(tipo) ? TIPO_CONSULTOR : null);
        model.addAttribute("datosConsultor", new LoginConsultorEntradaDTO());
        return "autenticacion";
    }

    // =====================================================================
    // Pasos 4 a 6: consultor
    // =====================================================================

    @PostMapping("/autenticacion/consultor")
    public String ingresarCredenciales(@ModelAttribute("datosConsultor") LoginConsultorEntradaDTO datos, Model model,
                                       HttpServletRequest request) {
        SesionConsultorDTO sesion = gestorAutenticacion.autenticarConsultor(datos);
        if (!sesion.isValido()) {
            // 5.A: mensaje de error; al cerrarlo los campos quedan en blanco y se vuelve al paso 4
            mostrarError(MENSAJE_CONSULTOR_INVALIDO, model);
            blanquearCampos(model);
            model.addAttribute("tipo", TIPO_CONSULTOR);
            return "autenticacion";
        }
        request.changeSessionId(); // nueva sesión al autenticarse
        iniciarSesion(sesion, request.getSession());
        return "redirect:/menu"; // PantallaMenuPrincipal.mostrarMenu() - paso 6
    }

    // =====================================================================
    // 3.A: candidato
    // =====================================================================

    @PostMapping("/autenticacion/candidato")
    public String ingresarCredenciales(@ModelAttribute("datos") LoginCandidatoEntradaDTO datos, Model model,
                                       HttpServletRequest request) {
        if (!gestorAutenticacion.autenticarCandidato(datos)) {
            // 3.A.2.1 a 3.A.2.5: mismo mensaje que CU026; campos en blanco y se vuelve a 3.A
            mostrarError(MENSAJE_CANDIDATO_INVALIDO, model);
            blanquearCampos(model);
            return mostrarIngresoCandidato(model, (LoginCandidatoEntradaDTO) model.getAttribute("datos"));
        }
        // 3.A.3: se invoca CU026 (PantallaCompletarCuestionario.completarCuestionario) con los mismos datos
        request.setAttribute(CANDIDATO_AUTENTICADO, Boolean.TRUE);
        return "forward:/cuestionario";
    }

    // =====================================================================
    // Mensajes, sesión y salida
    // =====================================================================

    public void mostrarError(String mensaje, Model model) {
        model.addAttribute("mensaje", mensaje);
    }

    /** Deja los campos en blanco (CU001 5.A.3 y 3.A.2.4). */
    public void blanquearCampos(Model model) {
        model.addAttribute("datosConsultor", new LoginConsultorEntradaDTO());
        model.addAttribute("datos", new LoginCandidatoEntradaDTO());
    }

    public void iniciarSesion(SesionConsultorDTO sesion, HttpSession session) {
        session.setAttribute(SESION_CONSULTOR, sesion);
    }

    /** Cierra la sesión del consultor y vuelve a la selección del tipo de acceso. */
    @GetMapping("/cerrar-sesion")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    /** Botón Salir (Observaciones del CU001): abandona el ingreso y vuelve a la pantalla inicial. */
    @GetMapping("/salir")
    public String salir(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private String mostrarIngresoCandidato(Model model, LoginCandidatoEntradaDTO datos) {
        model.addAttribute("datos", datos);
        model.addAttribute("tiposDocumento", TIPODOCUMENTO.values());
        model.addAttribute("nombreUsuario", "");
        return "cuestionarioIngreso";
    }

    private static PropertyEditorSupport editorMayusculas() {
        return new PropertyEditorSupport() {
            @Override
            public void setAsText(String texto) {
                setValue(texto == null || texto.isBlank() ? null : texto.trim().toUpperCase(Locale.ROOT));
            }
        };
    }
}
