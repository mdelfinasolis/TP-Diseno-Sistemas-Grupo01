# Gestión de Evaluaciones – TP Diseño de Sistemas 2026

TP de Diseño de Sistemas (UTN-FRSF). Implementa:

- **CU001 – Autenticar Usuario**: pantalla inicial; consultor contra el LDAP, candidato con documento y clave
- **CU020 – Gestionar Preguntas**
- **CU021 – Dar de alta Pregunta**
- **CU025 – Evaluar Candidatos**
- **CU026 – Completar Cuestionario**, con el ingreso del Candidato de CU001 3.A
- **CU028 – Emitir orden de mérito**

CU022, CU023 y CU033 (Generar con IA) se muestran como pantallas "en construcción".

Tecnología (diagrama de despliegue): Java 21, Spring Boot 3 empaquetado como `gestion-evaluaciones.war`
(Tomcat 10), Thymeleaf, JPA/Hibernate, PostgreSQL 16 y configuración en `application.yml`.

---

## 1. Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 21 o superior (`java -version`) |
| Docker Desktop | Para la base PostgreSQL 16 (recomendado). Alternativa: PostgreSQL 16 instalado |
| Maven | No hace falta instalarlo: el proyecto incluye Maven Wrapper (`mvnw` / `mvnw.cmd`), que lo descarga la primera vez |

## 2. Crear la base de datos

La aplicación espera una base llamada `gestion_evaluaciones`. Las **tablas no se crean a mano**: al iniciar,
la aplicación ejecuta `src/main/resources/esquema_gestion_evaluaciones.sql` (crea las tablas) y
`src/main/resources/data.sql` (carga los datos de prueba).

### Opción A (recomendada) – Contenedor Docker con PostgreSQL 16

`docker-compose.yml` define el servicio `postgres:16` con la base `gestion_evaluaciones`,
usuario `postgres`, contraseña `postgres`, publicado en el **puerto 5434** de la PC
(el 5432 queda libre para un PostgreSQL instalado y el 5433 lo usa otro contenedor).

1. Iniciar Docker Desktop y esperar a que diga *Engine running*.
2. Desde la carpeta `codigo_fuente/`, levantar la base:

   ```bat
   docker compose up -d
   ```

3. Verificar que el contenedor esté `healthy`:

   ```bat
   docker compose ps
   ```

   ```
   NAME                      IMAGE         STATUS                   PORTS
   gestion-evaluaciones-db   postgres:16   Up 10 seconds (healthy)  0.0.0.0:5434->5432/tcp
   ```

`application.yml` ya apunta a ese contenedor (`jdbc:postgresql://localhost:5434/gestion_evaluaciones`).
Los datos quedan en el volumen `datos-postgres`. Para detener la base: `docker compose stop`.
Para borrarla por completo (contenedor y volumen): `docker compose down -v`.

Para consultar la base desde la terminal:

```bat
docker exec -it gestion-evaluaciones-db psql -U postgres -d gestion_evaluaciones
```

### Opción B – PostgreSQL 16 instalado (psql o pgAdmin)

```bat
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -c "CREATE DATABASE gestion_evaluaciones ENCODING 'UTF8';"
```

Luego cambiar en `src/main/resources/application.yml` el puerto de la URL a `5432` y la contraseña por la
de su usuario `postgres`.

## 3. Configurar la conexión

En `src/main/resources/application.yml` (valores para el contenedor de la opción A):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5434/gestion_evaluaciones
    username: postgres
    password: postgres
