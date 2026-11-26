-- =====================================================================
-- Datos de prueba para CU020 (Gestionar Preguntas), CU021 (Dar de alta Pregunta),
-- CU025 (Evaluar Candidatos), CU026 (Completar Cuestionario) y CU028 (Emitir orden de mérito).
-- Todos los datos literales están en MAYÚSCULAS (requisito del enunciado).
-- Ver README.md, sección "Datos de prueba", para los caminos que cubre cada dato.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Empresas
-- ---------------------------------------------------------------------
INSERT INTO empresa (id_empresa, nombre) VALUES (1, 'FOLDER IT');
INSERT INTO empresa (id_empresa, nombre) VALUES (2, 'TECNOSUR S.A.');
INSERT INTO empresa (id_empresa, nombre) VALUES (3, 'AGRO LITORAL S.R.L.');
ALTER TABLE empresa ALTER COLUMN id_empresa RESTART WITH 100;

-- ---------------------------------------------------------------------
-- Competencias (C99 dada de baja: no debe aparecer en la lista)
-- ---------------------------------------------------------------------
INSERT INTO competencia (codigo_competencia, nombre, descripcion, tipo, eliminada) VALUES
('C01', 'LIDERAZGO', 'CAPACIDAD DE GUIAR E INFLUIR EN UN EQUIPO', 'GERENCIAL', FALSE),
('C02', 'TRABAJO EN EQUIPO', 'CAPACIDAD DE COLABORAR CON OTROS', 'GENERAL', FALSE),
('C03', 'COMUNICACIÓN', 'CAPACIDAD DE TRANSMITIR IDEAS CON CLARIDAD', 'GENERAL', FALSE),
('C04', 'ORIENTACIÓN A RESULTADOS', 'CAPACIDAD DE CUMPLIR OBJETIVOS', 'GENERAL', FALSE),
('C05', 'PENSAMIENTO ANALÍTICO', 'CAPACIDAD DE ANALIZAR PROBLEMAS', 'TÉCNICA', FALSE),
-- C06: ningún factor con 2 o más preguntas -> NO evaluable (CU025 9.A)
('C06', 'CREATIVIDAD', 'CAPACIDAD DE GENERAR IDEAS NUEVAS', 'GENERAL', FALSE),
-- C07: un factor con 3 preguntas y otro con 1 -> evaluable; en CU026 el factor con 1 pregunta se omite
('C07', 'ADAPTABILIDAD', 'CAPACIDAD DE ADAPTARSE A LOS CAMBIOS', 'GENERAL', FALSE),
('C99', 'NEGOCIACIÓN', 'COMPETENCIA DADA DE BAJA', 'GENERAL', TRUE);

-- ---------------------------------------------------------------------
-- Factores (F99 dado de baja: no debe aparecer en la lista de factores de C02)
-- ---------------------------------------------------------------------
INSERT INTO factor (codigo_factor, nombre, descripcion, nro_orden, eliminado, codigo_competencia) VALUES
('F01', 'TOMA DE DECISIONES', NULL, 1, FALSE, 'C01'),
('F02', 'MOTIVACIÓN DEL EQUIPO', NULL, 2, FALSE, 'C01'),
('F03', 'COLABORACIÓN', NULL, 1, FALSE, 'C02'),
('F04', 'RESOLUCIÓN DE CONFLICTOS', NULL, 2, FALSE, 'C02'),
('F99', 'FACTOR DADO DE BAJA', NULL, 3, TRUE, 'C02'),
('F05', 'COMUNICACIÓN ORAL', NULL, 1, FALSE, 'C03'),
('F06', 'COMUNICACIÓN ESCRITA', NULL, 2, FALSE, 'C03'),
('F07', 'CUMPLIMIENTO DE OBJETIVOS', NULL, 1, FALSE, 'C04'),
('F08', 'PLANIFICACIÓN', NULL, 2, FALSE, 'C04'),
('F09', 'RESOLUCIÓN DE PROBLEMAS', NULL, 1, FALSE, 'C05'),
('F10', 'ANÁLISIS DE DATOS', NULL, 2, FALSE, 'C05'),
('F11', 'GENERACIÓN DE IDEAS', NULL, 1, FALSE, 'C06'),
('F12', 'INNOVACIÓN', NULL, 2, FALSE, 'C06'),
('F13', 'FLEXIBILIDAD', NULL, 1, FALSE, 'C07'),
('F14', 'TOLERANCIA AL CAMBIO', NULL, 2, FALSE, 'C07');

