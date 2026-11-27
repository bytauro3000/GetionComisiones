-- Migración de datos: actualización de lotes con el plano unificado.
-- Villa Familia: 154 lotes; Prudencia V Etapa: 163 lotes.
-- Fuente: plano nuevo + lista de precios actual.
-- Regla: precio = area_m2 * precio_m2; saldo = precio - inicial; 160 cuotas.
-- Respaldo recomendado antes de desplegar:
-- CREATE TABLE lote_backup_20261127 AS SELECT * FROM lote WHERE id_proyecto IN (6, 8);



-- 1. Villa Familia: actualizar lotes existentes con el plano y la lista nueva.
UPDATE lote SET area_m2 = 115.50, precio = 84315.00, saldo = 80565.00, precio_m2 = 730.00, inicial = 3750.00, descripcion = 'Lote espalda a avenida frente a parque' WHERE id_proyecto = 8 AND manzana = 'C' AND numero = '13';
UPDATE lote SET area_m2 = 119.41, precio = 90751.60, saldo = 86251.60, precio_m2 = 760.00, inicial = 4500.00, descripcion = 'Lote esquina avenida' WHERE id_proyecto = 8 AND manzana = 'D' AND numero = '1';
UPDATE lote SET area_m2 = 135.54, precio = 101655.00, saldo = 97405.00, precio_m2 = 750.00, inicial = 4250.00, descripcion = 'Lote avenida' WHERE id_proyecto = 8 AND manzana = 'D' AND numero = '2';
UPDATE lote SET area_m2 = 108.00, precio = 81000.00, saldo = 76750.00, precio_m2 = 750.00, inicial = 4250.00, descripcion = 'Lote avenida' WHERE id_proyecto = 8 AND manzana = 'D' AND numero = '3';
UPDATE lote SET area_m2 = 108.00, precio = 81000.00, saldo = 76750.00, precio_m2 = 750.00, inicial = 4250.00, descripcion = 'Lote avenida' WHERE id_proyecto = 8 AND manzana = 'D' AND numero = '4';
UPDATE lote SET area_m2 = 108.00, precio = 81000.00, saldo = 76750.00, precio_m2 = 750.00, inicial = 4250.00, descripcion = 'Lote avenida' WHERE id_proyecto = 8 AND manzana = 'D' AND numero = '5';
UPDATE lote SET area_m2 = 108.00, precio = 81000.00, saldo = 76750.00, precio_m2 = 750.00, inicial = 4250.00, descripcion = 'Lote avenida' WHERE id_proyecto = 8 AND manzana = 'D' AND numero = '6';
UPDATE lote SET area_m2 = 125.58, precio = 94185.00, saldo = 89935.00, precio_m2 = 750.00, inicial = 4250.00, descripcion = 'Lote avenida' WHERE id_proyecto = 8 AND manzana = 'D' AND numero = '7';
UPDATE lote SET area_m2 = 126.95, precio = 95212.50, saldo = 90962.50, precio_m2 = 750.00, inicial = 4250.00, descripcion = 'Lote avenida' WHERE id_proyecto = 8 AND manzana = 'D' AND numero = '8';
UPDATE lote SET area_m2 = 122.33, precio = 91747.50, saldo = 87497.50, precio_m2 = 750.00, inicial = 4250.00, descripcion = 'Lote avenida' WHERE id_proyecto = 8 AND manzana = 'D' AND numero = '9';
UPDATE lote SET area_m2 = 132.54, precio = 100730.40, saldo = 96230.40, precio_m2 = 760.00, inicial = 4500.00, descripcion = 'Lote esquina avenida' WHERE id_proyecto = 8 AND manzana = 'D' AND numero = '10';
UPDATE lote SET area_m2 = 91.00, precio = 59150.00, saldo = 57400.00, precio_m2 = 650.00, inicial = 1750.00, descripcion = 'Lote interior' WHERE id_proyecto = 8 AND manzana = 'D' AND numero = '11';
UPDATE lote SET area_m2 = 100.71, precio = 65461.50, saldo = 63711.50, precio_m2 = 650.00, inicial = 1750.00, descripcion = 'Lote interior' WHERE id_proyecto = 8 AND manzana = 'D' AND numero = '41';
UPDATE lote SET area_m2 = 106.45, precio = 69192.50, saldo = 67442.50, precio_m2 = 650.00, inicial = 1750.00, descripcion = 'Lote interior' WHERE id_proyecto = 8 AND manzana = 'D' AND numero = '42';
UPDATE lote SET area_m2 = 104.53, precio = 77352.20, saldo = 73352.20, precio_m2 = 740.00, inicial = 4000.00, descripcion = 'Lote esquina espalda a avenida' WHERE id_proyecto = 8 AND manzana = 'E' AND numero = '1';
UPDATE lote SET area_m2 = 91.00, precio = 60970.00, saldo = 58720.00, precio_m2 = 670.00, inicial = 2250.00, descripcion = 'Lote espalda calle principal' WHERE id_proyecto = 8 AND manzana = 'E' AND numero = '11';
UPDATE lote SET area_m2 = 98.00, precio = 65660.00, saldo = 63410.00, precio_m2 = 670.00, inicial = 2250.00, descripcion = 'Lote espalda calle principal' WHERE id_proyecto = 8 AND manzana = 'E' AND numero = '13';
UPDATE lote SET area_m2 = 112.00, precio = 75040.00, saldo = 72790.00, precio_m2 = 670.00, inicial = 2250.00, descripcion = 'Lote espalda calle principal' WHERE id_proyecto = 8 AND manzana = 'E' AND numero = '14';
UPDATE lote SET area_m2 = 121.80, precio = 84042.00, saldo = 81292.00, precio_m2 = 690.00, inicial = 2750.00, descripcion = 'Lote costado del parque' WHERE id_proyecto = 8 AND manzana = 'E' AND numero = '16';
UPDATE lote SET area_m2 = 112.00, precio = 76160.00, saldo = 73660.00, precio_m2 = 680.00, inicial = 2500.00, descripcion = 'Lote calle principal' WHERE id_proyecto = 8 AND manzana = 'E' AND numero = '17';
UPDATE lote SET area_m2 = 98.00, precio = 66640.00, saldo = 64140.00, precio_m2 = 680.00, inicial = 2500.00, descripcion = 'Lote calle principal' WHERE id_proyecto = 8 AND manzana = 'E' AND numero = '19';
UPDATE lote SET area_m2 = 91.00, precio = 61880.00, saldo = 59380.00, precio_m2 = 680.00, inicial = 2500.00, descripcion = 'Lote calle principal' WHERE id_proyecto = 8 AND manzana = 'E' AND numero = '28';

