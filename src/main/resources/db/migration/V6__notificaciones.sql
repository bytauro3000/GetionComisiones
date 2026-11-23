-- Fase 4: notificaciones internas del vendedor (avisos de separaciones).

create table notificacion (
    id_notificacion integer not null auto_increment,
    id_vendedor integer not null,
    tipo varchar(40) not null,
    titulo varchar(120) not null,
    mensaje varchar(300) not null,
    id_referencia integer,
    clave varchar(100),
    leida bit not null,
    fecha_creacion datetime(6) not null,
    primary key (id_notificacion)
) engine=InnoDB;

create index idx_notificacion_tenant on notificacion (id_vendedor);
alter table notificacion add constraint uk_notificacion_tenant_clave unique (id_vendedor, clave);