-- ---------------------------------------------------------------------
-- Opciones de respuesta e ítems
-- ---------------------------------------------------------------------
INSERT INTO opcion_respuesta (nombre_opcion_respuesta, descripcion, eliminada) VALUES
('FRECUENCIA', 'ESCALA DE FRECUENCIA', FALSE),
('ACUERDO', 'ESCALA DE ACUERDO', FALSE);

INSERT INTO item_opcion (nombre_opcion_respuesta, orden, descripcion) VALUES
('FRECUENCIA', 1, 'NUNCA'),
('FRECUENCIA', 2, 'CASI NUNCA'),
('FRECUENCIA', 3, 'A VECES'),
('FRECUENCIA', 4, 'CASI SIEMPRE'),
('FRECUENCIA', 5, 'SIEMPRE'),
('ACUERDO', 1, 'TOTALMENTE EN DESACUERDO'),
('ACUERDO', 2, 'EN DESACUERDO'),
('ACUERDO', 3, 'NI DE ACUERDO NI EN DESACUERDO'),
('ACUERDO', 4, 'DE ACUERDO'),
('ACUERDO', 5, 'TOTALMENTE DE ACUERDO');

-- ---------------------------------------------------------------------
-- Preguntas: 28 vigentes (3 páginas de 10 en la grilla) + 1 dada de baja (P099).
-- Las nuevas que se den de alta con CU021 reciben el código siguiente (P100, P101, ...).
-- ---------------------------------------------------------------------
INSERT INTO pregunta (codigo_pregunta, nombre, enunciado, descripcion, eliminada, codigo_factor, nombre_opcion_respuesta) VALUES
('P001', 'DECISIONES BAJO PRESIÓN', '¿CON QUÉ FRECUENCIA TOMA DECISIONES RÁPIDAS BAJO PRESIÓN?', NULL, FALSE, 'F01', 'FRECUENCIA'),
('P002', 'ASUMIR RESPONSABILIDAD', '¿ASUME LA RESPONSABILIDAD POR LAS DECISIONES DE SU EQUIPO?', NULL, FALSE, 'F01', 'FRECUENCIA'),
('P003', 'ANÁLISIS DE ALTERNATIVAS', '¿EVALÚA VARIAS ALTERNATIVAS ANTES DE DECIDIR?', NULL, FALSE, 'F01', 'FRECUENCIA'),
('P004', 'RECONOCIMIENTO DEL EQUIPO', '¿RECONOCE PÚBLICAMENTE LOS LOGROS DE SUS COMPAÑEROS?', NULL, FALSE, 'F02', 'FRECUENCIA'),
('P005', 'INSPIRAR AL EQUIPO', '¿LOGRA QUE EL EQUIPO SE COMPROMETA CON UN OBJETIVO COMÚN?', NULL, FALSE, 'F02', 'ACUERDO'),
('P006', 'AYUDA A COMPAÑEROS', '¿OFRECE AYUDA A SUS COMPAÑEROS SIN QUE SE LA PIDAN?', NULL, FALSE, 'F03', 'FRECUENCIA'),
('P007', 'COMPARTIR INFORMACIÓN', '¿COMPARTE INFORMACIÓN ÚTIL CON EL RESTO DEL EQUIPO?', NULL, FALSE, 'F03', 'FRECUENCIA'),
('P008', 'TRABAJO COLABORATIVO', '¿PREFIERE TRABAJAR EN EQUIPO ANTES QUE EN FORMA INDIVIDUAL?', NULL, FALSE, 'F03', 'ACUERDO'),
('P009', 'MEDIACIÓN', '¿INTERVIENE PARA RESOLVER DESACUERDOS ENTRE COMPAÑEROS?', NULL, FALSE, 'F04', 'FRECUENCIA'),
('P010', 'MANEJO DE CRÍTICAS', '¿ACEPTA LAS CRÍTICAS SIN REACCIONAR DE FORMA DEFENSIVA?', NULL, FALSE, 'F04', 'ACUERDO'),
('P011', 'EXPOSICIÓN EN PÚBLICO', '¿SE SIENTE CÓMODO AL EXPONER FRENTE A UN GRUPO?', NULL, FALSE, 'F05', 'ACUERDO'),
('P012', 'ESCUCHA ACTIVA', '¿ESCUCHA CON ATENCIÓN ANTES DE RESPONDER?', NULL, FALSE, 'F05', 'FRECUENCIA'),
('P013', 'REDACCIÓN DE INFORMES', '¿REDACTA INFORMES CLAROS Y SIN ERRORES?', NULL, FALSE, 'F06', 'FRECUENCIA'),
('P014', 'CORREOS PROFESIONALES', '¿ADAPTA EL TONO DE SUS CORREOS AL DESTINATARIO?', NULL, FALSE, 'F06', 'FRECUENCIA'),
('P015', 'CUMPLIMIENTO DE PLAZOS', '¿CUMPLE CON LOS PLAZOS ACORDADOS?', NULL, FALSE, 'F07', 'FRECUENCIA'),
('P016', 'METAS DESAFIANTES', '¿SE PROPONE METAS MÁS EXIGENTES QUE LAS SOLICITADAS?', NULL, FALSE, 'F07', 'ACUERDO'),
('P017', 'ORGANIZACIÓN DE TAREAS', '¿ORGANIZA SUS TAREAS SEGÚN SU PRIORIDAD?', NULL, FALSE, 'F08', 'FRECUENCIA'),
('P018', 'ANTICIPACIÓN DE RIESGOS', '¿ANTICIPA LOS PROBLEMAS QUE PUEDEN AFECTAR UN PROYECTO?', NULL, FALSE, 'F08', 'FRECUENCIA'),
('P019', 'CAUSA RAÍZ', '¿BUSCA LA CAUSA DE UN PROBLEMA ANTES DE PROPONER UNA SOLUCIÓN?', NULL, FALSE, 'F09', 'FRECUENCIA'),
('P020', 'SOLUCIONES CREATIVAS', '¿PROPONE SOLUCIONES DISTINTAS A LAS HABITUALES?', NULL, FALSE, 'F09', 'FRECUENCIA'),
('P021', 'USO DE DATOS', '¿UTILIZA DATOS CONCRETOS PARA JUSTIFICAR SUS DECISIONES?', NULL, FALSE, 'F10', 'FRECUENCIA'),
('P022', 'DETECCIÓN DE PATRONES', '¿DETECTA TENDENCIAS EN LA INFORMACIÓN QUE ANALIZA?', NULL, FALSE, 'F10', 'ACUERDO'),
('P023', 'VERIFICACIÓN DE INFORMACIÓN', '¿VERIFICA LA EXACTITUD DE LOS DATOS QUE RECIBE?', NULL, FALSE, 'F10', 'FRECUENCIA'),
('P024', 'IDEAS ORIGINALES', '¿PROPONE IDEAS QUE NADIE HABÍA CONSIDERADO?', NULL, FALSE, 'F11', 'FRECUENCIA'),
('P025', 'CAMBIO DE PRIORIDADES', '¿SE ADAPTA RÁPIDAMENTE CUANDO CAMBIAN LAS PRIORIDADES?', NULL, FALSE, 'F13', 'FRECUENCIA'),
('P026', 'NUEVAS HERRAMIENTAS', '¿APRENDE CON FACILIDAD A USAR NUEVAS HERRAMIENTAS?', NULL, FALSE, 'F13', 'FRECUENCIA'),
('P027', 'TAREAS IMPREVISTAS', '¿ACEPTA TAREAS IMPREVISTAS SIN DIFICULTAD?', NULL, FALSE, 'F13', 'ACUERDO'),
('P028', 'REORGANIZACIONES', '¿MANTIENE SU RENDIMIENTO DURANTE UNA REORGANIZACIÓN?', NULL, FALSE, 'F14', 'ACUERDO'),
('P099', 'PREGUNTA DADA DE BAJA', '¿ESTA PREGUNTA NO DEBE APARECER EN LA GRILLA?', NULL, TRUE, 'F05', 'FRECUENCIA');

