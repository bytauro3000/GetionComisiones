-- Flujo de aprobación de ventas registradas por vendedores socios.
-- El socio registra la venta en estado PENDIENTE; el jefe la aprueba o rechaza.

alter table venta
    modify column estado enum('PENDIENTE','ACTIVA','ANULADA','RECHAZADA') not null;

-- Parámetros de la venta que antes solo viajaban en el DTO y que ahora se
-- necesitan al momento de aprobar (para generar la comisión).
alter table venta
    add column monto_cuota decimal(14,2) null;

alter table venta
    add column porcentaje_anticipo decimal(5,4) null;
