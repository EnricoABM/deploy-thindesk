# Thindesk

![dashboard](docs/dashboard.png)

Sistema web de **help desk** para registro e acompanhamento de chamados de clientes, com controle de acesso por perfil de usuário (Atendente, Técnico e Administrador).

* **Deploy:** https://deploy-thindesk.onrender.com


### Funcionalidades

- **Autenticação**. 
- **Dashboard**.
- **Gestão de Clientes**.
- **Gestão de Chamados**.
- **Gestão de Usuários**.

### Matriz de permissões

| Funcionalidade                       |               Atendente               |                      Técnico                      |      Administrador       |
| ------------------------------------ | :-----------------------------------: | :-----------------------------------------------: | :----------------------: |
| Ver dashboard                        | :white_check_mark: chamados que abriu |   :white_check_mark: chamados atribuídos a ele    | :white_check_mark: todos |
| Listar e visualizar clientes         |          :white_check_mark:           |                :white_check_mark:                 |    :white_check_mark:    |
| Cadastrar, editar e excluir clientes |          :white_check_mark:           |                        :x:                        |    :white_check_mark:    |
| Listar e visualizar chamados         | :white_check_mark: chamados que abriu | :white_check_mark: sem técnico e atribuídos a ele | :white_check_mark: todos |
| Abrir chamado                        |          :white_check_mark:           |                        :x:                        |    :white_check_mark:    |
| Editar dados do chamado              | :white_check_mark: chamados que abriu |                        :x:                        |    :white_check_mark:    |
| Assumir chamado sem técnico          |                  :x:                  |                :white_check_mark:                 |    :white_check_mark:    |
| Alterar status do chamado            |                  :x:                  |   :white_check_mark: chamados atribuídos a ele    |    :white_check_mark:    |
| Cancelar chamado                     | :white_check_mark: chamados que abriu |                        :x:                        |    :white_check_mark:    |
| Gerenciar usuários                   |                  :x:                  |                        :x:                        |    :white_check_mark:    |

## Stack

- **Java 21**
- **Spring Boot**
- **Thymeleaf**
- **MongoDB**
- **Lombok**
- **Maven**
- **Docker**
- **GitHub Actions** 
- **Render**

## Como executar

### Pré-requisitos

- Java 21, **ou** Docker e Docker Compose.

### Variáveis de ambiente

Copie o arquivo de exemplo e preencha os valores:

```bash
cp .env.example .env
```

| Variável                                       | Descrição                                                 |
| ---------------------------------------------- | --------------------------------------------------------- |
| `DB_USER`, `DB_PASSWORD`, `DB_HOST`, `DB_NAME` | Credenciais do MongoDB Atlas                              |
| `MONGO_URI`                                    | URI de conexão do MongoDB                                 |
| `ADMIN_USUARIO`                                | Login do administrador inicial                            |
| `ADMIN_SENHA_INICIAL`                          | Senha do administrador inicial                            |
| `FUSO_HORARIO`                                 | Fuso das datas da dashboard (padrão: `America/Sao_Paulo`) |

### Opção 1: Docker Compose (aplicação + MongoDB)

```bash
ADMIN_SENHA_INICIAL=suaSenha 

docker compose up -d --build
```

Acesse http://localhost:8080. 

Para parar, rode `docker compose down`.

### Opção 2: Maven com MongoDB local

```bash
docker compose up -d mongo

./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Opção 3: Maven com MongoDB Atlas

Com o `.env` preenchido:

```bash
./mvnw spring-boot:run
```

### Primeiro acesso

Entre com o usuário definido em `ADMIN_USUARIO` (padrão `admin`) e a senha de `ADMIN_SENHA_INICIAL`. Pela tela de usuários, cadastre atendentes e técnicos.

### Testes e build

```bash
./mvnw clean verify
```

## Deploy

A aplicação está publicada no Render, construída a partir do `Dockerfile` do projeto:

* https://deploy-thindesk.onrender.com