-- ---------------------------------------------------------------------
-- Ponderaciones de cada ítem para cada pregunta (suman 10: 0 + 1 + 2 + 3 + 4)
-- ---------------------------------------------------------------------
INSERT INTO pregunta_item_ponderacion (codigo_pregunta, nombre_opcion_respuesta, orden_item, ponderacion)
SELECT p.codigo_pregunta, i.nombre_opcion_respuesta, i.orden, i.orden - 1
FROM pregunta p
JOIN item_opcion i ON i.nombre_opcion_respuesta = p.nombre_opcion_respuesta;

-- ---------------------------------------------------------------------
-- Puestos y competencias requeridas (ponderación mínima 0..10)
--  1515 PROGRAMADOR         -> 2 evaluaciones, todos los estados de cuestionario
--  4845 ANALISTA FUNCIONAL  -> 1 evaluación
--  2147 JEFE DE PROYECTO    -> 1 evaluación sin cuestionarios COMPLETO
--  7845 SOPORTE TÉCNICO     -> SIN evaluaciones (flujo alternativo 6.A)
--  9999 PUESTO DADO DE BAJA -> eliminado, no debe aparecer
-- ---------------------------------------------------------------------
INSERT INTO puesto (codigo_puesto, nombre, descripcion, eliminado, id_empresa) VALUES
('1515', 'PROGRAMADOR', 'DESARROLLO DE SOFTWARE', FALSE, 1),
('4845', 'ANALISTA FUNCIONAL', 'RELEVAMIENTO Y ANÁLISIS DE REQUERIMIENTOS', FALSE, 1),
('2147', 'JEFE DE PROYECTO', 'GESTIÓN DE PROYECTOS', FALSE, 2),
('7845', 'SOPORTE TÉCNICO', 'ATENCIÓN DE INCIDENTES', FALSE, 3),
('3030', 'DISEÑADOR UX', 'DISEÑO DE EXPERIENCIA DE USUARIO', FALSE, 2),
('6060', 'ANALISTA DE DATOS', 'ANÁLISIS E INTERPRETACIÓN DE DATOS', FALSE, 3),
('9999', 'PUESTO DADO DE BAJA', 'NO DEBE APARECER', TRUE, 2);

