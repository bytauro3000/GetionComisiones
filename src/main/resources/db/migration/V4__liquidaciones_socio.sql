-- Fase 2: liquidaciones (pagos) a socios con descuento de IGV y detracción.

create table liquidacion_socio (
    id_liquidacion integer not null auto_increment,
    id_vendedor integer not null,
    id_socio integer not null,
    fecha date not null,
    monto_bruto decimal(14,2) not null,
    descuento_igv decimal(14,2) not null,
    descuento_detraccion decimal(14,2) not null,
    monto_neto decimal(14,2) not null,
    referencia varchar(60),
    notas varchar(200),
    fecha_registro datetime(6) not null,
    primary key (id_liquidacion)
) engine=InnoDB;

create index idx_liquidacion_tenant on liquidacion_socio (id_vendedor);
