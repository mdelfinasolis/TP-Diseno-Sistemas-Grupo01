/*
 * Comportamiento común de las pantallas:
 *  - Enter tiene el mismo efecto que hacer click en un botón: sobre un botón lo presiona
 *    (comportamiento nativo) y sobre un campo presiona el botón principal de la pantalla
 *    (el elemento marcado con data-boton-defecto en el mismo formulario).
 *  - Shift+Tab retrocede un campo (comportamiento nativo del navegador; el orden lo fija tabindex).
 *  - Los datos literales se muestran en mayúsculas mientras se escriben.
 *  - Las ventanas de mensaje se cierran con la X o con Escape.
 */
document.addEventListener("keydown", function (e) {
    if (e.key !== "Enter" || e.shiftKey || e.ctrlKey || e.altKey) {
        return;
    }
    const el = e.target;
    const esCampo = el.matches("input:not([type=button]):not([type=submit]), select");
    if (!esCampo) {
        return; // en botones, enlaces y áreas de texto Enter conserva su comportamiento
    }
    const formulario = el.closest("form");
    const botonDefecto = (formulario && formulario.querySelector("[data-boton-defecto]"))
        || document.querySelector("[data-boton-defecto]");
    if (botonDefecto && !botonDefecto.disabled) {
        e.preventDefault();
        botonDefecto.click();
    }
});

document.addEventListener("input", function (e) {
    const el = e.target;
    // La contraseña del consultor (data-sin-mayusculas) se respeta tal cual: la valida el LDAP
    if (el.matches("input[type=text], input[type=password], textarea") && !el.matches("[data-sin-mayusculas]")) {
        const inicio = el.selectionStart;
        const fin = el.selectionEnd;
        el.value = el.value.toUpperCase();
        el.setSelectionRange(inicio, fin);
    }
});

/* Alerta auditiva: un sonido corto generado en el navegador (CU021 y CU026). */
function emitirAlertaAuditiva() {
    try {
        const Contexto = window.AudioContext || window.webkitAudioContext;
        const contexto = new Contexto();
        const oscilador = contexto.createOscillator();
        const volumen = contexto.createGain();
        oscilador.type = "square";
        oscilador.frequency.value = 880;
        volumen.gain.setValueAtTime(0.08, contexto.currentTime);
        volumen.gain.exponentialRampToValueAtTime(0.0001, contexto.currentTime + 0.25);
        oscilador.connect(volumen).connect(contexto.destination);
        oscilador.start();
        oscilador.stop(contexto.currentTime + 0.25);
    } catch (error) {
        /* el navegador no permite reproducir sonido: se ignora */
    }
}

/* Ventanas de mensaje: mostrar / ocultar. */
function mostrarVentana(velo) {
    velo.classList.remove("oculto");
    const cerrar = velo.querySelector(".cerrar");
    if (cerrar) {
        cerrar.focus();
    }
}

function ocultarVentana(velo) {
    velo.classList.add("oculto");
}

/* CU021: el factor elegido queda seleccionado en el formulario (no requiere consultar al servidor). */
function seleccionarFactor(codigoFactor) {
    const factor = document.getElementById("codigoFactor");
    if (factor) {
        factor.value = codigoFactor;
    }
}

/* CU021: al elegir la opción de respuesta se arma la grilla Orden / Descripción / Ponderación. */
function seleccionarOpcionRespuesta(nombreOpcion) {
    const cuerpo = document.getElementById("itemsOpcion");
    const selector = document.getElementById("nombreOpcionRespuesta");
    if (!cuerpo || !selector) {
        return;
    }
    const filaVacia = function () {
        cuerpo.innerHTML = '<tr class="vacia"><td colspan="3">Seleccione una opción de respuesta '
            + 'para cargar las ponderaciones.</td></tr>';
        actualizarSumaPonderaciones();
    };
    if (!nombreOpcion) {
        filaVacia();
        return;
    }
    fetch(selector.dataset.urlOpcion + "?nombre=" + encodeURIComponent(nombreOpcion))
        .then(function (r) { return r.status === 200 ? r.json() : null; })
        .then(function (detalle) {
            if (!detalle) {
                filaVacia();
                return;
            }
            cuerpo.innerHTML = "";
            detalle.items.forEach(function (item, i) {
                const fila = document.createElement("tr");
                const opciones = Array.from({length: 11}, function (_, n) {
                    return '<option value="' + n + '">' + n + "</option>";
                }).join("");
                fila.innerHTML = "<td>" + item.orden + "</td><td></td>"
                    + '<td class="accion"><input type="hidden" name="ponderaciones[' + i + '].ordenItem" value="'
                    + item.orden + '"><select class="ponderacion" tabindex="7" name="ponderaciones[' + i
                    + '].ponderacion" aria-label="Ponderación del ítem ' + item.orden + '">' + opciones
                    + "</select></td>";
                fila.children[1].textContent = item.descripcion;
                cuerpo.appendChild(fila);
            });
            actualizarSumaPonderaciones();
        });
}

