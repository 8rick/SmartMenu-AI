# SmartMenu AI

API RESTful inteligente projetada para ajudar restaurantes a reduzirem o desperdício de alimentos através do uso de Inteligência Artificial.

## 🚀 Sobre o Projeto

O SmartMenu AI analisa dados e oferece insights para otimizar o cardápio e o estoque de restaurantes. Utilizando o ecossistema Spring (junto com o Spring AI e a API da OpenAI), o sistema é capaz de gerar recomendações precisas para minimizar o descarte de ingredientes e maximizar a eficiência operacional do estabelecimento.

## 🛠 Tecnologias

Este projeto foi construído com as seguintes tecnologias:

- **Java 21**
- **Spring Boot 3.5.5** (Web, Data JPA, Validation)
- **Spring AI** (Integração com OpenAI)
- **PostgreSQL** (Banco de dados principal)
- **H2 Database** (Banco em memória para testes)
- **Maven** (Gerenciamento de dependências)

## 📋 Pré-requisitos

Para rodar o projeto localmente, você vai precisar de:
- Java 21 ou superior
- Maven 3.8+
- PostgreSQL rodando localmente (ou via Docker)
- Uma chave de API válida da OpenAI

## ⚙️ Como executar

1. **Clone o repositório**
   ```bash
   git clone <url-do-repositorio>
   cd smartmenu-ai
   ```

2. **Configuração do Banco de Dados e API**
   No arquivo `src/main/resources/application-postgres.properties` (ou no seu `application.properties` principal), configure as credenciais do banco e a chave da IA:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/seu_banco
   spring.datasource.username=seu_usuario
   spring.datasource.password=sua_senha
   
   spring.ai.openai.api-key=sua-api-key-aqui
   ```

3. **Iniciando a aplicação**
   Na raiz do projeto, execute o comando do Maven:
   ```bash
   mvn spring-boot:run
   ```

Por padrão, a aplicação estará rodando na porta `8080`.

## 🧪 Testes

O projeto utiliza o H2 como banco de dados em memória para agilizar a execução dos testes. Para rodar a suíte de testes, basta executar:

```bash
mvn test
```

## 🤝 Contribuindo

Pull requests são sempre bem-vindos. Para mudanças maiores, por favor, abra uma issue primeiro para discutirmos o que você gostaria de modificar.

---
Desenvolvido com foco na sustentabilidade e eficiência gastronômica.

## Technologies
- Java 21
- Spring Boot
- Spring AI
- JUnit 5

