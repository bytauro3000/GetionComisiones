-- Enlace público (no adivinable) para compartir el PDF de la proforma por WhatsApp.

alter table proforma add column public_token varchar(36);

update proforma set public_token = uuid() where public_token is null;

create unique index uk_proforma_public_token on proforma (public_token);
