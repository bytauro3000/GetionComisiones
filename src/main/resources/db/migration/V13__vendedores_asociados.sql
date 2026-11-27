-- Jerarquía vendedor jefe -> vendedores asociados.
-- El jefe conserva el tenant; el asociado comparte el tenant del jefe.

alter table usuario
    add column id_vendedor_jefe int null;

alter table usuario
    modify column rol enum('ADMINISTRADOR','VENDEDOR','VENDEDOR_ASOCIADO') not null;

alter table venta
    add column id_vendedor_asociado int null;

alter table proforma
    add column id_vendedor_asociado int null;

alter table cliente
    add column id_vendedor_asociado int null;

alter table comision
    add column id_vendedor_asociado int null;

-- Cada asociado puede tener su propia ficha del mismo documento sin
-- compartirla con los demás. En MySQL/TiDB los NULL siguen permitiendo
-- varias filas del jefe, por lo que la unicidad del jefe se valida en servicio.
alter table cliente drop index uk_cliente_tenant_doc;
create unique index uk_cliente_owner_doc
    on cliente (id_vendedor, id_vendedor_asociado, num_documento);

create index idx_usuario_jefe on usuario (id_vendedor_jefe);
create index idx_venta_asociado on venta (id_vendedor_asociado);
create index idx_proforma_asociado on proforma (id_vendedor_asociado);
create index idx_cliente_asociado on cliente (id_vendedor_asociado);
create index idx_comision_asociado on comision (id_vendedor_asociado);