```

También se puede cambiar sin editar el archivo, con variables de entorno:
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.

> `spring.sql.init.mode: always` **borra y recrea las tablas y recarga los datos de prueba en cada arranque**.
> Para conservar los datos entre ejecuciones, cambiarlo a `never` después del primer arranque.

### Repositorio de configuraciones (CU025 y CU026)

Los parámetros que no figuran en el diseño de pantallas están en `application.yml`, bajo `app.cuestionario`:

| Parámetro | Valor | Uso |
|---|---|---|
| `tiempo-maximo-minutos` | 20 | Se copia a cada cuestionario al crearlo (CU025). EN_PROCESO vence si ahora − inicio > tiempo máximo |
| `preguntas-por-bloque` | 4 | Preguntas por paso del wizard; se guarda en la evaluación |
| `cantidad-maxima-accesos` | 3 | Solo para cuestionarios EN_PROCESO: el acceso siguiente lo deja INCOMPLETO |
| `dias-plazo-activo` | 7 | Días que un cuestionario ACTIVO puede esperar a comenzar; después queda SIN_CONTESTAR |
| `longitud-clave` | 8 | Claves de ingreso aleatorias con letras y dígitos |
| `instrucciones.*` | textos | Instrucciones que se muestran al candidato antes de comenzar |

## 4. Ejecutar

Desde la carpeta `codigo_fuente/`:

```bat
mvnw.cmd spring-boot:run        (Windows)
./mvnw spring-boot:run          (Linux / macOS)
```

Con la base levantada (paso 2), el arranque es correcto cuando el log termina con:

```
Tomcat started on port 8080 (http) with context path '/'
Started GestionEvaluacionesApplication in ... seconds
```

Abrir **http://localhost:8080** → pantalla inicial (CU001) con dos opciones:

- **Ingresar como usuario consultor**: usuario y contraseña, validados contra el LDAP (ver abajo). Lleva al menú
  (`/menu`) con *Gestionar Preguntas*, *Evaluar Candidatos* y *Emitir Orden de Mérito*. Arriba a la derecha se ve
  el nombre del consultor y **Cerrar sesión**. Sin sesión, cualquier pantalla del consultor redirige al inicio.
- **Realizar cuestionario**: tipo y número de documento y clave (ver los casos de CU026 más abajo).

### Usuarios de prueba del LDAP (consultores)

El LDAP de la organización (diagrama de despliegue) se simula con un servidor LDAP embebido de Spring Boot que se
carga desde `src/main/resources/ldap/consultores.ldif` y escucha en el puerto 8389. No hay usuarios fijos en el código.

| Usuario | Contraseña | Nombre |
|---|---|---|
| `LFERRARI` | `Ferrari2026` | LUCIA FERRARI |
| `TBIANCHI` | `Bianchi.2026` | TOMAS BIANCHI |
| `CROSSI` | `Rossi#2026` | CARLA ROSSI |

- El usuario se ingresa siempre en mayúsculas (máx. 15 caracteres); la contraseña distingue mayúsculas y minúsculas
  (máx. 20) y se muestra con asteriscos.
- Con datos incorrectos aparece "El usuario o la contraseña no son válidos" y los campos quedan en blanco.
- Para agregar un consultor, sumar una entrada `inetOrgPerson` al `.ldif` (con `uid`, `givenName`, `sn` y
  `userPassword`) y reiniciar. Para usar un LDAP real, quitar `spring.ldap.embedded` de `application.yml` y
  configurar `spring.ldap.urls`, `base`, `username` y `password`.

Si el puerto 8080 está ocupado: `mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`.

Para comprobar que `data.sql` se cargó (deben verse 29 preguntas y 20 cuestionarios):

```bat
docker exec gestion-evaluaciones-db psql -U postgres -d gestion_evaluaciones -c "SELECT (SELECT count(*) FROM pregunta) AS preguntas, (SELECT count(*) FROM cuestionario) AS cuestionarios;"
```

> **Zona horaria.** En Windows, Java informa la zona `America/Buenos_Aires` (un alias antiguo) y el driver
> JDBC se la envía a PostgreSQL. La imagen `postgres:16` ya no incluye ese alias y rechaza la conexión con
> `FATAL: invalid value for parameter "TimeZone"`. Por eso `pom.xml` arranca la JVM de `spring-boot:run` con
> `-Duser.timezone=America/Argentina/Buenos_Aires`. Si se ejecuta desde un IDE, agregar ese mismo parámetro
> en las *VM options* de la configuración de ejecución.