-- 2. Villa Familia: insertar los lotes nuevos D-51, E-29 y E-30.
INSERT INTO lote (id_empresa, id_vendedor, manzana, numero, precio, proyecto, id_proyecto, descripcion, inicial, num_cuotas, precio_m2, saldo, fecha_registro, estado, separacion_ajena) VALUES
(7, 2, 'D', '51', 64356.50, 'RESIDENCIAL VILLA FAMILIA', 8, 'Lote interior', 1750.00, 160, 650.00, 62606.50, NOW(6), 'DISPONIBLE', b'0'),
(7, 2, 'E', '29', 61880.00, 'RESIDENCIAL VILLA FAMILIA', 8, 'Lote calle principal', 2500.00, 160, 680.00, 59380.00, NOW(6), 'DISPONIBLE', b'0'),
(7, 2, 'E', '30', 85455.20, 'RESIDENCIAL VILLA FAMILIA', 8, 'Lote esquina espalda avenida', 4000.00, 160, 740.00, 81455.20, NOW(6), 'DISPONIBLE', b'0');

-- 3. Villa Familia: la lista nueva usa 160 cuotas.
UPDATE lote SET num_cuotas = 160 WHERE id_proyecto = 8;

-- 4. Prudencia V Etapa: insertar los 163 lotes del plano nuevo.
--    Proyecto 6 / RESIDENCIAL PRUDENCIA ETAPA 5 / empresa 5 / vendedor 2.
INSERT INTO lote (id_empresa, id_vendedor, manzana, numero, precio, proyecto, id_proyecto, descripcion, inicial, num_cuotas, precio_m2, saldo, fecha_registro, estado, separacion_ajena) VALUES
(5, 2, 'A', '1', 79456.10, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote en esquina frente al parque', 3250.00, 160, 710.00, 76206.10, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'A', '2', 71359.80, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote costado del parque', 2750.00, 160, 690.00, 68609.80, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'A', '3', 104604.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote costado del parque', 2750.00, 160, 690.00, 101854.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '1', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 75090.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '2', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 75090.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '3', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 75090.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '4', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 75090.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '5', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 75090.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '6', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 75090.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '7', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 75090.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '8', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 75090.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '9', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 75090.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '10', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 75090.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '11', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 75090.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '12', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 75090.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '13', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 75090.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '14', 90746.20, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote esquina espalda avenida', 4000.00, 160, 740.00, 86746.20, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '15', 60573.50, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 58823.50, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '16', 59741.50, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 57991.50, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '17', 143123.20, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote esquina avenida', 4500.00, 160, 760.00, 138623.20, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '18', 126000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 121750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '19', 126000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 121750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '20', 126000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 121750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '21', 126000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 121750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '22', 126000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 121750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '23', 126000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 121750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '24', 126000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 121750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '25', 126000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 121750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '26', 126000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 121750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'B', '27', 126000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 121750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '1', 105000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 100750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '2', 105000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 100750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '3', 105000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 100750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '4', 105000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 100750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '5', 105000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 100750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '6', 105000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 100750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '7', 105000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 100750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '8', 105000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 100750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '9', 105000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 100750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '10', 105000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 100750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '11', 105000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 100750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '12', 105000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 100750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '13', 122268.80, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote esquina avenida', 4500.00, 160, 760.00, 117768.80, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '14', 71181.50, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 69431.50, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '15', 99966.60, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote esquina espalda avenida', 4000.00, 160, 740.00, 95966.60, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '16', 83160.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida', 3500.00, 160, 720.00, 79660.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '17', 83160.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida', 3500.00, 160, 720.00, 79660.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '18', 83160.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida', 3500.00, 160, 720.00, 79660.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '19', 83160.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida', 3500.00, 160, 720.00, 79660.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '20', 83160.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida', 3500.00, 160, 720.00, 79660.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '21', 84315.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 80565.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '22', 84315.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 80565.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '23', 84315.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 80565.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '24', 84315.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 80565.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'C', '25', 84315.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida frente a parque', 3750.00, 160, 730.00, 80565.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '1', 116302.80, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote esquina avenida', 4500.00, 160, 760.00, 111802.80, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '2', 87750.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 83500.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '3', 78840.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 74590.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '4', 81067.50, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 76817.50, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '5', 81795.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 77545.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '6', 84397.50, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 80147.50, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '7', 89550.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 85300.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '8', 117195.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 112945.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '9', 105937.50, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote avenida', 4250.00, 160, 750.00, 101687.50, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '10', 92371.50, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 90621.50, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '11', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '12', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '13', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '14', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '15', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '16', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '17', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '18', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '19', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '20', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '21', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '22', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '23', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '24', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '25', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '26', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '27', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '28', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '29', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '30', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '31', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '32', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '33', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '34', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '35', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '36', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '37', 68250.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 66500.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '38', 79450.80, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote esquina interior', 2000.00, 160, 660.00, 77450.80, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '39', 97738.60, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote en esquina frente al parque', 3250.00, 160, 710.00, 94488.60, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '40', 63000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote frente al parque', 3000.00, 160, 700.00, 60000.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '41', 63000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote frente al parque', 3000.00, 160, 700.00, 60000.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '42', 63000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote frente al parque', 3000.00, 160, 700.00, 60000.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '43', 63000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote frente al parque', 3000.00, 160, 700.00, 60000.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '44', 63000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote frente al parque', 3000.00, 160, 700.00, 60000.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '45', 63000.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote frente al parque', 3000.00, 160, 700.00, 60000.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '46', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '47', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '48', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '49', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '50', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '51', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '52', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '53', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '54', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '55', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '56', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '57', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '58', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '59', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '60', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '61', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '62', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '63', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '64', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '65', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '66', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'D', '67', 58500.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote interior', 1750.00, 160, 650.00, 56750.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '1', 106737.60, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote esquina espalda avenida', 4000.00, 160, 740.00, 102737.60, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '2', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '3', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '4', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '5', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '6', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '7', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '8', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '9', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '10', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '11', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '12', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '13', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '14', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '15', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '16', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '17', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '18', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '19', 65280.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote calle principal', 2500.00, 160, 680.00, 62780.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '20', 70656.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote costado del parque', 2750.00, 160, 690.00, 67906.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '21', 68889.60, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote costado del parque', 2750.00, 160, 690.00, 66139.60, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '22', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '23', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '24', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '25', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '26', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '27', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '28', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '29', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '30', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '31', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '32', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '33', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '34', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '35', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '36', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '37', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '38', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '39', 62712.00, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda calle principal', 2250.00, 160, 670.00, 60462.00, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '40', 95430.40, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote esquina espalda avenida', 4000.00, 160, 740.00, 91430.40, NOW(6), 'DISPONIBLE', b'0'),
(5, 2, 'E', '41', 74980.80, 'RESIDENCIAL PRUDENCIA ETAPA 5', 6, 'Lote espalda a avenida', 3500.00, 160, 720.00, 71480.80, NOW(6), 'DISPONIBLE', b'0');

