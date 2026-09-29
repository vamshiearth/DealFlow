# DealFlow System Architecture

```mermaid
flowchart TB

    User["Enterprise User<br/>Sales Rep / Manager / Finance / VP / CPQ Admin"]

    subgraph Client["Client Layer"]
        Browser["Web Browser"]
    end

    subgraph Edge["Application Entry Point"]
        Nginx["Nginx<br/>Port 8082<br/>SPA Hosting + Reverse Proxy"]
    end

    subgraph Frontend["Frontend"]
        React["React + TypeScript + Vite<br/><br/>Dashboard<br/>Deals<br/>Approvals<br/>Exceptions<br/>Oracle CPQ Workspace"]
    end

    subgraph Backend["Spring Boot Backend"]
        Security["Spring Security<br/>JWT Authentication<br/>RBAC"]
        DealAPI["Deal Management"]
        ApprovalAPI["Approval Engine"]
        ExceptionAPI["Exception Engine"]
        AuditAPI["Audit Service"]
        CPQService["CPQ Integration Layer<br/>CpqService / CpqClient"]
    end

    subgraph CPQ["External CPQ Integration"]
        MockCPQ["MockCpqClient<br/>Current Implementation"]
        OracleCPQ["OracleCpqClient<br/>Future Oracle CPQ REST Adapter"]
    end

    subgraph Data["Data Layer"]
        PostgreSQL[("PostgreSQL 16<br/>Deals<br/>Users<br/>Approvals<br/>Exceptions<br/>Audit")]
        Volume["Docker Named Volume<br/>dealflow-postgres-data"]
    end

    subgraph Platform["Container Platform"]
        Docker["Docker Compose"]
    end

    subgraph CICD["CI/CD + DevSecOps"]
        GitHub["GitHub"]
        Actions["GitHub Actions"]
        BackendCI["Backend CI<br/>Maven Tests + JAR"]
        FrontendCI["Frontend CI<br/>npm + Vite Build"]
        DockerCI["Docker Build Validation"]
        DependencyScan["Dependency Security<br/>OWASP + npm audit"]
        SecretScan["Gitleaks<br/>Secret Detection"]
        Trivy["Trivy<br/>Container Scanning"]
        Smoke["Compose Smoke Test"]
    end

    User --> Browser
    Browser -->|"HTTP :8082"| Nginx
    Nginx -->|"/"| React
    Nginx -->|"/api/*"| Security
    React -->|"REST / JSON"| Nginx

    Security --> DealAPI
    Security --> ApprovalAPI
    Security --> ExceptionAPI
    Security --> AuditAPI
    Security --> CPQService

    DealAPI --> PostgreSQL
    ApprovalAPI --> PostgreSQL
    ExceptionAPI --> PostgreSQL
    AuditAPI --> PostgreSQL

    CPQService --> MockCPQ
    CPQService -.-> OracleCPQ
    PostgreSQL --> Volume

    Docker -. orchestrates .-> Nginx
    Docker -. orchestrates .-> Security
    Docker -. orchestrates .-> PostgreSQL

    GitHub --> Actions
    Actions --> BackendCI
    Actions --> FrontendCI
    Actions --> DockerCI
    Actions --> DependencyScan
    Actions --> SecretScan
    Actions --> Trivy
    Actions --> Smoke
```

## Reading the Diagram

The browser reaches one public entry point at Nginx on port 8082. Nginx serves the React SPA and proxies `/api/*` requests to the Spring Boot backend. Spring Security authenticates requests with JWT and applies server-side RBAC before domain services handle deals, approvals, exceptions, audit events, and CPQ operations.

The CPQ integration is intentionally isolated behind `CpqService` and `CpqClient`. `MockCpqClient` is the current reproducible implementation; `OracleCpqClient` is the future adapter boundary for a real Oracle CPQ REST integration.

Docker Compose orchestrates the application containers, while PostgreSQL persists the domain data in the external `dealflow-postgres-data` named volume. GitHub Actions validates builds, dependencies, secrets, container images, and full-stack startup.
