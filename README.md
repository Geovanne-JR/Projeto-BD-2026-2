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
| `perfis`, `usuarios`, `rede_local`, `dispositivo`, `servidor_dns`, `cat_dominio`, `dominio`, `lista_bloqueio`, `regra_personalizada`, `requisicoes`, `alertas_seguranca` | `usam` (rede × servidor DNS), `contem` (lista × domínio), `tem` (dispositivo × lista) |

Os nomes acima são os do script `sql/schema.sql`. A documentação da Entrega 1 usa os nomes conceituais do MER (`usuario`, `requisicao_dns`, `rede_servidor_dns`...).

A documentação completa (MER, cardinalidades, modelo relacional e dicionário de dados) está em [`docs/entrega1/entrega1.md`](docs/entrega1/entrega1.md).

## Stack

| Camada | Tecnologia |
|---|---|
| SGBD | **PostgreSQL** (14+) |
| Aplicação / CRUD | **Java 21** + **JavaFX** (interface gráfica) + **JDBC** (persistência), com Maven |
| Modelagem | brModelo (DER e modelo relacional) |

## Estrutura do repositório

```
Projeto-BD-2026-2/
├── README.md
├── .gitignore
├── docs/
│   ├── entrega1/
│   │   └── entrega1.md        # Introdução, MER/DER, Modelo Relacional, Uso de IA
│   └── entrega2/
│       └── entrega2.md        # Persistência, mapeamento, CRUD, view/procedure/triggers, uso de IA
├── sql/
│   ├── criar_banco.sql        # Roda os três scripts abaixo, em ordem
│   ├── schema.sql             # DDL: tabelas, tipos e índices
│   ├── objetos.sql            # View, procedure e triggers
│   └── dados.sql              # Carga de dados de exemplo
├── pom.xml                    # Projeto Maven (JavaFX + driver PostgreSQL)
└── src/main/
    ├── resources/
    │   └── db.properties.example  # Copiar para db.properties (não versionado)
    └── java/br/unb/bd/pihole/
        ├── Launcher.java / App.java   # Ponto de entrada e janela principal (abas)
        ├── modelo/                    # Classes que espelham as tabelas
        ├── persistencia/              # ConexaoBD, interface DAO, um DAO por tabela
        ├── ui/                        # Telas JavaFX (uma View por aba; CadastroView é a base)
        └── util/                      # Hash de senha e formatação
```

### Camadas da aplicação

```
ui (DispositivoView)  →  persistencia (DispositivoDAO)  →  JDBC (ConexaoBD)  →  PostgreSQL
       ↑                             │
       └──── modelo (Dispositivo) ───┘   (objetos trafegam entre as camadas)
```

A interface nunca escreve SQL: ela chama métodos do DAO, que devolvem objetos do modelo.
Erros do banco (FK, UNIQUE, CHECK) chegam à tela como `PersistenciaException`, já com uma mensagem amigável.

Para cadastrar uma nova tabela, siga o padrão de `Dominio` + `DominioDAO` + `DominioView` e adicione a aba em `App.java`.

Os detalhes (diagramas, mapeamento, view/procedure/triggers) estão em [`docs/entrega2/entrega2.md`](docs/entrega2/entrega2.md).

## Como criar o banco

```bash
createdb -U postgres pihole_bd
psql -U postgres -d pihole_bd -f sql/criar_banco.sql
```

O script recria tabelas, view, procedure e triggers e carrega os dados de exemplo, então pode ser executado várias vezes. Todos os usuários de exemplo têm a senha `senha123`.

## Como rodar a aplicação

Requisitos: JDK 21+ e Maven. O IntelliJ e o VS Code (Extension Pack for Java) já trazem Maven embutido.

1. Crie o banco (seção acima).
2. Copie `src/main/resources/db.properties.example` para `db.properties` e ajuste usuário e senha.
3. Rode `mvn javafx:run`, ou execute `br.unb.bd.pihole.Launcher` pela IDE.

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
- [x] Camada de persistência + diagrama de como a interface acessa essa camada (`entrega2.md`, seção 2)
- [x] Diagrama da camada de mapeamento de uma tabela (`entrega2.md`, seção 3: `usuarios` ↔ `Usuario`)
- [x] Programa com CRUD, incluindo funcionalidades com **2 ou mais tabelas relacionadas** (dispositivo × listas, lista × domínios, rede × servidores)
- [x] **View** `vw_historico_requisicoes` (aba Histórico DNS)
- [x] **Procedure** `registrar_requisicao` (botão "Simular requisição")
- [x] **Triggers** `trg_alerta_requisicao` e `trg_contem_atualiza_lista`
- [x] Dado binário no banco (`usuarios.foto_perfil BYTEA`), com upload na aba Usuários
- [x] Carga de dados de exemplo (`sql/dados.sql`)
- [x] Indicação do uso de IA (`entrega2.md`, seção 9; falta o grupo completar a responsabilidade)
- [ ] Gerar o PDF da entrega (com o link do GitHub no início) e preencher os integrantes

## Uso de IA

O grupo usou ferramentas de IA (Claude e Gemini) como apoio. O detalhamento por etapa está na seção **"Indicação do Uso de IA"** de cada documento em [`docs/`](docs/).
