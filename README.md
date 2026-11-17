# Projeto BD 2026/2 — Monitoramento de Rede Doméstica e Bloqueio de Anúncios via DNS

**Repositório oficial:** <https://github.com/Geovanne-JR/Projeto-BD-2026-2>

Projeto da disciplina **Banco de Dados** — Departamento de Ciência da Computação, Universidade de Brasília (UnB), semestre 2026/2.

---

## Sobre o sistema

Sistema inspirado no **[Pi-hole](https://pi-hole.net/)** para **monitorar o tráfego DNS de uma rede doméstica** e **bloquear anúncios, rastreadores e domínios maliciosos** em todos os dispositivos da rede, sem instalar nada em cada aparelho.

O banco de dados permite:

- cadastrar **usuários** (com foto de perfil em binário) e **perfis** de acesso;
- mapear **redes locais**, os **dispositivos** conectados (identificados pelo MAC) e os **servidores DNS** usados;
- gerenciar **listas de bloqueio**, **domínios** e suas **categorias de risco**;
- criar **regras personalizadas** para bloquear ou liberar um domínio em um dispositivo específico;
- registrar o **histórico de requisições DNS** e gerar **alertas de segurança**.

### Modelo de dados

**11 entidades** (o mínimo exigido é 10) e **3 tabelas associativas**, totalizando 14 tabelas:

| Entidades | Associativas (N:N) |
|---|---|
| `perfil`, `usuario`, `rede_local`, `dispositivo`, `servidor_dns`, `categoria_dominio`, `dominio`, `lista_bloqueio`, `regra_personalizada`, `requisicao_dns`, `alerta_seguranca` | `rede_servidor_dns`, `lista_dominio`, `dispositivo_lista` |

A documentação completa (MER, cardinalidades, modelo relacional e dicionário de dados) está em [`docs/entrega1/entrega1.md`](docs/entrega1/entrega1.md).

## Stack

| Camada | Tecnologia |
|---|---|
| SGBD | **PostgreSQL** (14+) |
| Aplicação / CRUD | **Java** + **JavaFX** (interface gráfica) + **JDBC** (persistência), na entrega final |
| Modelagem | brModelo (DER e modelo relacional) |

## Estrutura do repositório

```
Projeto-BD-2026-2/
├── README.md
├── .gitignore
├── docs/
│   ├── entrega1/
│   │   └── entrega1.md        # Introdução, MER/DER, Modelo Relacional, Uso de IA
│   └── entrega2/              # Documentação da entrega final
├── sql/
│   └── schema.sql             # Script DDL que gera o banco de dados
└── src/
    └── main/
        └── java/              # Código da aplicação CRUD (entrega final)
```

## Como criar o banco

```bash
createdb pihole_bd
psql -U postgres -d pihole_bd -f sql/schema.sql
```

O script remove e recria todas as tabelas, então pode ser executado várias vezes.

## Requisitos da disciplina

**Gerais**
- Grupo de 2 a 4 estudantes; todos os integrantes apresentam.
- Mínimo de **10 entidades**.
- Tabelas principais com **pelo menos 10 registros**.
- **CRUD funcionando**, com **interface gráfica** para um usuário leigo.
- Qualquer SGBD relacional e qualquer linguagem.
- Indicar **onde e com qual objetivo** a IA foi usada.

**Entrega 1 — 17/11** (um único PDF no Aprender3)
- [x] Introdução
- [x] Modelo Entidade-Relacionamento (ferramenta de modelagem)
- [x] Modelo Relacional (ferramenta de modelagem)
- [x] Script SQL que gera o banco
- [x] Indicação do uso de IA

**Entrega final — 01/12 e 03/12** (PDF com o link do GitHub no início)
- [x] Gerador automático de chave primária em pelo menos uma tabela (`GENERATED ALWAYS AS IDENTITY`)
- [ ] Camada de persistência + diagrama de como a interface acessa essa camada
- [ ] Diagrama da camada de mapeamento de uma tabela
- [ ] Programa com CRUD, incluindo uma funcionalidade que envolva **2 ou mais tabelas relacionadas** (integridade referencial)
- [ ] Pelo menos uma **View**
- [ ] Pelo menos uma **Procedure**
- [ ] Pelo menos um **Trigger**
- [x] Dado binário no banco (`usuario.foto_perfil BYTEA`), modelado; falta implementar o upload no CRUD
- [ ] Carga de dados de exemplo
- [ ] Indicação do uso de IA

## Uso de IA

O grupo usou ferramentas de IA (Claude e Gemini) como apoio. O detalhamento por etapa está na seção **"Indicação do Uso de IA"** de cada documento em [`docs/`](docs/).
