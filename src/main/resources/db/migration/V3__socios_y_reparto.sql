-- Fase 1: socios y reparto flexible de comisiones.

create table socio (
    id_socio integer not null auto_increment,
    id_vendedor integer not null,
    nombres varchar(120) not null,
    apellidos varchar(120),
    documento varchar(15),
    telefono varchar(100),
    activo bit not null,
    fecha_registro datetime(6) not null,
    primary key (id_socio)
) engine=InnoDB;

create index idx_socio_tenant on socio (id_vendedor);

create table comision_reparto (
    id_reparto integer not null auto_increment,
    id_vendedor integer not null,
    id_comision integer not null,
    id_socio integer not null,
    participa_inicial bit not null,
    porcentaje_inicial decimal(5,4),
    monto_inicial decimal(14,2),
    participa_cuotas bit not null,
    porcentaje_cuotas decimal(5,4),
    monto_cuota_socio decimal(14,2),
    primary key (id_reparto)
) engine=InnoDB;

create index idx_reparto_tenant on comision_reparto (id_vendedor);
create index idx_reparto_comision on comision_reparto (id_comision);
alter table comision_reparto add constraint uk_reparto_comision_socio unique (id_comision, id_socio);

-- % del inicial del cliente aplicado al anticipo (snapshot por comisión).
alter table comision add column porcentaje_anticipo decimal(5,4);
