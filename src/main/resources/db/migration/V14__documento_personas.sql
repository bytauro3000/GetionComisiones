-- Tipo y número de documento para personas que no son clientes.
-- Se conserva `usuario.dni` por compatibilidad con datos históricos.

alter table usuario
    add column tipo_documento varchar(20) null;

alter table usuario
    add column num_documento varchar(15) null;

update usuario
set tipo_documento = 'DNI',
    num_documento = dni
where dni is not null
  and num_documento is null;

alter table socio
    add column tipo_documento varchar(20) null;
