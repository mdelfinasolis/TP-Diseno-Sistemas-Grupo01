package ar.edu.utn.frsf.gestionevaluaciones.control;

import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.LoginCandidatoEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.entrada.LoginConsultorEntradaDTO;
import ar.edu.utn.frsf.gestionevaluaciones.dto.salida.SesionConsultorDTO;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Candidato;
import ar.edu.utn.frsf.gestionevaluaciones.entity.Consultor;
import ar.edu.utn.frsf.gestionevaluaciones.interfaces.IAutenticable;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.directory.Attribute;
import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * CU001 - Autenticar usuario. El consultor se valida contra el LDAP de la organización;
 * el candidato, por documento y clave de un cuestionario activo o en proceso (vía IAutenticable).
 */
@Service
@Transactional(readOnly = true)
public class GestorAutenticacion {

    /** Longitudes máximas definidas por el LDAP (CU001, requerimientos especiales). */
    static final int LONGITUD_USUARIO = 15;
    static final int LONGITUD_CONTRASENA = 20;

    private final LdapTemplate ldapTemplate;
    private final GestorCandidatos gestorCandidatos;

    public GestorAutenticacion(LdapTemplate ldapTemplate, GestorCandidatos gestorCandidatos) {
        this.ldapTemplate = ldapTemplate;
        this.gestorCandidatos = gestorCandidatos;
    }

    /** Pasos 4 a 6: valida usuario y contraseña en el LDAP y arma la sesión del consultor con sus datos. */
    public SesionConsultorDTO autenticarConsultor(LoginConsultorEntradaDTO datos) {
        if (!validarLDAP(datos)) {
            return SesionConsultorDTO.invalida();
        }
        List<String[]> atributos = ldapTemplate.search(
                query().where("uid").is(datos.getUsuario()),
                (AttributesMapper<String[]>) attrs -> new String[]{
                        valor(attrs.get("uid")), valor(attrs.get("givenName")), valor(attrs.get("sn"))});
        if (atributos.isEmpty()) {
            return SesionConsultorDTO.invalida();
        }
        String[] datosLdap = atributos.get(0);
        Consultor consultor = new Consultor(datosLdap[0].toUpperCase(), datosLdap[1], datosLdap[2]);
        return new SesionConsultorDTO(consultor.getNombreUsuario(), consultor.getNombre(), consultor.getApellido(), true);
    }

    /** 3.A.2: existe el candidato y la clave es la de uno de sus cuestionarios activos o en proceso. */
    public boolean autenticarCandidato(LoginCandidatoEntradaDTO datos) {
        Candidato candidato = gestorCandidatos.obtenerPorDocumento(datos.getTipoDocumento(), datos.getNroDocumento());
        if (candidato == null) {
            return false;
        }
        IAutenticable autenticable = candidato;
        return autenticable.validarCredenciales(datos.getClaveIngreso());
    }

    /** Autenticación (bind) contra el LDAP con el usuario y la contraseña ingresados. */
    public boolean validarLDAP(LoginConsultorEntradaDTO datos) {
        String usuario = datos.getUsuario();
        String contrasena = datos.getContrasena();
        if (usuario == null || usuario.isBlank() || contrasena == null || contrasena.isEmpty()
                || usuario.length() > LONGITUD_USUARIO || contrasena.length() > LONGITUD_CONTRASENA) {
            return false;
        }
        try {
            ldapTemplate.authenticate(query().where("uid").is(usuario), contrasena);
            return true;
        } catch (RuntimeException e) {
            // Usuario inexistente, contraseña incorrecta o LDAP no disponible: no se informa el detalle
            return false;
        }
    }

    private static String valor(Attribute atributo) throws javax.naming.NamingException {
        return atributo == null ? "" : String.valueOf(atributo.get());
    }
}
