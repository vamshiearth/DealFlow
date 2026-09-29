# DealFlow Demo Script

## Opening (20-30 seconds)

> This project is DealFlow, an enterprise CPQ Deal Desk and approval automation platform.
>
> A CPQ system is responsible for configuring and pricing a quote, while DealFlow handles the governance around that quote: discount exceptions, approval routing, role-based access, and audit history.
>
> I built the platform using React and TypeScript on the frontend, Spring Boot and Java 21 on the backend, PostgreSQL for persistence, and Docker Compose with Nginx as the single application entry point.

Show the Dashboard and point out the deal, approval, exception, and recent activity metrics. Explain that the application is role-aware, so each user sees the functionality relevant to their responsibilities.

## 1. Role-Based Access

Log in as Maya, the Sales Rep.

Show the sidebar:

```text
Dashboard
Deals
Oracle CPQ
```

> Maya is a Sales Rep, so she can work with deals and CPQ information, but she does not get Manager, Finance, or VP approval controls.
>
> The frontend hides irrelevant functionality for usability, but authorization is enforced on the Spring Boot backend using JWT authentication and role-based access control.

```text
Frontend role filtering -> usability
Backend RBAC            -> security
```

## 2. Oracle CPQ Workspace

Open **Oracle CPQ** and search for `Q-10005`.

Show GreenBridge Logistics, the `$520,000` quote, `27%` discount, `13%` margin, configuration, attributes, pricing, quote lines, BOM, and DealFlow import status.

> The application has a CPQ integration abstraction. For this portfolio version I use a mock CPQ adapter so the project is reproducible, but the backend uses a `CpqClient` interface so a real Oracle CPQ REST adapter can be introduced without rewriting the DealFlow business workflow.
>
> DealFlow receives normalized quote information rather than having the React frontend communicate directly with CPQ.

```text
React
  |
Spring Boot
  |
CpqService
  |
CpqClient
  |
CPQ provider
```

## 3. Core Business Problem

Open Deal 38:

```text
Q-DOCKER-E2E-001
Docker E2E Customer
$520,000
27% discount
13% margin
APPROVED
```

> This is a good example of the business rules DealFlow handles.

```text
27% discount -> above the standard 20% limit -> discount exception required
13% margin   -> below 15%                    -> Finance approval required
$520,000     -> above $500,000               -> VP Sales approval required
```

The resulting workflow is:

```text
Discount Exception
        |
Sales Manager
        |
Finance
        |
VP Sales
```

## 4. Exception Enforcement

Show the approved exception on Deal 38.

> Before this deal could be submitted, the 27% discount required an approved exception because the standard policy limit is 20%.
>
> Submitting before an exception exists returns `409 Conflict`. Submitting while the exception is pending also returns `409`. Only after the Sales Manager approves the matching 27% exception does the deal become eligible for submission.

```text
No exception
-> submission blocked

Pending exception
-> submission blocked

Approved matching exception
-> submission allowed
```

## 5. Sequential Approvals

Scroll to the approval section and show:

```text
Sales Manager -> APPROVED
Finance       -> APPROVED
VP Sales      -> APPROVED
```

> Once the deal passes policy validation, the backend dynamically calculates the required approval chain.

```text
10-20% discount -> Manager
20-30%          -> Manager + Finance
>30%            -> Manager + Finance + VP
Margin <15%     -> Finance
Value >$500K    -> VP
```

> The engine deduplicates roles and preserves approval order. For Deal 38, the sequence is Manager, Finance, then VP.
>
> The workflow is sequential. Finance cannot approve before the Manager step becomes active, and VP Sales cannot bypass Finance. Approval cycles and superseded-row protection prevent stale approvals from an earlier workflow cycle being acted on after a deal is changed and resubmitted.

## 6. Audit Trail

Scroll to Audit History and show the 14 audit events on Deal 38.

> Every meaningful lifecycle action is recorded in the audit trail.

Examples include:

```text
Deal Created
Exception Created
Exception Approved
Deal Submitted
Approval Created
Manager Approved
Finance Approved
VP Approved
Final Approval
```

> Failed operations do not create false success events. For example, the blocked submit attempts did not produce a `Deal Submitted` audit record.