INSERT INTO puesto_competencia (codigo_puesto, codigo_competencia, ponderacion_minima) VALUES
('1515', 'C02', 6),
('1515', 'C03', 4),
('1515', 'C05', 5),
('4845', 'C03', 6),
('4845', 'C04', 4),
('4845', 'C05', 5),
('2147', 'C01', 7),
('2147', 'C03', 5),
('2147', 'C04', 6),
('7845', 'C02', 4),
('7845', 'C03', 3),
-- 3030 DISEÑADOR UX: requiere CREATIVIDAD, que no es evaluable (CU025 9.A)
('3030', 'C03', 4),
('3030', 'C06', 5),
-- 6060 ANALISTA DE DATOS: todas evaluables (flujo completo CU025 -> CU026)
('6060', 'C05', 5),
('6060', 'C07', 6),
('9999', 'C01', 3);

-- ---------------------------------------------------------------------
-- Candidatos
-- ---------------------------------------------------------------------
INSERT INTO candidato (nro_candidato, nombre, apellido, tipo_documento, nro_documento, fecha_nacimiento, genero, nacionalidad, email, escolaridad, eliminado) VALUES
(1,  'JUAN',      'PÉREZ',     'DNI', '30111222', DATE '1985-03-12', 'H', 'ARGENTINA', 'JPEREZ@MAIL.COM',     'UNIVERSITARIO', FALSE),
(2,  'LUCÍA',     'GÓMEZ',     'DNI', '31222333', DATE '1986-07-25', 'M', 'ARGENTINA', 'LGOMEZ@MAIL.COM',     'UNIVERSITARIO', FALSE),
(3,  'MARTÍN',    'FERNÁNDEZ', 'DNI', '32333444', DATE '1988-01-30', 'H', 'ARGENTINA', 'MFERNANDEZ@MAIL.COM', 'TERCIARIO',     FALSE),
(4,  'SOFÍA',     'LÓPEZ',     'DNI', '33444555', DATE '1990-11-02', 'M', 'ARGENTINA', 'SLOPEZ@MAIL.COM',     'UNIVERSITARIO', FALSE),
(5,  'DIEGO',     'MARTÍNEZ',  'LE',  '5123456',  DATE '1975-05-19', 'H', 'ARGENTINA', 'DMARTINEZ@MAIL.COM',  'SECUNDARIO',    FALSE),
(6,  'VALENTINA', 'RODRÍGUEZ', 'DNI', '34555666', DATE '1991-09-14', 'M', 'ARGENTINA', 'VRODRIGUEZ@MAIL.COM', 'UNIVERSITARIO', FALSE),
(7,  'TOMÁS',     'SÁNCHEZ',   'DNI', '35666777', DATE '1993-02-08', 'H', 'ARGENTINA', 'TSANCHEZ@MAIL.COM',   'TERCIARIO',     FALSE),
(8,  'CAMILA',    'ROMERO',    'DNI', '36777888', DATE '1994-06-21', 'M', 'ARGENTINA', 'CROMERO@MAIL.COM',    'UNIVERSITARIO', FALSE),
(9,  'NICOLÁS',   'TORRES',    'PP',  'AB123456', DATE '1989-12-03', 'H', 'URUGUAYA',  'NTORRES@MAIL.COM',    'UNIVERSITARIO', FALSE),
(10, 'JULIETA',   'ÁLVAREZ',   'DNI', '37888999', DATE '1995-04-17', 'M', 'ARGENTINA', 'JALVAREZ@MAIL.COM',   'UNIVERSITARIO', FALSE),
(11, 'FACUNDO',   'RUIZ',      'DNI', '38999000', DATE '1996-08-29', 'H', 'ARGENTINA', 'FRUIZ@MAIL.COM',      'TERCIARIO',     FALSE),
(12, 'MARTINA',   'DÍAZ',      'DNI', '39000111', DATE '1997-10-10', 'M', 'ARGENTINA', 'MDIAZ@MAIL.COM',      'UNIVERSITARIO', FALSE),
(13, 'IGNACIO',   'MORALES',   'LC',  '4987654',  DATE '1972-01-22', 'H', 'ARGENTINA', 'IMORALES@MAIL.COM',   'SECUNDARIO',    FALSE),
(14, 'AGUSTINA',  'SUÁREZ',    'DNI', '40111222', DATE '1998-03-05', 'M', 'ARGENTINA', 'ASUAREZ@MAIL.COM',    'UNIVERSITARIO', FALSE),
(15, 'LUCAS',     'BENÍTEZ',   'DNI', '41222333', DATE '1999-07-16', 'H', 'ARGENTINA', 'LBENITEZ@MAIL.COM',   'TERCIARIO',     FALSE),
-- 16 a 20: nunca evaluados (se pueden evaluar en CU025)
(16, 'ANA',       'GARCÍA',    'DNI', '42000016', DATE '1993-05-11', 'M', 'ARGENTINA', 'AGARCIA@MAIL.COM',    'UNIVERSITARIO', FALSE),
(17, 'PABLO',     'MOLINA',    'DNI', '42000017', DATE '1990-08-23', 'H', 'ARGENTINA', 'PMOLINA@MAIL.COM',    'TERCIARIO',     FALSE),
(18, 'ELENA',     'CASTRO',    'DNI', '42000018', DATE '1988-02-14', 'M', 'ARGENTINA', 'ECASTRO@MAIL.COM',    'UNIVERSITARIO', FALSE),
(19, 'RAMIRO',    'ORTIZ',     'DNI', '42000019', DATE '1995-11-30', 'H', 'ARGENTINA', 'RORTIZ@MAIL.COM',     'SECUNDARIO',    FALSE),
(20, 'PAULA',     'SILVA',     'DNI', '42000020', DATE '1997-06-07', 'M', 'URUGUAYA',  'PSILVA@MAIL.COM',     'UNIVERSITARIO', FALSE),
-- 21 a 25: cuestionarios para probar CU026 (claves en la sección de cuestionarios)
(21, 'LAURA',     'VEGA',      'DNI', '42111001', DATE '1994-03-02', 'M', 'ARGENTINA', 'LVEGA@MAIL.COM',      'UNIVERSITARIO', FALSE),
(22, 'MATEO',     'ACOSTA',    'DNI', '42111002', DATE '1992-09-19', 'H', 'ARGENTINA', 'MACOSTA@MAIL.COM',    'TERCIARIO',     FALSE),
(23, 'CAROLINA',  'RÍOS',      'DNI', '42111003', DATE '1991-12-12', 'M', 'ARGENTINA', 'CRIOS@MAIL.COM',      'UNIVERSITARIO', FALSE),
(24, 'BRUNO',     'MÉNDEZ',    'DNI', '42111004', DATE '1989-04-25', 'H', 'ARGENTINA', 'BMENDEZ@MAIL.COM',    'UNIVERSITARIO', FALSE),
(25, 'LUCÍA',     'HERRERA',   'DNI', '42111005', DATE '1996-10-08', 'M', 'ARGENTINA', 'LHERRERA@MAIL.COM',   'SECUNDARIO',    FALSE);

