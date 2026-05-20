# Documentacion de vistas React - Portal del estudiante

Este documento describe todas las vistas del frontend React para que puedas compartir contexto con otro modelo (por ejemplo Claude) y pedir propuestas de rediseno UX/UI orientadas a un sistema educativo.

## 1) Contexto tecnico rapido

- Stack: React + TypeScript + Vite + React Router.
- Layout invitado: `GuestShell` (home + autenticacion).
- Layout autenticado: `AppLayout` (dashboard, perfil, foro).
- Proteccion de rutas: `ProtectedRoute`.
- Estado global de sesion: `AuthProvider` + JWT en `localStorage`.
- Estilo actual: tokens CSS en `src/index.css` (tema claro, estetica academica).

## 2) Mapa de rutas y vistas

### Publicas (GuestShell)

- `/` -> `Home`
- `/login` -> `Login`
- `/register` -> `Register`
- `/forgot-password` -> `ForgotPassword`
- `/reset-password?token=...` -> `ResetPassword`
- `/confirm?token=...` -> `ConfirmEmail`

### Privadas (ProtectedRoute + AppLayout)

- `/dashboard` -> `Dashboard`
- `/profile` -> `Profile`
- `/forum/:courseId` -> `Forum`

### Comportamientos globales

- Ruta no encontrada: redirecciona a `/`.
- Carga diferida (lazy): usa `RouteFallback`.
- Error global de React: `RootErrorBoundary`.

## 3) Detalle por vista

## `Home`

- Objetivo: landing inicial del campus virtual.
- Audiencia: usuarios no autenticados (y autenticados que vuelven a inicio).
- Contenido:
  - Hero principal con propuesta de valor.
  - Tarjetas de beneficios: aprendizaje, foro, certificados.
  - CTA principal:
    - si autenticado -> "Ir a mi espacio"
    - si no autenticado -> "Iniciar sesion" y "Registrarme".
- Datos/API: no consume API.
- Oportunidades UX:
  - Agregar prueba social (estadisticas, testimonios cortos).
  - Mostrar rutas de uso por perfil (nuevo estudiante vs recurrente).
  - Reforzar onboarding visual del "primer paso".

## `Login`

- Objetivo: autenticar al estudiante.
- Campos:
  - correo
  - contrasena
- Acciones:
  - iniciar sesion
  - ir a recuperar contrasena
  - navegar a registro.
- Datos/API:
  - `POST /api/auth/login`
  - guarda `accessToken` y `refreshToken`.
- Estados:
  - `loading`, `error`.
  - redirige a la ruta previa (`from`) o `/dashboard`.
- Oportunidades UX:
  - Mostrar ayudas de error mas humanas (ej. credenciales invalidas vs servidor caido).
  - Opcion de mostrar/ocultar contrasena.
  - Mensaje de bienvenida contextual (nombre de institucion/ciclo).

## `Register`

- Objetivo: crear cuenta de estudiante.
- Campos:
  - nombre
  - apellido
  - correo
  - contrasena (minimo 6).
- Datos/API:
  - `POST /api/auth/register`
- Estados:
  - `loading`, `message`, `error`.
- Flujo:
  - crea usuario y pide confirmacion por correo.
- Oportunidades UX:
  - Medidor de fortaleza de contrasena.
  - Checklist visual de requisitos.
  - Mensaje posterior con "siguientes pasos" (revisar spam, tiempo estimado de correo).

## `ForgotPassword`

- Objetivo: solicitar enlace de recuperacion.
- Campo: correo.
- Datos/API:
  - `POST /api/auth/forgot-password?email=...`
- Estados:
  - `loading`, `message`, `error`.
- Oportunidades UX:
  - Texto neutro anti-enumeracion (ya existe parcialmente).
  - Confirmacion mas guiada ("si no llega en 5 min, revisa spam").
  - Boton secundario para volver a login siempre visible.

## `ResetPassword`

- Objetivo: definir nueva contrasena con token.
- Parametro obligatorio: `token` en querystring.
- Campo: nueva contrasena.
- Datos/API:
  - `POST /api/auth/reset-password?token=...&newPassword=...`
- Estados:
  - invalido sin token
  - `loading`, `message`, `error`.
- Oportunidades UX:
  - Validacion en vivo de requisitos.
  - Confirmacion de contrasena (campo doble).
  - Redireccion automatica a login tras exito.

## `ConfirmEmail`

- Objetivo: validar correo con token.
- Parametro: `token` en querystring.
- Datos/API:
  - `GET /api/auth/confirm?token=...`
- Particularidad tecnica:
  - evita doble solicitud en StrictMode (cache en `sessionStorage` + `Map` de promesas).
- Estados:
  - confirmando, exito, error, token faltante.
- Oportunidades UX:
  - Pantalla de estado mas ilustrativa (icono de exito/error).
  - CTA principal claro: "Ir a iniciar sesion".
  - Copia micro UX para resolver casos comunes.

## `Dashboard`

- Objetivo: vista principal del alumno autenticado.
- Secciones:
  - encabezado con nombre, correo y total de certificados.
  - cursos inscritos (progreso por modulos, acceso a foro, detalle API).
  - catalogo disponible (descripcion, instructor, precio, estado de inscripcion).
  - certificados emitidos con descarga PDF.
- Datos/API:
  - `GET /api/dashboard`
  - `GET /api/progress/{courseId}`
  - `GET /api/certificates/{email}` (fetch directo)
  - `GET /api/certificates/{id}/download` para PDF.
- Estados:
  - carga inicial
  - error con reintento
  - carga por curso para detalle de progreso.