function actualizarSumaPonderaciones() {
    const indicador = document.getElementById("sumaPonderaciones");
    if (!indicador) {
        return;
    }
    const selects = document.querySelectorAll("select.ponderacion");
    if (!selects.length) {
        indicador.textContent = "";
        return;
    }
    let suma = 0;
    selects.forEach(function (s) { suma += Number(s.value); });
    indicador.textContent = "(Suma actual: " + suma + ")";
    indicador.classList.toggle("suma-incorrecta", suma !== 10);
}

/* CU026: el candidato responde las preguntas del bloque; al responder se quita el resaltado. */
function responderPreguntas() {
    document.querySelectorAll("select.respuesta").forEach(function (s) {
        s.addEventListener("change", function () {
            s.closest(".pregunta").classList.toggle("sin-responder", !s.value);
        });
    });
}

/* CU026: true si todas las preguntas del bloque tienen respuesta. */
function validarPreguntasRespondidas() {
    return Array.from(document.querySelectorAll("select.respuesta")).every(function (s) { return s.value; });
}

/* CU026 6.A: resalta y lista las preguntas sin responder, manteniendo el bloque y las respuestas. */
function mostrarPreguntasSinResponder() {
    const lista = document.getElementById("listaSinResponder");
    const ventana = document.getElementById("ventanaSinResponder");
    if (!lista || !ventana) {
        return;
    }
    lista.innerHTML = "";
    document.querySelectorAll(".pregunta").forEach(function (p) {
        const sinRespuesta = !p.querySelector("select.respuesta").value;
        p.classList.toggle("sin-responder", sinRespuesta);
        if (sinRespuesta) {
            const li = document.createElement("li");
            li.textContent = "Pregunta " + p.dataset.numero;
            lista.appendChild(li);
        }
    });
    mostrarVentana(ventana);
}

/* CU028: imprimir() - la impresión la resuelve el navegador del Consultor. */
function imprimir() {
    window.print();
}