-- ---------------------------------------------------------------------
-- Evaluaciones
-- ---------------------------------------------------------------------
INSERT INTO evaluacion (id_evaluacion, fecha_creacion, cant_preguntas_por_bloque, codigo_puesto) VALUES
(1, DATE '2026-09-01', 2, '1515'),
(2, DATE '2026-10-15', 2, '1515'),
(3, DATE '2026-10-20', 3, '4845'),
(4, DATE '2026-11-02', 2, '2147');
-- Evaluaciones de ANALISTA DE DATOS con fechas relativas a hoy (para probar plazos en CU026)
INSERT INTO evaluacion (id_evaluacion, fecha_creacion, cant_preguntas_por_bloque, codigo_puesto) VALUES
(5, CURRENT_DATE, 4, '6060'),
(6, CAST(CURRENT_DATE - INTERVAL '10' DAY AS DATE), 4, '6060');
ALTER TABLE evaluacion ALTER COLUMN id_evaluacion RESTART WITH 100;

-- ---------------------------------------------------------------------
-- Cuestionarios (atributos de DetalleCuestionario incluidos)
-- ---------------------------------------------------------------------
INSERT INTO cuestionario (clave, tiempo_maximo, id_evaluacion, nro_candidato, fecha_inicio, fecha_fin, estado, cantidad_accesos, ultimo_ingreso) VALUES
-- Evaluación 1 - PROGRAMADOR (mínimos: C02 >= 6, C03 >= 4, C05 >= 5)
('K1001', 60, 1, 1,  TIMESTAMP '2026-09-03 09:00:00', TIMESTAMP '2026-09-03 10:05:00', 'COMPLETO',      1, TIMESTAMP '2026-09-03 09:00:00'),
('K1002', 60, 1, 2,  TIMESTAMP '2026-09-03 11:00:00', TIMESTAMP '2026-09-04 12:30:00', 'COMPLETO',      2, TIMESTAMP '2026-09-04 11:40:00'),
('K1003', 60, 1, 3,  TIMESTAMP '2026-09-04 08:15:00', TIMESTAMP '2026-09-04 09:20:00', 'COMPLETO',      1, TIMESTAMP '2026-09-04 08:15:00'),
('K1004', 60, 1, 4,  TIMESTAMP '2026-09-04 10:00:00', NULL,                            'INCOMPLETO',    3, TIMESTAMP '2026-09-05 18:20:00'),
('K1005', 60, 1, 5,  TIMESTAMP '2026-09-06 11:00:00', NULL,                            'EN_PROCESO',    1, TIMESTAMP '2026-09-06 11:00:00'),
-- Evaluación 2 - PROGRAMADOR
('K2001', 60, 2, 6,  TIMESTAMP '2026-10-16 09:30:00', TIMESTAMP '2026-10-16 10:40:00', 'COMPLETO',      1, TIMESTAMP '2026-10-16 09:30:00'),
('K2002', 60, 2, 7,  TIMESTAMP '2026-10-16 14:00:00', TIMESTAMP '2026-10-17 15:10:00', 'COMPLETO',      2, TIMESTAMP '2026-10-17 14:45:00'),
('K2003', 60, 2, 8,  NULL,                            NULL,                            'ACTIVO',        0, NULL),
('K2004', 60, 2, 9,  NULL,                            NULL,                            'SIN_CONTESTAR', 0, NULL),
('K2005', 60, 2, 15, TIMESTAMP '2026-10-18 16:00:00', TIMESTAMP '2026-10-18 17:00:00', 'COMPLETO',      1, TIMESTAMP '2026-10-18 16:00:00'),
-- Evaluación 3 - ANALISTA FUNCIONAL (mínimos: C03 >= 6, C04 >= 4, C05 >= 5)
('K3001', 60, 3, 10, TIMESTAMP '2026-10-21 09:00:00', TIMESTAMP '2026-10-21 10:15:00', 'COMPLETO',      1, TIMESTAMP '2026-10-21 09:00:00'),
('K3002', 60, 3, 11, TIMESTAMP '2026-10-21 11:00:00', TIMESTAMP '2026-10-22 09:50:00', 'COMPLETO',      2, TIMESTAMP '2026-10-22 09:05:00'),
('K3003', 60, 3, 12, TIMESTAMP '2026-10-22 15:00:00', NULL,                            'INCOMPLETO',    4, TIMESTAMP '2026-10-24 19:30:00'),
-- Evaluación 4 - JEFE DE PROYECTO (ningún cuestionario COMPLETO)
('K4001', 60, 4, 13, TIMESTAMP '2026-11-03 10:00:00', NULL,                            'EN_PROCESO',    2, TIMESTAMP '2026-11-04 08:30:00'),
('K4002', 60, 4, 14, NULL,                            NULL,                            'ACTIVO',        0, NULL);

