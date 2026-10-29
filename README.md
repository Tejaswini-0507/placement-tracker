# CareerSpace – Placement Tracker & Intelligence Platform

**A centralized platform to track campus placements, manage interview experiences, and gain data-driven career insights.**

## 📌 Overview

CareerSpace is a placement management platform designed to help students organize and track their campus recruitment journey in one place. Instead of relying on scattered spreadsheets, messages, and documents, students can manage applications, record interview experiences, participate in discussions, and analyze placement trends through a centralized system.

The platform also provides administrative capabilities to manage companies, publish announcements, and monitor placement-related activities.

The project focuses on **backend development, secure REST APIs, search, analytics, and real-time communication**.

## ✨ Key Features

### 👨‍🎓 Student Features
- **Placement Tracking:** Track companies, applications, interview rounds, and recruitment outcomes.
- **Interview Experiences:** Document and share online assessment, technical interview, and HR interview experiences.
- **Advanced Search:** Search and filter interview experiences using Elasticsearch.
- **Placement Analytics:** Analyze success rates, rejections, and company-wise placement trends.
- **Resume Management:** Organize resume versions for different job applications.
- **Discussion Platform:** Share preparation strategies and discuss recruitment experiences.
- **Notifications:** Receive placement updates, announcements, and relevant notifications.
- **Personalized Preparation:** Support career preparation with insights based on placement activity.

### 🛡️ Admin Features
- Manage company information and placement-related announcements.
- Monitor student activities and recruitment information.
- Publish notifications and important placement updates.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java | Core programming language |
| Spring Boot | Backend application and REST APIs |
| Spring Security | Authentication and authorization |
| JWT | Token-based authentication |
| Spring Data JPA / Hibernate | Database access and ORM |
| PostgreSQL | Relational database |
| Elasticsearch | Search and filtering of interview experiences |
| Redis | Caching and performance optimization |
| HTML5, CSS3, JavaScript | Frontend |
| Docker | Containerization |
| Render | Application deployment |

## 🏗️ Architecture

CareerSpace follows a layered backend architecture to separate API handling, business logic, security, and database operations.

```text
                 Student / Admin
                       |
                       v
                Frontend Interface
                       |
                       v
                  REST APIs
                       |
                       v
              Spring Security + JWT
                       |
                       v
                 Controller Layer
                       |
                       v
                  Service Layer
                       |
                       v
                Repository Layer
                       |
             +---------+----------+
             |         |          |
             v         v          v
        PostgreSQL Elasticsearch Redis
```

*The diagram represents the logical architecture; individual integrations depend on the implemented modules.*

## 🔐 Security

- Authentication and authorization using Spring Security.
- JWT-based authentication for protected API endpoints.
- Role-based access control for student and administrator operations.
- Password hashing using BCrypt.
- Protected endpoints configured through Spring Security.

## 🔎 Search and Analytics

Elasticsearch supports searching and filtering interview experiences, making it easier for students to discover relevant preparation information.

The analytics module is designed to transform placement activity into useful insights, such as:

- Company-wise interview and recruitment outcomes.
- Success and rejection rates.
- Trends across recorded placement experiences.
- Data that helps students make informed preparation decisions.

Redis can be used to cache frequently accessed information and reduce repeated database operations.

## 🚀 Getting Started

### Prerequisites

Install the following tools before running the project:

- Java 17
- Maven
- PostgreSQL
- Docker (optional, depending on your setup)
- Elasticsearch and Redis, if their integrations are enabled

### 1. Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd placement-tracker
```

### 2. Configure Environment Variables

Configure your database connection and any enabled service integrations in `application.properties` or through environment variables.

Example configuration:

```properties
server.port=8081
server.servlet.context-path=/api

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Use the actual variable names and configuration required by your application. Configure Elasticsearch, Redis, JWT secrets, and OAuth credentials separately if applicable.

**Security note:** Never commit database passwords, JWT secrets, OAuth client secrets, or other credentials to GitHub.

### 3. Start the Application

Make sure PostgreSQL and the other required services are running, then execute:

```bash
mvn clean install
mvn spring-boot:run
```

Alternatively, run the main Spring Boot application class from your IDE.

### 4. Access the Application

When running locally on port `8081`, the backend base URL is:

```text
http://localhost:8081/api
```

The frontend is maintained under:

```text
src/main/resources/static/
```

Open the frontend through the configured static hosting or application URL, depending on your deployment setup.

## 📂 Project Structure

```text
placement-tracker/
├── src/
│   └── main/
│       ├── java/
│       │   └── .../
│       │       ├── controller/
│       │       ├── service/
│       │       ├── repository/
│       │       ├── entity/
│       │       ├── dto/
│       │       ├── security/
│       │       └── config/
│       └── resources/
│           ├── application.properties
│           └── static/
│               ├── HTML files
│               ├── CSS files
│               └── JavaScript files
├── pom.xml
└── README.md
```

*The package names and file layout above illustrate the intended organization. Update them to match your actual repository.*

## 📈 Key Engineering Goals

- Build maintainable and scalable REST APIs.
- Implement secure authentication and role-based authorization.
- Improve search performance using Elasticsearch.
- Analyze placement data to provide meaningful career insights.
- Reduce repeated data retrieval through caching where appropriate.
- Support reliable notification delivery and a better student experience.

## 🔮 Future Enhancements

- AI-powered resume recommendations.
- Personalized preparation roadmaps based on target companies.
- Improved placement trend visualization.
- More comprehensive notification history and offline delivery.
- Performance testing and optimization for high concurrent usage.
- Expanded automated testing and CI/CD integration.

## 👩‍💻 Author

**Tejaswini**  
Computer Science and Engineering Student

GitHub: [Your GitHub Profile](https://github.com/)

---

## 📄 License

This project is developed for educational and portfolio purposes. Add an appropriate open-source license if you intend to distribute it for reuse.
