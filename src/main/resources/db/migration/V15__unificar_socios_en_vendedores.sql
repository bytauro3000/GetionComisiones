-- Unifica el concepto "socio" dentro de "usuario" (vendedor asociado).
--
-- Antes existían dos registros para la misma persona:
--   * `socio`        -> solo reparto/liquidación, sin login.
--   * `usuario`      -> cuenta del vendedor asociado, con login.
-- A partir de ahora el reparto y las liquidaciones apuntan al usuario.
--
-- Pasos:
--   1) Crear usuarios (pendientes) para socios que no tengan cuenta.
--   2) Construir el mapa socio -> usuario.
--   3) Repuntar comision_reparto.id_socio y liquidacion_socio.id_socio.
--   4) Crear reparto 50/50 para comisiones ya atribuidas a un asociado.
--
-- NOTA: la tabla `socio` NO se elimina aquí para conservar el respaldo; queda
-- inactiva y podrá eliminarse en una migración posterior tras verificar datos.

create table socio_usuario_map (
    id_socio integer not null,
    id_usuario integer not null,
    primary key (id_socio)
) engine=InnoDB;

-- 1) Socios que ya tienen una cuenta de vendedor asociado del mismo jefe:
--    se vinculan por número de documento.
insert into socio_usuario_map (id_socio, id_usuario)
select s.id_socio, min(u.id_usuario)
from socio s
join usuario u
  on u.id_vendedor_jefe = s.id_vendedor
 and u.rol = 'VENDEDOR_ASOCIADO'
 and s.documento is not null and s.documento <> ''
 and (u.num_documento = s.documento or u.dni = s.documento)
group by s.id_socio;

-- 2) Socios sin cuenta: se crea una cuenta pendiente con contraseña aleatoria
--    (el jefe deberá restablecerla). El correo es provisional y editable.
insert into usuario (
    dni, fecha_registro, nombres, apellidos, contrasena, correo, telefono,
    estado, rol, id_vendedor_jefe, tipo_documento, num_documento
)
select
    case when coalesce(s.tipo_documento, 'DNI') = 'DNI'
              and char_length(s.documento) = 8 then s.documento else null end,
    now(6),
    s.nombres,
    coalesce(s.apellidos, ''),
    '$2a$10$L4HBgmFs.TtmLpN3gFdg9.E.p3EU7x/Ar9X6YunlER/lUjUAcjFWG',
    concat('socio_', s.id_socio, '@pendiente.local'),
    s.telefono,
    'pendiente',
    'VENDEDOR_ASOCIADO',
    s.id_vendedor,
    coalesce(s.tipo_documento, 'DNI'),
    s.documento
from socio s
where not exists (
    select 1 from socio_usuario_map m where m.id_socio = s.id_socio
);

-- 3) Mapear las cuentas recién creadas.
insert into socio_usuario_map (id_socio, id_usuario)
select s.id_socio, u.id_usuario
from socio s
join usuario u on u.correo = concat('socio_', s.id_socio, '@pendiente.local')
where not exists (
    select 1 from socio_usuario_map m where m.id_socio = s.id_socio
);

-- 4) Repuntar el reparto y las liquidaciones sociales hacia el usuario.
update comision_reparto r
join socio_usuario_map m on r.id_socio = m.id_socio
set r.id_socio = m.id_usuario;

update liquidacion_socio l
join socio_usuario_map m on l.id_socio = m.id_socio
set l.id_socio = m.id_usuario;

-- 5) Comisiones atribuidas a un vendedor asociado que aún no tienen su fila
--    de reparto: se crea el 50/50 (inicial y cuotas).
insert into comision_reparto (
    id_vendedor, id_comision, id_socio,
    participa_inicial, porcentaje_inicial, monto_inicial,
    participa_cuotas, porcentaje_cuotas, monto_cuota_socio
)
select
    c.id_vendedor, c.id_comision, c.id_vendedor_asociado,
    1, 0.5000, null,
    1, 0.5000, null
from comision c
where c.id_vendedor_asociado is not null
  and not exists (
      select 1 from comision_reparto r
      where r.id_comision = c.id_comision
        and r.id_socio = c.id_vendedor_asociado
  );

drop table socio_usuario_map;