-- CU026 (tiempo máximo 20 minutos; plazo para comenzar 7 días; máximo 3 accesos):
--  ACTIVA27 VEGA     DNI 42111001: ACTIVO vigente -> instrucciones y cuestionario completo
--  VENCID38 ACOSTA   DNI 42111002: ACTIVO creado hace 10 días -> SIN_CONTESTAR ("Tiempo de espera alcanzado.")
--  PROCES49 RÍOS     DNI 42111003: EN_PROCESO, bloque 1 respondido -> retoma en el bloque 2 de 2
--  TIEMPO52 MÉNDEZ   DNI 42111004: EN_PROCESO iniciado hace 30 minutos -> INCOMPLETO ("Tiempo máximo alcanzado.")
--  ACCESO63 HERRERA  DNI 42111005: EN_PROCESO con 3 accesos -> el 4.º la deja INCOMPLETO ("Cantidad máxima de accesos alcanzada.")
INSERT INTO cuestionario (clave, tiempo_maximo, id_evaluacion, nro_candidato, fecha_inicio, fecha_fin, estado, cantidad_accesos, ultimo_ingreso) VALUES
('ACTIVA27', 20, 5, 21, NULL,                                  NULL, 'ACTIVO',     0, NULL),
('VENCID38', 20, 6, 22, NULL,                                  NULL, 'ACTIVO',     0, NULL),
('PROCES49', 20, 5, 23, LOCALTIMESTAMP - INTERVAL '5' MINUTE,  NULL, 'EN_PROCESO', 1, LOCALTIMESTAMP - INTERVAL '5' MINUTE),
('TIEMPO52', 20, 5, 24, LOCALTIMESTAMP - INTERVAL '30' MINUTE, NULL, 'EN_PROCESO', 1, LOCALTIMESTAMP - INTERVAL '30' MINUTE),
('ACCESO63', 20, 5, 25, LOCALTIMESTAMP - INTERVAL '5' MINUTE,  NULL, 'EN_PROCESO', 3, LOCALTIMESTAMP - INTERVAL '2' MINUTE);

