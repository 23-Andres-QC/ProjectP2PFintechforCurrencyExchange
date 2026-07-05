# 6. Soporte, legales y chatbot

## Soporte, ayuda y legales

- **Debe hacer:** dejar consultar ayuda/terminos/privacidad, y registrar reclamos (ver resolucion en el archivo de Disputas/Admin).
- **Debe ver:** textos legibles, confirmacion al enviar un reclamo.
- **Debe llenar:** categoria y mensaje del reclamo.
- **Se espera:** los textos de ayuda y legales no prometen nada que el sistema no haga de verdad.

### Validacion aplicada: soporte, reclamos y legales

- **Ayuda:** las preguntas frecuentes explican el flujo real: el comprador transfiere a la cuenta del vendedor, sube comprobante, y el vendedor revisa/libera desde la app.
- **Reclamos:** la app envia categoria y mensaje al backend; el backend valida tipos permitidos y mensaje obligatorio; la pantalla muestra confirmacion y lista los reclamos del usuario.
- **Estados legibles:** los codigos del backend (`pending`, `under_review`, `resolved`, `closed`) y tipos (`platform_error`, `transaction_issue`, etc.) se muestran con etiquetas claras.
- **Legales:** terminos, privacidad y contrato fueron ajustados para no prometer OCR, liberacion automatica, custodia bancaria, plazos garantizados, 24/7, comisiones no mostradas ni controles tecnicos no verificados.

## Chatbot

- **Debe hacer:** responder preguntas sobre el uso de la app, sin inventar reglas ni prometer funciones que no existen.
- **Debe ver:** boton flotante que no tape acciones importantes, chat legible en pantalla chica.
- **Debe llenar:** el mensaje/pregunta del usuario.
- **Se espera:** el chatbot ayuda sin contradecir la logica real del sistema.

### Validacion aplicada: chatbot

- **Prompt:** el asistente queda limitado a funciones reales de la app y debe admitir cuando algo no esta disponible.
- **Reglas reales incluidas:** cuenta del vendedor obligatoria para publicar, cuenta del comprador opcional para comprar, comprobantes revisados por usuario, disputas desde transaccion y reclamos desde soporte.
- **Boton flotante:** se mantiene solo en Mercado y se redujo de tamano para no tapar acciones importantes.
