# 2. Mercado y ofertas

Mercado principal, Publicar oferta, Mis ofertas.

## Mercado principal

- **Debe hacer:** mostrar ofertas activas de otros (nunca propias), mostrar tasas de cambio reales, permitir filtrar y comprar/match.
- **Debe ver:** carrusel de tasas contra el sol, lista de ofertas, filtro de moneda.
- **Debe llenar:** nada (solo filtros/seleccion).
- **Se espera:** carga rapido, carrusel sin huecos ni saltos, comprar/match lleva a una transaccion valida.

## Publicar oferta

- **Debe hacer:** validar KYC aprobado, cuenta bancaria registrada, montos positivos, limites min/max coherentes, antes de crear la oferta.
- **Debe ver:** que moneda vende, que moneda recibe, tasa explicada con ejemplo, errores antes de enviar.
- **Debe llenar:** moneda, monto, tasa, limite minimo, limite maximo, cuenta bancaria/metodo de pago (preseleccionada la predeterminada).
- **Se espera:** solo un usuario verificado (KYC aprobado) y con cuenta bancaria publica; la oferta aparece en el Mercado de otros.

## Mis ofertas

- **Debe hacer:** listar ofertas propias, permitir pausar/cerrar/editar sin romper transacciones ya creadas.
- **Debe ver:** estado (activa/pausada/cerrada), disponible vs. monto original, confirmacion antes de accion destructiva.
- **Debe llenar:** los campos que se editen (precio, limites, estado).
- **Se espera:** gestionar una oferta no debe afectar transacciones ya en curso.