document.addEventListener("DOMContentLoaded", function () {
    /* Alerta auditiva pedida por el servidor (errores de validación, vencimientos). */
    if (document.body.dataset.alertaAuditiva === "true") {
        emitirAlertaAuditiva();
    }

    /* CU020 / CU021: al seleccionar una competencia se cargan sus factores (seleccionarCompetencia). */
    const competencia = document.getElementById("codigoCompetencia");
    const factor = document.getElementById("codigoFactor");
    if (competencia && factor) {
        competencia.addEventListener("change", function () {
            const url = competencia.dataset.urlFactores + "?codigoCompetencia=" + encodeURIComponent(competencia.value);
            factor.length = 1; // deja solo "Seleccionar..."
            if (!competencia.value) {
                return;
            }
            fetch(url)
                .then(function (r) { return r.json(); })
                .then(function (factores) {
                    factores.forEach(function (f) {
                        factor.add(new Option(f.nombre, f.codigo));
                    });
                });
        });
    }

    /* CU021: opción de respuesta y suma de ponderaciones. */
    const opcion = document.getElementById("nombreOpcionRespuesta");
    if (opcion) {
        opcion.addEventListener("change", function () { seleccionarOpcionRespuesta(opcion.value); });
        document.addEventListener("change", function (e) {
            if (e.target.matches("select.ponderacion")) {
                actualizarSumaPonderaciones();
            }
        });
        actualizarSumaPonderaciones();
    }

    /* CU028 pantalla 1: "Emitir el orden de mérito" se habilita al seleccionar un puesto. */
    const radiosPuesto = document.querySelectorAll("input[name=codigoPuesto]");
    const botonEmitirOrden = document.getElementById("botonEmitirOrden");
    if (radiosPuesto.length && botonEmitirOrden) {
        const actualizar = function () {
            botonEmitirOrden.disabled = !document.querySelector("input[name=codigoPuesto]:checked");
        };
        radiosPuesto.forEach(function (r) { r.addEventListener("change", actualizar); });
        actualizar();
    }

    /* CU028 pantalla 2: selección de evaluaciones y opción TODAS. */
    const checks = document.querySelectorAll("input[name=idsEvaluaciones]");
    const todas = document.getElementById("todasLasEvaluaciones");
    const botonSeleccionarTodas = document.getElementById("botonSeleccionarTodas");
    const botonEmitir = document.getElementById("botonEmitir");
    if (checks.length && todas && botonEmitir) {
        const actualizar = function () {
            const marcadas = document.querySelectorAll("input[name=idsEvaluaciones]:checked").length;
            todas.value = marcadas === checks.length ? "true" : "false";
            botonEmitir.disabled = marcadas === 0;
        };
        checks.forEach(function (c) { c.addEventListener("change", actualizar); });
        if (botonSeleccionarTodas) {
            botonSeleccionarTodas.addEventListener("click", function () {
                checks.forEach(function (c) { c.checked = true; });
                actualizar();
            });
        }
        actualizar();
    }

    /* CU025: el Excel se descarga desde el navegador y luego el CU vuelve a la pantalla principal. */
    const formExportar = document.getElementById("formExportar");
    if (formExportar) {
        formExportar.addEventListener("submit", function (e) {
            e.preventDefault();
            fetch(formExportar.action, {method: "POST", body: new FormData(formExportar)})
                .then(function (r) {
                    const disposicion = r.headers.get("Content-Disposition") || "";
                    const coincidencia = /filename\*=UTF-8''([^;]+)/.exec(disposicion);
                    const nombre = coincidencia ? decodeURIComponent(coincidencia[1]) : "candidatos.xls";
                    return r.blob().then(function (blob) { return {blob: blob, nombre: nombre}; });
                })
                .then(function (archivo) {
                    const enlace = document.createElement("a");
                    enlace.href = URL.createObjectURL(archivo.blob);
                    enlace.download = archivo.nombre;
                    document.body.appendChild(enlace);
                    enlace.click();
                    enlace.remove();
                    setTimeout(function () { window.location.href = formExportar.dataset.urlFin; }, 800);
                });
        });
    }

    /* CU026: respuestas, validación del bloque y cuenta regresiva. */
    const formBloque = document.getElementById("formBloque");
    if (formBloque) {
        responderPreguntas();
        formBloque.addEventListener("submit", function (e) {
            if (!validarPreguntasRespondidas()) {
                e.preventDefault();
                mostrarPreguntasSinResponder();
                emitirAlertaAuditiva();
            }
        });
    }
    const cuenta = document.getElementById("cuentaRegresiva");
    if (cuenta) {
        const limite = Number(cuenta.dataset.limite);
        const dosDigitos = function (n) { return String(n).padStart(2, "0"); };
        const actualizarCuenta = function () {
            const restante = Math.max(0, Math.floor((limite - Date.now()) / 1000));
            cuenta.textContent = dosDigitos(Math.floor(restante / 3600)) + ":"
                + dosDigitos(Math.floor((restante % 3600) / 60)) + ":" + dosDigitos(restante % 60);
            cuenta.classList.toggle("agotado", restante === 0);
        };
        actualizarCuenta();
        setInterval(actualizarCuenta, 1000);
    }

    /* Ventanas de mensaje: se cierran con la X (botón) o con Escape. */
    document.querySelectorAll(".velo").forEach(function (velo) {
        const cerrar = velo.querySelector("button.cerrar");
        if (cerrar) {
            cerrar.addEventListener("click", function () { ocultarVentana(velo); });
        }
        if (!velo.classList.contains("oculto")) {
            const foco = velo.querySelector("[autofocus], .cerrar, .boton");
            if (foco) {
                foco.focus();
            }
        }
    });
    document.addEventListener("keydown", function (e) {
        if (e.key !== "Escape") {
            return;
        }
        document.querySelectorAll(".velo:not(.oculto)").forEach(function (velo) {
            if (velo.querySelector("button.cerrar")) {
                ocultarVentana(velo);
            }
        });
    });
});