- Oportunidades UX:
  - Priorizar tareas del dia ("continua este curso", "actividad pendiente").
  - Tarjetas con semaforo de avance y proximos hitos.
  - Separar "Catalogo" de "Mi aprendizaje" para menos carga cognitiva.
  - Evolucionar "Detalle API" a accion centrada en estudiante ("Ver avance detallado").

## `Profile`

- Objetivo: gestion de perfil del estudiante.
- Funciones:
  - ver datos base
  - editar nombre/apellido
  - subir foto de perfil
  - sincronizar con CRM.
- Datos/API:
  - `GET /api/profile`
  - `PUT /api/profile`
  - `POST /api/upload/profile-picture` (multipart)
  - `POST /api/crm/sync`
- Estados:
  - carga perfil
  - guardado en curso
  - mensajes de exito/error.
- Oportunidades UX:
  - Separar visualmente "Datos personales" y "Integraciones externas".
  - Feedback de subida de imagen con preview y barra de progreso.
  - Validaciones mas claras en campos editables.

## `Forum`

- Objetivo: comunicacion por curso.
- Parametro ruta: `courseId`.
- Funciones:
  - historial de mensajes
  - publicacion REST (persistente)
  - envio WebSocket (tiempo real)
  - indicador de estado del canal en vivo.
- Datos/API:
  - `GET /api/courses/{courseId}/forum?page=0&size=100`
  - `POST /api/courses/{courseId}/forum`
  - WS SockJS/STOMP:
    - endpoint: `/ws`
    - topic: `/topic/course/{courseId}/forum`
    - publish: `/app/forum/{courseId}`.
- Estados:
  - `wsState`: `off`, `connecting`, `live`
  - errores de carga/publicacion/canal.
- Oportunidades UX:
  - Input con soporte de Enter/Ctrl+Enter, contador y limites.
  - Agrupar mensajes por autor/fecha para lectura mas limpia.
  - Destacar mensajes propios y no leidos.
  - Disenar version "foro academico" (hilos, etiquetas, respuestas).

## 4) Componentes transversales relevantes para rediseno

- `GuestShell`: barra superior simple para usuarios no autenticados.
- `AppLayout`: navegacion principal autenticada (Panel, Perfil, Salir).
- `ProtectedRoute`: redirige a login si no hay sesion.
- `AuthContext`: login/logout/refresh session y datos de email.
- `RootErrorBoundary`: fallback si falla render de React.
- `RouteFallback`: estado de carga durante lazy loading.

## 5) Sistema visual actual (resumen)

- Tipografia:
  - display: Fraunces
  - UI: Plus Jakarta Sans.
- Colores base:
  - primario azul
  - acento teal
  - fondo claro con gradientes suaves.
- Componentes UI definidos en CSS:
  - botones (`primary`, `secondary`, `ghost`)
  - cards, alerts, badges
  - formularios, barras de progreso
  - layout responsive de tarjetas.

## 6) Dolor actual y oportunidades de producto educativo

- El flujo existe y funciona, pero puede verse "tecnico" en partes (ej. "Detalle API", mensajes de error muy crudos).
- Falta un concepto fuerte de "journey del estudiante" (proxima clase, pendientes, objetivo semanal).
- Foro aun se siente como chat tecnico; podria evolucionar a comunidad academica moderada.
- Perfil mezcla acciones personales y de integracion (CRM) en el mismo nivel de prioridad.
- Se puede reforzar motivacion y retencion con elementos de avance pedagogico (metas, streaks, recomendaciones).

## 7) Prompt listo para Claude (copiar/pegar)

Usa este prompt para pedir propuestas de rediseno:

```text
Actua como Senior Product Designer + UX Writer especializado en EdTech.
Quiero redisenar un portal del estudiante en React para que sea mas amigable, claro y motivador.

Contexto actual (vistas existentes):
- Home (landing)
- Login / Register
- ForgotPassword / ResetPassword / ConfirmEmail
- Dashboard (cursos inscritos, catalogo, certificados)
- Profile (edicion perfil, subida de foto, sync CRM)
- Forum por curso (historial + tiempo real)

Objetivo:
- Que se sienta como "Portal del Estudiante" moderno (academico, humano, confiable).
- Mejorar usabilidad, claridad de flujo y percepcion de valor para el alumno.
- Mantener implementacion realista para un proyecto React con backend ya hecho.

Quiero que me entregues:
1) Un concepto visual global (look & feel) con principios UX.
2) Arquitectura de informacion propuesta (navegacion y jerarquia).
3) Rediseno pantalla por pantalla (que moverias, que quitarias, que agregarias).
4) Microcopy sugerido (titulos, mensajes de vacio, errores, CTAs).
5) Componentes reutilizables de UI para estandarizar el sistema.
6) Priorizacion por fases:
   - Fase 1: quick wins (1-2 semanas)
   - Fase 2: mejoras medias
   - Fase 3: evolucion del producto
7) Lista de metricas UX para medir si el rediseno mejora (activacion, retencion, finalizacion de cursos, etc.).

Restricciones:
- No rompas el flujo funcional ya existente.
- Propone cambios incrementales, no un rewrite completo.
- Responde en espanol, con tablas o bullets accionables.
```

## 8) Recomendacion de implementacion incremental

Si vas a ejecutar el rediseno por etapas:

- Etapa A: autenticacion (login/register/recuperacion) + microcopy.
- Etapa B: dashboard centrado en progreso y tareas.
- Etapa C: perfil y comunidad (foro).
- Etapa D: sistema visual consolidado (tokens, componentes, estados vacios, skeletons).

