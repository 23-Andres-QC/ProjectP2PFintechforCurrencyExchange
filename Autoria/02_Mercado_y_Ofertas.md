# 2. Mercado y ofertas

Mercado principal, Publicar oferta, Mis ofertas.

## Mercado principal

- **Debe hacer:** mostrar ofertas activas de otros (nunca propias), mostrar tasas de cambio reales, permitir filtrar y comprar/match.
- **Debe ver:** en el header azul, a la izquierda el logo "PeruExchange"; a la derecha, primero un circulo con la inicial del usuario (sin nombre, solo el icono) que lleva al Perfil, y luego la campana de notificaciones con su contador. Debajo del header, el carrusel de tasas contra el sol; debajo, **una sola tarjeta** con los 3 elementos adentro (asi no compiten en tamano/borde entre si): tipo de cambio a la izquierda (arriba el par de monedas, ej. "USD → PEN", abajo el valor, ej. "PEN 3.413"), boton de texto **Filtro**, boton solido **Matching**. El boton Filtro abre un dialogo centrado en la pantalla (no un panel que sube desde abajo) con titulo "Filtrar por moneda" centrado, los selectores "Tengo"/"Quiero", el boton de intercambiar, y "Aplicar filtro" para cerrarlo — ahi es donde se ve y se cambia el filtro de moneda actual, no en la tarjeta principal.
- **Debe llenar:** nada (solo filtros/seleccion, dentro del panel flotante).
- **Se espera:** carga rapido, carrusel sin huecos ni saltos, comprar/match lleva a una transaccion valida.

## Publicar oferta

- **Debe hacer:** validar KYC aprobado, cuenta bancaria registrada, montos positivos, limites min/max coherentes, antes de crear la oferta.
- **Debe ver:** que moneda vende, que moneda recibe, tasa explicada con ejemplo, errores antes de enviar.
- **Debe llenar:** moneda, monto, tasa, limite minimo, limite maximo, cuenta bancaria/metodo de pago (preseleccionada la predeterminada).
- **Se espera:** solo un usuario verificado (KYC aprobado) y con cuenta bancaria publica; la oferta aparece en el Mercado de otros.

## Mis ofertas

- **Debe hacer:** listar ofertas propias, permitir pausar/cerrar/editar sin romper transacciones ya creadas. Debe incluir **4 pestanas**: Todas, Activas, Pausadas y **Finalizadas** (ofertas ya cerradas — vendidas por completo o eliminadas por el vendedor), cada una con su numerito de conteo. Antes las ofertas cerradas se ocultaban por completo del listado; ahora quedan visibles como historial en la pestana Finalizadas.
- **Debe ver:** estado (activa/pausada/finalizada), disponible vs. monto original, confirmacion antes de accion destructiva. Una oferta finalizada no muestra botones de "Pausar/Reanudar" ni "Eliminar" (no aplican a algo ya cerrado).
- **Debe llenar:** los campos que se editen (precio, limites, estado).
- **Se espera:** gestionar una oferta no debe afectar transacciones ya en curso; el vendedor puede revisar su historial de ofertas finalizadas sin que desaparezcan.
