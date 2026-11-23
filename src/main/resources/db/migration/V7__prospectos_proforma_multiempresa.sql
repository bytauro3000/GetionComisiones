-- Fase A/B/C: prospectos y seguimiento, proforma multiempresa y numeración por vendedor.

-- ── Cliente: prospecto + seguimiento comercial ──────────────────────────────
alter table cliente add column prospecto bit not null default 0;
alter table cliente add column estado_seguimiento varchar(20) not null default 'NUEVO';
alter table cliente add column nota_seguimiento varchar(300);
alter table cliente add column proximo_contacto date;

-- ── Proforma: empresa referencial opcional + snapshot por lote ──────────────
alter table proforma modify column id_empresa integer null;

alter table proforma_detalle add column id_empresa integer;
alter table proforma_detalle add column empresa_nombre varchar(150);
alter table proforma_detalle add column proyecto varchar(120);

-- ── Numeración de proformas por vendedor y año ───────────────────────────────
create table secuencia_proforma (
    id_secuencia_proforma integer not null auto_increment,
    id_vendedor integer not null,
    anio integer not null,
    ultimo_numero integer not null,
    primary key (id_secuencia_proforma)
) engine=InnoDB;

alter table secuencia_proforma
    add constraint uk_secuencia_proforma_tenant_anio unique (id_vendedor, anio);

-- ── Índices para filtros por fecha y seguimiento ────────────────────────────
create index idx_proforma_tenant_fecha on proforma (id_vendedor, fecha);
create index idx_proforma_cliente on proforma (id_cliente);
create index idx_proforma_detalle_proforma on proforma_detalle (id_proforma);
create index idx_cliente_tenant_prospecto on cliente (id_vendedor, prospecto);
create index idx_cliente_tenant_seguimiento on cliente (id_vendedor, estado_seguimiento);