-- Preguntas asignadas a PROCES49 (2 por factor de C05 y C07; F14 tiene 1 sola pregunta y se omite),
-- mezcladas y en bloques de 4: el bloque 1 ya fue respondido.
INSERT INTO pregunta_cuestionario (id_pregunta_cuestionario, clave_cuestionario, codigo_pregunta, orden, nro_bloque) VALUES
(1, 'PROCES49', 'P021', 1, 1),
(2, 'PROCES49', 'P025', 2, 1),
(3, 'PROCES49', 'P019', 3, 1),
(4, 'PROCES49', 'P026', 4, 1),
(5, 'PROCES49', 'P023', 5, 2),
(6, 'PROCES49', 'P020', 6, 2);
ALTER TABLE pregunta_cuestionario ALTER COLUMN id_pregunta_cuestionario RESTART WITH 100;

INSERT INTO respuesta_seleccionada (id_respuesta_seleccionada, id_pregunta_cuestionario, nombre_opcion_respuesta, orden_item, fecha_respuesta) VALUES
(1, 1, 'FRECUENCIA', 4, LOCALTIMESTAMP - INTERVAL '3' MINUTE),
(2, 2, 'FRECUENCIA', 5, LOCALTIMESTAMP - INTERVAL '3' MINUTE),
(3, 3, 'FRECUENCIA', 3, LOCALTIMESTAMP - INTERVAL '3' MINUTE),
(4, 4, 'FRECUENCIA', 4, LOCALTIMESTAMP - INTERVAL '3' MINUTE);
ALTER TABLE respuesta_seleccionada ALTER COLUMN id_respuesta_seleccionada RESTART WITH 100;

