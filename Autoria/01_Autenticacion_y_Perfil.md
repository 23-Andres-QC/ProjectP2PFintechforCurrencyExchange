# 1. Autenticacion y perfil

Welcome, Login, Registro, Terminos, KYC, Recuperar password, Perfil.

## Welcome

- **Debe hacer:** si hay sesion guardada, entrar directo al Mercado; si no, mostrar Welcome.
- **Debe ver:** logo + botones "Iniciar sesion" y "Registrarse".
- **Debe llenar:** nada.
- **Se espera:** sin sesion no se puede entrar a pantallas protegidas.

## Login

- **Debe hacer:** validar credenciales, iniciar sesion, guardar token, registrar dispositivo para notificaciones, entrar al Mercado.
- **Debe ver:** campos de correo/contrasena, mostrar/ocultar password, error claro si falla, loading mientras procesa.
- **Debe llenar:** correo, contrasena.
- **Se espera:** login correcto entra al Mercado; si falla el registro de notificaciones, el login igual se completa.

## Registro

- **Debe hacer:** crear la cuenta solo si acepto terminos; guardar esa aceptacion de forma auditable.
- **Debe ver:** formulario por pasos (datos, KYC, contrato, contrasena), checkbox de terminos obligatorio.
- **Debe llenar:** nombre, correo, DNI (8 digitos), contrasena + confirmacion (min. 8 caracteres), checkbox de terminos.
- **Se espera:** no se crea cuenta sin aceptar terminos; el backend guarda fecha/version/url de esa aceptacion.

## KYC

- **Debe hacer:** recibir documentos, dejarlos en revision (nunca auto-aprobar), bloquear publicar/comprar hasta aprobacion de un admin.
- **Debe ver:** estado de cada foto (pendiente/cargada/error), opcion de reemplazar, mensaje de revision/aprobado/rechazado.
- **Debe llenar:** foto DNI frente, foto DNI reverso, selfie, firma (si aplica).
- **Se espera:** subir documentos no aprueba solo; sin KYC aprobado no se puede publicar ni comprar.

## Recuperar password

- **Debe hacer:** enviar correo real de recuperacion, permitir cambiar la contrasena sin ayuda manual.
- **Debe ver:** mensaje neutral (no revela si el correo existe), confirmacion del siguiente paso.
- **Debe llenar:** correo.
- **Se espera:** el usuario recupera el acceso solo, sin intervencion manual.

## Perfil

- **Debe hacer:** mostrar datos reales del usuario (nombre, rol, KYC, reputacion), permitir editar datos permitidos, cerrar sesion.
- **Debe ver:** estado de KYC visible, accesos agrupados (cuenta, seguridad, soporte, legal, admin si aplica).
- **Debe llenar:** campos editables de perfil (no rol ni KYC).
- **Se espera:** perfil siempre con datos actuales del servidor; logout borra token y datos sensibles.