### Desplegar el .war en Tomcat 10

```bat
mvnw.cmd clean package
```

Genera `target/gestion-evaluaciones.war`. Copiarlo a `webapps/` de Tomcat 10; la aplicación queda en
`http://localhost:8080/gestion-evaluaciones/`. Por lo explicado arriba sobre la zona horaria, iniciar Tomcat con
`set CATALINA_OPTS=-Duser.timezone=America/Argentina/Buenos_Aires` (Windows) o
`export CATALINA_OPTS=-Duser.timezone=America/Argentina/Buenos_Aires` (Linux / macOS).

### Tests automáticos

```bat
mvnw.cmd test
```

Usan una base H2 en memoria (modo PostgreSQL) con el mismo esquema y los mismos datos de prueba, y el LDAP
embebido en un puerto libre; no necesitan PostgreSQL. Cubren los gestores de los seis CU y recorren sus pantallas,
incluidos los flujos alternativos. Las pruebas de las pantallas del consultor corren con una sesión de consultor
iniciada; `AutenticacionTest` prueba CU001 y el control de acceso sin sesión.
Los tests de CU001, CU021, CU025 y CU026 se revierten al terminar (`@Transactional`), así que no se afectan entre sí.

---

## 5. Datos de prueba (`data.sql`) y caminos que cubren

### CU020 – Gestionar Preguntas

| Prueba | Cómo |
|---|---|
| Buscar sin filtros | Buscar sin completar nada → 28 preguntas en 3 páginas (paginación) |
| Lista de factores dependiente | Elegir *TRABAJO EN EQUIPO* → aparecen solo *COLABORACIÓN* y *RESOLUCIÓN DE CONFLICTOS* (F99 está dado de baja) |
| Filtrar por competencia / factor | *LIDERAZGO* → 5 preguntas; *LIDERAZGO* + *TOMA DE DECISIONES* → 3 |
| Filtro por nombre ("contiene") | Escribir `equipo` (se convierte a `EQUIPO`) → P004 y P005 |
| Sin resultados | Nombre `XYZ` → grilla vacía |
| Datos dados de baja | La competencia *NEGOCIACIÓN* (C99) y la pregunta P099 nunca aparecen |
| Nuevo / Modificar / Eliminar | Nuevo abre CU021; Modificar y Eliminar abren CU022 / CU023 ("en construcción") y al volver la grilla se actualiza |

### CU021 – Dar de alta Pregunta

| Prueba | Cómo |
|---|---|
| Omisiones (2.A) | Aceptar sin datos → se listan TODAS las omisiones en un panel que no tapa campos ni botones, con alerta auditiva |
| Longitudes (2.B) | Nombre de más de 100 caracteres o pregunta/descripción de más de 500 |
| Ponderaciones | Con un valor negativo o que no sumen 10 (la pantalla muestra la suma actual) |
| Pregunta existente (2.C) | Competencia LIDERAZGO, factor TOMA DE DECISIONES, nombre `DECISIONES BAJO PRESIÓN` (o el texto de otra pregunta del factor) |
| Alta exitosa | Datos completos y ponderaciones que suman 10 → "La Pregunta … se ha creado correctamente. ¿Desea cargar otra?" (SI / NO). Recibe el código P100, P101, … |
| Generar con IA | Lleva a CU033 (en construcción); al volver se conservan los datos cargados |

### CU025 – Evaluar Candidatos

