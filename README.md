# DealFlow

**Enterprise CPQ Deal Desk, Exception Management, and Multi-Level Approval Automation Platform**

DealFlow is a full-stack enterprise application for governing complex Configure-Price-Quote (CPQ) deals before approval. It combines quote data, configurable policy checks, discount exceptions, sequential approvals, role-based access control, and an auditable deal history.

> CPQ builds and prices the quote. DealFlow determines how the organization safely approves it.

## Overview

Enterprise sales teams need more than a generated quote. High-value or highly discounted deals may require management, finance, or executive review. DealFlow provides the workflow layer between CPQ-generated quote data and the final business decision.

```text
Oracle CPQ (target adapter)
	|
Configuration, pricing, quote, BOM
	|
     DealFlow
	|
Policy validation | Exceptions | Approval routing | Audit history
	|
    Final approval
```

## Key Features

### Deal management

Deals move through a controlled lifecycle:

```text
DRAFT -> SUBMITTED -> MANAGER_REVIEW -> FINANCE_REVIEW -> VP_REVIEW -> APPROVED
```

The workflow also supports `REJECTED`, `CHANGES_REQUESTED`, and `CANCELLED`. Users can create and edit drafts, import CPQ quote data, submit and resubmit deals, review approval history, inspect exception requirements, and view the full audit trail.

### Dynamic approval routing

Approval requirements are calculated from deal characteristics:

| Condition | Required approval |
| --- | --- |
| Discount below 10% | None |
| Discount 10%-20% | Sales Manager |
| Discount above 20%-30% | Sales Manager + Finance |
| Discount above 30% | Sales Manager + Finance + VP Sales |
| Margin below 15% | Finance |
| Deal value above $500,000 | VP Sales |

Duplicate roles are removed while preserving the required sequence. For example, a $520,000 deal with a 27% discount and 13% margin routes to Sales Manager, Finance, and VP Sales.

### Exception management

The standard discount policy allows up to 20%. A 27% discount therefore requires an approved exception before submission:

```text
27% discount -> policy limit 20% -> exception required
	      -> pending -> manager approval -> deal may be submitted
```

Pending, rejected, incorrect, or superseded exceptions do not satisfy the policy requirement.

### Sequential approvals

Approval records are created when a valid deal is submitted. Only the active step can be acted upon; later approvers cannot bypass earlier stages. The workflow supports approve, reject, request changes, approval cycles, superseded approval protection, and stale approval protection.

### Audit trail

Important lifecycle events record the event type, actor, entity, timestamp, and workflow action. Examples include deal creation and updates, exception decisions, submission, approval creation, individual approvals, and final approval. Failed operations do not create false success events.

## Oracle CPQ Integration

DealFlow isolates CPQ access behind an adapter boundary:

```text
React -> REST API -> CpqService -> CpqClient
				  |-> MockCpqClient
				  |-> OracleCpqClient (adapter target)
```

The current project uses a reproducible mock CPQ implementation. It is **not** connected to a production Oracle CPQ instance. A real Oracle CPQ REST adapter can replace the mock without changing the DealFlow business workflow.

The CPQ workspace models quote lookup, customer information, total price, discount, margin, product configuration, pricing breakdown, quote lines, BOM data, and CPQ-to-DealFlow import. Demonstration quote: `Q-10005`, GreenBridge Logistics, $520,000, 27% discount, 13% margin.

## Role-Based Access Control

JWT authentication, Spring Security, BCrypt password hashing, and server-side role authorization protect the application.

| Role | Primary responsibilities |
| --- | --- |
| `SALES_REP` | Create/edit deals, request exceptions, submit deals, CPQ lookup |
| `SALES_MANAGER` | Manager approvals and discount exceptions |
| `FINANCE` | Finance approvals |
| `VP_SALES` | Executive approvals |
| `CPQ_ADMIN` | CPQ administration and broad workflow access |

The frontend filters navigation for usability, but backend authorization remains the security boundary.

## Architecture

```text
			 +------------------+
			 |     Browser      |
			 +--------+---------+
				  | HTTP :8082
				  v
			 +------------------+
			 | Nginx            |
			 | / -> React       |
			 | /api/* -> API    |
			 +--------+---------+
				  | backend:8081
				  v
		    +-----------------------------+
		    | Spring Boot REST API        |
		    | Auth, Deals, Approvals      |
		    | Exceptions, CPQ, Audit      |
		    +--------------+--------------+
				   | postgres:5432
				   v
		    +-----------------------------+
		    | PostgreSQL 16 + named volume |
		    +-----------------------------+
```

The containerized application uses one public origin: `http://localhost:8082`. Nginx serves the React application and proxies `/api/*` to Spring Boot.

For the detailed system architecture, see [System Architecture](docs/architecture/system-architecture.md).

## Technology Stack

- **Backend:** Java 21, Spring Boot, Spring Web, Spring Data JPA, Spring Security, JWT, Bean Validation, Maven
- **Frontend:** React, TypeScript, Vite, React Router, responsive CSS, REST integration
- **Data and infrastructure:** PostgreSQL 16, Docker, Docker Compose, Nginx, persistent Docker volume
- **Quality and security:** GitHub Actions, Maven tests, npm audit, OWASP Dependency-Check, Gitleaks, Trivy, Compose smoke tests

