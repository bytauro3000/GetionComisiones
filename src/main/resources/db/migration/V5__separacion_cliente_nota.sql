-- Fase 3: datos de la separación (cliente y nota) para distinguirlas mejor.

alter table lote add column cliente_separacion varchar(150);
alter table lote add column nota_separacion varchar(300);