| Prueba | Cómo |
|---|---|
| 4.A | Agregar o Siguiente sin candidatos → "Se debe seleccionar al menos un candidato." |
| 6.A | Agregar a ROMERO (8, cuestionario ACTIVO), MARTÍNEZ (5) o MORALES (13, EN_PROCESO) y presionar Siguiente |
| Candidatos evaluables | GARCÍA, MOLINA, CASTRO, ORTIZ y SILVA (16 a 20, nunca evaluados) y los que ya terminaron su cuestionario |
| 9.A | Función **3030 DISEÑADOR UX**: CREATIVIDAD no tiene ningún factor con 2 preguntas → Aceptar vuelve al menú |
| Competencia evaluable con un factor incompleto | **6060 ANALISTA DE DATOS**: ADAPTABILIDAD tiene un factor con 3 preguntas y otro con 1 → es evaluable |
| 10.A | Siguiente sin función / puesto → "Se debe seleccionar al menos una función o puesto." |
| Finalizar y exportar | Se crean los cuestionarios ACTIVO; "Exportar" descarga `aaaa-mm-dd-hh-mm-NombreFunción.xls`; Cancelar en el diálogo vuelve a la pregunta de exportación |

### CU026 – Completar Cuestionario (ingreso con tipo DNI)

| Documento | Clave | Situación |
|---|---|---|
| 42111001 (VEGA) | `ACTIVA27` | ACTIVO vigente → instrucciones, 2 bloques (6 preguntas) y "El cuestionario ha sido completado con éxito." |
| 42111002 (ACOSTA) | `VENCID38` | ACTIVO creado hace 10 días → "Tiempo de espera alcanzado." (SIN CONTESTAR) |
| 42111003 (RÍOS) | `PROCES49` | EN_PROCESO con el bloque 1 respondido → retoma en "Bloque (2/2)" |
| 42111004 (MÉNDEZ) | `TIEMPO52` | EN_PROCESO iniciado hace 30 minutos → "Tiempo máximo alcanzado." (INCOMPLETO) |
| 42111005 (HERRERA) | `ACCESO63` | EN_PROCESO con 3 accesos → "Cantidad máxima de accesos alcanzada." (INCOMPLETO) |
| cualquier otro dato | | "Los datos ingresados no son válidos o no existe un cuestionario para el Candidato" y campos en blanco |

Las fechas de estos cuestionarios se calculan al cargar `data.sql` (relativas al momento del arranque). Como la
app recarga los datos en cada inicio, los casos vuelven a su estado original al reiniciarla.
Siguiente / Finalizar con preguntas sin responder lista las faltantes, mantiene el bloque y emite la alerta auditiva.
Un cuestionario completado aparece luego en el orden de mérito (CU028) del puesto 6060.

### CU028 – Emitir orden de mérito

| Puesto | Situación | Qué se ve |
|---|---|---|
| **1515 PROGRAMADOR** (FOLDER IT) | 2 evaluaciones, todos los estados | Orden de mérito: PÉREZ (24) y RODRÍGUEZ (24, empate → por apellido), GÓMEZ (15, justo en los mínimos). No alcanzan: FERNÁNDEZ (22, C05 < 5), BENÍTEZ (20, sin resultado en C05), SÁNCHEZ (14). Otros: LÓPEZ (INCOMPLETO), MARTÍNEZ (EN_PROCESO), ROMERO (ACTIVO, sin fechas), TORRES (SIN_CONTESTAR) |
| **4845 ANALISTA FUNCIONAL** (FOLDER IT) | 1 evaluación | ÁLVAREZ cumple; RUIZ tiene más puntaje pero no cumple C03; DÍAZ INCOMPLETO |
| **2147 JEFE DE PROYECTO** (TECNOSUR S.A.) | 1 evaluación sin COMPLETO | Las dos primeras secciones vacías; EN_PROCESO y ACTIVO |
| **7845 SOPORTE TÉCNICO** (AGRO LITORAL S.R.L.) | **Sin evaluaciones** | Flujo alternativo 6.A: "El puesto seleccionado no posee evaluaciones." |
| 9999 PUESTO DADO DE BAJA | Eliminado | No aparece en la lista ni en la búsqueda |