## Running the Application

### Prerequisites

Install Docker Desktop, Docker Compose, and Git. For non-container development also install Java 21, Node.js 22, npm, and PostgreSQL.

### Configure local secrets

```powershell
Copy-Item .env.example .env
```

Set local values in `.env`:

```env
DEALFLOW_DB_PASSWORD=your-local-database-password
DEALFLOW_JWT_SECRET=your-long-random-jwt-secret
```

The real `.env` is ignored by Git. Never commit local or production secrets.

### Start DealFlow

The Compose file uses an external named volume. Create it once:

```bash
docker volume create dealflow-postgres-data
docker compose build
docker compose up -d
docker compose ps
```

Open `http://localhost:8082`. The public health endpoint is `http://localhost:8082/api/health`; the direct backend endpoint is `http://localhost:8081/api/health`.

Stop the application with:

```bash
docker compose down
```

Avoid `docker compose down -v` unless deleting persistent database data is intentional.

### Database initialization note

The current project uses a persistent PostgreSQL volume containing the DealFlow schema and development/demo data. A completely empty volume does not yet automatically bootstrap the complete schema and demo dataset. Migration and seed automation is a separate deployment-hardening item; the existing Compose and CI smoke tests validate infrastructure startup and public health independently of populated demonstration data.

## Demonstrated End-to-End Workflow

```text
Sales Rep creates deal
	-> policy blocks submission
	-> discount exception created
	-> manager approves exception
	-> Sales Rep submits deal
	-> Sales Manager approves
	-> Finance approves
	-> VP Sales approves
	-> APPROVED
```

The validated Dockerized scenario uses quote `Q-DOCKER-E2E-001`, a $520,000 value, 27% discount, and 13% margin.

## CI/CD and Security

Seven GitHub Actions workflows run against `master`:

| Workflow | Gate |
| --- | --- |
| Backend CI | Java 21, Maven tests, Spring Boot package, JAR artifact |
| Frontend CI | Node.js 22, `npm ci`, Vite production build |
| Docker Build CI | Backend and frontend Buildx image validation |
| Dependency Security CI | OWASP Dependency-Check and `npm audit` |
| Secret Detection CI | Gitleaks repository and history scan |
| Container Security CI | Trivy backend and frontend image scan |
| Compose Smoke CI | Fresh-stack readiness, proxy, health, and SPA checks |

Production images pass the configured fixable HIGH/CRITICAL vulnerability gate.

Security controls include JWT authentication, Spring Security, server-side authorization, BCrypt hashing, approval sequencing, stale approval protection, exception enforcement, audit logging, environment-based secrets, dependency scanning, secret scanning, and container scanning.

## Project Structure

```text
DealFlow/
├── backend/                 # Spring Boot API and tests
├── frontend/                # React/TypeScript application
├── database/                # SQL schema scripts
├── docs/                    # Project documentation
├── .github/workflows/       # CI and security workflows
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

## Screenshots

Screenshots will be added for the dashboard, deals page, deal workspace, approval queue, exception queue, and Oracle CPQ workspace.

## Demo Roles

The development environment includes demo users for each workflow role. Credentials are intentionally not documented in this public README.

| Persona | Role |
| --- | --- |
| Maya Patel | Sales Rep |
| Daniel Brooks | Sales Manager |
| Sophia Chen | Finance |
| Marcus Reed | VP Sales |
| Ethan Rao | CPQ Admin |

## Testing

DealFlow has been validated through backend unit/integration tests, frontend production builds, REST authentication and authorization checks, approval and exception transitions, CPQ failure scenarios, Docker builds, PostgreSQL persistence and recreation, Nginx proxying, SPA routing, browser-driven workflow validation, dependency scanning, secret scanning, container scanning, and CI Compose smoke testing.

## Project Status

Core DealFlow development is complete, including the backend business logic, deal lifecycle, approval and exception engines, JWT/RBAC, audit trail, CPQ abstraction, React frontend, Docker Compose deployment, PostgreSQL persistence, Nginx reverse proxy, and CI/security gates.

Remaining portfolio work includes architecture and workflow diagrams, application screenshots, a demo walkthrough, interview documentation, and final presentation polish.

## Future Enhancements

- Real Oracle CPQ Cloud REST adapter
- Flyway or Liquibase migrations and automated seed data
- Database-backed approval policy configuration
- Margin exception policies
- Email or Slack notifications
- Delegation, SLAs, and escalation workflows
- Reporting and analytics
- AWS or Kubernetes deployment
- Enterprise secret management and SSO/OAuth2

## Design Principles

```text
Business policy belongs on the server.
UI role filtering improves usability; backend authorization determines access.
Approval sequencing cannot be bypassed.
Exceptions must explicitly satisfy policy.
Failed operations must not create false audit history.
External CPQ integration is isolated behind an adapter.
Application data must survive container recreation.
Security checks belong in CI, not only on developer machines.
```

## Project Purpose

DealFlow demonstrates practical experience with enterprise workflow design, CPQ concepts, Deal Desk automation, Java/Spring Boot, React/TypeScript, REST API design, PostgreSQL, security and RBAC, Docker, Nginx, CI/CD, DevSecOps, and enterprise integration patterns.
