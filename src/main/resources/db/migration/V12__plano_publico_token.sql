-- Enlace público de solo lectura para compartir el plano por WhatsApp.

alter table proyecto add column plano_public_token varchar(36) null;

update proyecto
set plano_public_token = uuid()
where plano_public_token is null;

create unique index uk_proyecto_plano_publico on proyecto (plano_public_token);