Con PROGRAMADOR se puede elegir la evaluación 1, la 2 o *Seleccionar todas*.

---

## 6. Organización del código

```
src/main/java/ar/edu/utn/frsf/gestionevaluaciones/
├── boundary/   Pantallas (controllers): PantallaAutenticacion, PantallaMenuPrincipal, PantallaGestionPreguntas,
│               PantallaAltaPregunta, PantallaEvaluarCandidatos, PantallaCompletarCuestionario, PantallaOrdenMerito;
│               InterceptorSesionConsultor y ConfiguracionWeb (control de sesión del consultor)
├── control/    Gestores (@Service): GestorAutenticacion, GestorPreguntas, GestorCompetencias, GestorFactores,
│               GestorOpcionesRespuesta, GestorCandidatos, GestorEvaluaciones, GestorCuestionarios, GestorPuestos,
│               GestorEmpresas, GestorReportes
├── entity/     Modelo de dominio (JPA); Consultor no se persiste (sus datos vienen del LDAP)
├── interfaces/ IAutenticable (la implementa Candidato)
├── dto/        entrada/, salida/ y excel/
└── dao/        IDAO<T, ID> y un DAO por entidad, implementado con EntityManager
src/main/resources/
├── boundary/templates/   Templates Thymeleaf de las pantallas
├── static/               CSS y JS de las pantallas
├── ldap/consultores.ldif Consultores de prueba del LDAP embebido
├── esquema_gestion_evaluaciones.sql
└── data.sql
```

### Criterios aplicados

- Los nombres de clases, métodos, parámetros y DTOs son los del diagrama de clases. Cada mensaje de los
  diagramas de secuencia (CU001, CU020, CU021, CU025, CU026 y CU028) tiene su método con el mismo nombre.
- Un gestor solo usa su propio DAO; con las demás entidades se comunica a través del gestor correspondiente.
  `GestorReportes` no usa ningún DAO.
- Las pantallas solo reciben y envían DTOs (`spring.jpa.open-in-view: false` impide que una entidad llegue a la vista).
- Los datos literales se convierten a mayúsculas en el servidor (`@InitBinder` de cada pantalla) y se muestran en mayúsculas.
- Enter sobre un campo equivale a presionar el botón principal de la pantalla; Shift+Tab retrocede un campo
  (el orden de tabulación es el de la columna *Tab* del diseño).
- La impresión del orden de mérito se hace desde el navegador (`window.print()`), con una hoja de estilos de impresión.
- Puntaje del orden de mérito = suma de los `ResultadoCompetencia` de las competencias del puesto. Un
  candidato cumple los mínimos si tiene resultado en todas las competencias del puesto y en cada una
  alcanza la ponderación mínima.
- CU001: el consultor se valida contra el LDAP; el candidato, por documento y por `Candidato.validarCredenciales`
  (vía `IAutenticable`), que busca entre sus cuestionarios uno ACTIVO o EN_PROCESO con esa clave. Luego CU026 vuelve
  a validar con el mismo criterio y mensaje, y recién ahí registra el acceso. A CU026 solo se llega desde CU001.
- La "alerta auditiva" de los diagramas es un sonido corto generado en el navegador (Web Audio).
- CU025: el Excel (.xls) se arma en la pantalla a partir de `ExportacionCandidatosExcelDTO` y se descarga desde el
  navegador; eso reemplaza la selección de carpeta destino.
- CU026: la clave se ingresa con asteriscos; la cuenta regresiva usa `segundosRestantes`. Las preguntas se asignan
  al azar al aceptar las instrucciones (2 por factor con al menos 2 preguntas) y quedan guardadas, así que el
  cuestionario se retoma idéntico. Puntajes: factor = suma de ponderaciones / 2; competencia = promedio de sus factores.
- No se implementa CU032 (finalizar cuestionarios vencidos): un cuestionario ACTIVO vencido sigue contando como
  activo para CU025 hasta que el candidato intenta ingresar.
