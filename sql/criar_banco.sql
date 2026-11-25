-- Cria o banco completo, na ordem certa. Uso:
--   psql -U postgres -d pihole_bd -f sql/criar_banco.sql
\set ON_ERROR_STOP on
\ir schema.sql
\ir objetos.sql
\ir dados.sql