## 7. Show Another Role

Log out and log in as Daniel, the Sales Manager.

Show:

```text
Dashboard
Deals
Approvals
Exceptions
Oracle CPQ
```

Open **Approvals** and/or **Exceptions**.

> The Sales Manager has different workflow responsibilities. The backend returns role-specific pending queues, and only actionable workflow records are exposed. After an approval is processed, the queue refreshes and the row disappears.

## 8. Docker Architecture

Show the architecture diagram or README.

```text
Browser
   |
Nginx :8082
   |-- React SPA
   |-- /api/*
          |
      Spring Boot
          |
      PostgreSQL
```

> The browser only talks to port 8082. Nginx serves the React application and reverse-proxies `/api` requests to the backend container.
>
> PostgreSQL uses a named Docker volume. I tested persistence by restarting containers, deleting and recreating the PostgreSQL container, and running full Compose down/up cycles while verifying that approved deal records survived.

## 9. CI/CD and Security

Show the GitHub Actions page or the CI section of the README.

```text
Backend CI
-> Maven tests
-> package JAR

Frontend CI
-> npm ci
-> Vite production build

Docker CI
-> backend image
-> frontend image

Dependency Security
-> OWASP Dependency-Check
-> npm audit

Secret Detection
-> Gitleaks

Container Security
-> Trivy

Full Stack Smoke Test
-> Docker Compose startup
-> PostgreSQL readiness
-> Nginx
-> backend health
-> SPA routing
```

> During container scanning, Trivy detected vulnerabilities in the frontend Alpine/Nginx image. I upgraded the Nginx runtime image and Alpine packages, then reran the scan until the production image had zero HIGH or CRITICAL findings.
>
> The backend dependency scan also led to deploying a patched Tomcat version through CI.

## Closing (20 seconds)

> DealFlow demonstrates the full lifecycle of an enterprise workflow application: external CPQ integration, policy enforcement, exceptions, sequential approvals, RBAC, auditability, persistence, containerization, CI/CD, and security scanning.
>
> The current CPQ implementation is intentionally abstracted behind a mock adapter for reproducibility. The next integration step would be connecting the same interface to an actual Oracle CPQ REST environment.

## 60-Second Version

> DealFlow is an enterprise CPQ Deal Desk and approval automation platform built with React, Spring Boot, PostgreSQL, Docker, and GitHub Actions.
>
> A CPQ quote enters DealFlow, and the backend evaluates rules based on discount, margin, and deal value. For example, a $520,000 deal with a 27% discount and 13% margin requires a discount exception plus sequential Sales Manager, Finance, and VP Sales approvals.
>
> The application has JWT/RBAC, role-specific queues, exception enforcement, stale-approval protection, and a complete audit trail.
>
> I containerized the entire stack behind Nginx, tested PostgreSQL persistence through container recreation, and built CI pipelines for backend and frontend builds, Docker validation, dependency scanning, Gitleaks, Trivy, and full Compose smoke testing.
>
> The CPQ integration is behind an adapter interface, so the current mock implementation can later be replaced with an Oracle CPQ REST client.

## Interview Questions

### Why separate exceptions from approvals?

> An approval answers whether an authorized person accepts a deal, while an exception answers whether the deal is allowed to violate a specific policy. A deal may need both.

### Why enforce roles on the backend if the frontend hides buttons?

> Frontend role filtering is only UX. A user can manipulate browser requests, so Spring Security and method-level authorization remain the actual trust boundary.

### Why use a CPQ interface?

> It decouples DealFlow's domain logic from Oracle-specific APIs and makes the system testable with a mock provider.

### Why sequential approvals?

> Enterprise approvals often have dependencies. Finance should not authorize a deal that has not passed the required Manager stage, and VP should not bypass Finance.

### What was one problem discovered while building it?

> Container security scanning found vulnerabilities in the frontend runtime image. I upgraded the Nginx/Alpine runtime and verified that the final image had no HIGH or CRITICAL findings.

### How did you test persistence?

> I restarted each service independently, deleted and recreated the PostgreSQL container using the same named volume, and performed full Compose down/up cycles while verifying that approved deal records survived.