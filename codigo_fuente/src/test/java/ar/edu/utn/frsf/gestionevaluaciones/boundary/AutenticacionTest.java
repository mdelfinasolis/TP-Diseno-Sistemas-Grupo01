package ar.edu.utn.frsf.gestionevaluaciones.boundary;

import ar.edu.utn.frsf.gestionevaluaciones.control.GestorAutenticacion;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.LoginCandidatoEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.LoginConsultorEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.SesionConsultorDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.TIPODOCUMENTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** CU001 - Autenticar usuario: pantalla inicial, LDAP embebido, candidato y control de sesión. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AutenticacionTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private GestorAutenticacion gestorAutenticacion;

    @Test
    void pantallaInicialConLasDosOpciones() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ingresar como usuario consultor")))
                .andExpect(content().string(containsString("Realizar cuestionario")));
        mvc.perform(get("/").param("tipo", "consultor"))
                .andExpect(content().string(containsString("type=\"password\"")))
                .andExpect(content().string(containsString("maxlength=\"15\"")));
    }

    @Test
    void lasPantallasDelConsultorExigenSesion() throws Exception {
        for (String url : new String[]{"/menu", "/preguntas", "/preguntas/alta", "/evaluar", "/orden-merito"}) {
            mvc.perform(get(url)).andExpect(redirectedUrl("/"));
        }
    }

    @Test
    void consultorValidoIngresaAlMenuYCierraSesion() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        // El identificador se convierte a mayúsculas; la contraseña se respeta
        mvc.perform(post("/autenticacion/consultor").session(sesion)
                        .param("usuario", "lferrari").param("contrasena", "Ferrari2026"))
                .andExpect(redirectedUrl("/menu"));
        SesionConsultorDTO consultor = (SesionConsultorDTO) sesion.getAttribute(PantallaAutenticacion.SESION_CONSULTOR);
        assertThat(consultor.getNombreUsuario()).isEqualTo("LFERRARI");

        mvc.perform(get("/menu").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FERRARI, LUCIA")))
                .andExpect(content().string(containsString("Cerrar sesión")));
        mvc.perform(get("/").session(sesion)).andExpect(redirectedUrl("/menu"));

        mvc.perform(get("/cerrar-sesion").session(sesion)).andExpect(redirectedUrl("/"));
        assertThat(sesion.isInvalid()).isTrue();
    }

    @Test
    void consultorInvalidoVeElErrorYLosCamposEnBlanco() throws Exception {
        mvc.perform(post("/autenticacion/consultor").param("usuario", "LFERRARI").param("contrasena", "otra"))
                .andExpect(content().string(containsString("El usuario o la contraseña no son válidos")))
                .andExpect(content().string(not(containsString("value=\"LFERRARI\""))));

        // La contraseña distingue mayúsculas y minúsculas
        assertThat(gestorAutenticacion.autenticarConsultor(new LoginConsultorEntradaDTO("LFERRARI", "FERRARI2026"))
                .isValido()).isFalse();
        // Usuario inexistente y usuario que supera los 15 caracteres del LDAP
        assertThat(gestorAutenticacion.validarLDAP(new LoginConsultorEntradaDTO("NOEXISTE", "Ferrari2026"))).isFalse();
        assertThat(gestorAutenticacion.validarLDAP(new LoginConsultorEntradaDTO("LFERRARI1234567X", "Ferrari2026"))).isFalse();
        assertThat(gestorAutenticacion.autenticarConsultor(new LoginConsultorEntradaDTO("TBIANCHI", "Bianchi.2026")))
                .extracting(SesionConsultorDTO::getApellido).isEqualTo("BIANCHI");
    }

    @Test
    void candidatoSeValidaPorDocumentoYClaveDeUnCuestionarioActivoOEnProceso() throws Exception {
        assertThat(gestorAutenticacion.autenticarCandidato(
                new LoginCandidatoEntradaDTO(TIPODOCUMENTO.DNI, "42111001", "ACTIVA27"))).isTrue();
        assertThat(gestorAutenticacion.autenticarCandidato(
                new LoginCandidatoEntradaDTO(TIPODOCUMENTO.DNI, "42111003", "PROCES49"))).isTrue();
        // Cuestionario COMPLETO, clave de otro candidato y documento inexistente
        assertThat(gestorAutenticacion.autenticarCandidato(
                new LoginCandidatoEntradaDTO(TIPODOCUMENTO.DNI, "30111222", "K1001"))).isFalse();
        assertThat(gestorAutenticacion.autenticarCandidato(
                new LoginCandidatoEntradaDTO(TIPODOCUMENTO.DNI, "42111001", "PROCES49"))).isFalse();
        assertThat(gestorAutenticacion.autenticarCandidato(
                new LoginCandidatoEntradaDTO(TIPODOCUMENTO.DNI, "99999999", "ACTIVA27"))).isFalse();

        mvc.perform(post("/autenticacion/candidato").param("tipoDocumento", "DNI")
                        .param("nroDocumento", "42111001").param("claveIngreso", "mala1234"))
                .andExpect(content().string(containsString(
                        "Los datos ingresados no son válidos o no existe un cuestionario para el Candidato")))
                .andExpect(content().string(not(containsString("value=\"42111001\""))));

        MvcResult valido = mvc.perform(post("/autenticacion/candidato").param("tipoDocumento", "DNI")
                        .param("nroDocumento", "42111001").param("claveIngreso", "activa27"))
                .andExpect(forwardedUrl("/cuestionario"))
                .andReturn();
        assertThat(valido.getRequest().getAttribute(PantallaAutenticacion.CANDIDATO_AUTENTICADO)).isEqualTo(true);
    }

    @Test
    void elCandidatoSoloLlegaACU026PasandoPorCU001() throws Exception {
        mvc.perform(get("/cuestionario")).andExpect(redirectedUrl("/?tipo=candidato"));
        mvc.perform(post("/cuestionario").param("tipoDocumento", "DNI")
                        .param("nroDocumento", "42111001").param("claveIngreso", "ACTIVA27"))
                .andExpect(redirectedUrl("/?tipo=candidato"));
        // Un candidato sin sesión de consultor no accede a las pantallas del consultor
        mvc.perform(get("/evaluar")).andExpect(redirectedUrl("/"));
    }
}