-- ---------------------------------------------------------------------
-- Resultados por competencia (solo cuestionarios COMPLETO)
-- Puntaje = suma de los resultados de las competencias del puesto
-- ---------------------------------------------------------------------
INSERT INTO resultado_competencia (clave_cuestionario, codigo_competencia, total) VALUES
-- K1001 PÉREZ: 8 + 7 + 9 = 24 -> cumple todos los mínimos
('K1001', 'C02', 8), ('K1001', 'C03', 7), ('K1001', 'C05', 9),
-- K1002 GÓMEZ: 6 + 4 + 5 = 15 -> cumple (justo en el mínimo)
('K1002', 'C02', 6), ('K1002', 'C03', 4), ('K1002', 'C05', 5),
-- K1003 FERNÁNDEZ: 9 + 9 + 4 = 22 -> NO cumple (C05 < 5) aunque supera en puntaje a GÓMEZ
('K1003', 'C02', 9), ('K1003', 'C03', 9), ('K1003', 'C05', 4),
-- K2001 RODRÍGUEZ: 7 + 8 + 9 = 24 -> cumple (empata con PÉREZ, se desempata por apellido)
('K2001', 'C02', 7), ('K2001', 'C03', 8), ('K2001', 'C05', 9),
-- K2002 SÁNCHEZ: 5 + 3 + 6 = 14 -> NO cumple (C02 < 6 y C03 < 4)
('K2002', 'C02', 5), ('K2002', 'C03', 3), ('K2002', 'C05', 6),
-- K2005 BENÍTEZ: 10 + 10 = 20 -> NO cumple (no tiene resultado para C05)
('K2005', 'C02', 10), ('K2005', 'C03', 10),
-- K3001 ÁLVAREZ: 8 + 6 + 7 = 21 -> cumple
('K3001', 'C03', 8), ('K3001', 'C04', 6), ('K3001', 'C05', 7),
-- K3002 RUIZ: 5 + 9 + 9 = 23 -> NO cumple (C03 < 6)
('K3002', 'C03', 5), ('K3002', 'C04', 9), ('K3002', 'C05', 9);
