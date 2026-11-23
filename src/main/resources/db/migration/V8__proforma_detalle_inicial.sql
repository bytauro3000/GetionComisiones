-- Inicial y n° de cuotas por lote en la proforma (permite simular una inicial menor).

alter table proforma_detalle add column inicial decimal(14,2);
alter table proforma_detalle add column num_cuotas integer;
