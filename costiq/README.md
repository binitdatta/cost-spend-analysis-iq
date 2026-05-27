# CostIQ — Enterprise Cost Spend Analysis Platform

**GlobalBite Foods Inc.** — Spring Boot 3.5 / Keycloak 26 / MySQL 8 / Bootstrap 5

---

## Architecture Overview

```
Port 8085 — Spring Boot (context: /costiq)
Port 8080 — Keycloak 26
Port 3306 — MySQL 8
```

### Database Design (DBA-owned DDL)
- JPA `ddl-auto: validate` — App has NO schema creation privileges
- 14 tables across 4 cost domains: Food, Packaging, Toys, Marketing
- Generated computed columns (`total_cost_usd`) via MySQL `GENERATED ALWAYS AS`
- Full FK integrity, indexed for reporting queries

### Security
- OAuth2 Authorization Code + **PKCE (S256)** via Keycloak 26
- Public client (`costiq-app`) — no client secret
- Public routes: `/`, `/public/**`, `/css/**`, `/js/**`
- All `/dashboard/**` routes require authenticated session
- OIDC back-channel logout to Keycloak on sign-out

---

## Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 21      |
| Maven | 3.9+  |
| MySQL | 8.x   |
| Keycloak | 26.x |

---

## Step 1 — MySQL Setup

Run as root/DBA:

```sql
-- 01_schema.sql  (creates all 14 tables + user)
source src/main/resources/db/01_schema.sql;

-- 02_seed_data.sql  (rich seed data for all tables)
source src/main/resources/db/02_seed_data.sql;
```

The schema creates `costiq_user` with SELECT/INSERT/UPDATE/DELETE only.
The app user has **no** CREATE/DROP/ALTER privileges.

---

## Step 2 — Keycloak 26 Setup

1. Start Keycloak in dev mode:
```bash
bin/kc.sh start-dev --http-port=8080
```

2. Login to admin console: `http://localhost:8080`

3. Import the realm:
   - Go to **Realm Settings → Import** (or use the dropdown "Create realm")
   - Upload `keycloak-costiq-realm.json`
   - The import creates:
     - Realm: `costiq-realm`
     - Client: `costiq-app` (public, PKCE S256)
     - 3 roles: `ROLE_ANALYST`, `ROLE_MANAGER`, `ROLE_ADMIN`
     - 3 test users (below)

### Test Users

| Username | Password | Roles |
|----------|----------|-------|
| `analyst1` | `analyst123!` | ROLE_ANALYST |
| `manager1` | `manager123!` | ROLE_MANAGER |
| `admin1`   | `admin123!`   | ROLE_ADMIN, ROLE_MANAGER, ROLE_ANALYST |

---

## Step 3 — Run the Application

```bash
mvn spring-boot:run
```

| URL | Description |
|-----|-------------|
| `http://localhost:8085/costiq/` | Public home (no auth required) |
| `http://localhost:8085/costiq/public/about` | About page (no auth) |
| `http://localhost:8085/costiq/dashboard` | Dashboard (redirects to Keycloak if not logged in) |
| `http://localhost:8085/costiq/dashboard/food-costs` | Food Cost entries CRUD |
| `http://localhost:8085/costiq/dashboard/packaging-costs` | Packaging Cost entries CRUD |
| `http://localhost:8085/costiq/dashboard/toy-costs` | Toy Allocation CRUD |
| `http://localhost:8085/costiq/dashboard/marketing-costs` | Marketing Cost CRUD |

---

## Project Structure

```
costiq/
├── pom.xml
├── keycloak-costiq-realm.json          ← Import this into Keycloak
├── src/main/
│   ├── java/com/enterprise/costiq/
│   │   ├── CostIQApplication.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java     ← PKCE OAuth2 + dual route protection
│   │   ├── entity/                     ← 14 JPA entities (all public .java files)
│   │   │   ├── Region.java
│   │   │   ├── Country.java
│   │   │   ├── FiscalPeriod.java
│   │   │   ├── CostCenter.java
│   │   │   ├── Supplier.java
│   │   │   ├── FoodCategory.java
│   │   │   ├── FoodItem.java
│   │   │   ├── FoodCostEntry.java      ← Editable cost rows
│   │   │   ├── PackagingType.java
│   │   │   ├── PackagingItem.java
│   │   │   ├── PackagingCostEntry.java ← Editable cost rows
│   │   │   ├── ToyCategory.java
│   │   │   ├── ToyItem.java
│   │   │   ├── Campaign.java
│   │   │   ├── CampaignToyAllocation.java   ← Editable cost rows
│   │   │   └── CampaignMarketingCost.java   ← Editable cost rows
│   │   ├── repository/                 ← JPA repos with JOIN FETCH queries
│   │   ├── service/
│   │   │   ├── DashboardService.java   ← Aggregates all KPIs + chart data
│   │   │   ├── FoodCostService.java
│   │   │   ├── PackagingCostService.java
│   │   │   └── CampaignCostService.java
│   │   ├── controller/
│   │   │   ├── PublicController.java   ← Unauthenticated home/about
│   │   │   ├── DashboardController.java
│   │   │   ├── FoodCostController.java
│   │   │   ├── PackagingCostController.java
│   │   │   ├── CampaignToyAllocationController.java
│   │   │   └── CampaignMarketingCostController.java
│   │   └── dto/                        ← Validated form DTOs
│   └── resources/
│       ├── application.yml
│       ├── db/
│       │   ├── 01_schema.sql           ← DBA runs this (creates schema + user)
│       │   └── 02_seed_data.sql        ← Rich seed data (5 regions, 18 countries)
│       ├── templates/
│       │   ├── fragments/layout.html   ← Shared navbar/sidebar/footer
│       │   ├── public/                 ← Public (unauthenticated) pages
│       │   └── dashboard/              ← Protected dashboard + 4 CRUD modules
│       └── static/
│           ├── css/costiq.css          ← Light blue Bootstrap 5 theme
│           └── js/costiq.js
```

---

## Dashboard Features

- **8 KPI cards** — Grand total, Food/Packaging/Campaign spend, active campaigns, suppliers
- **Stacked bar chart** — Spend by region (Food + Packaging + Toys)
- **Doughnut chart** — Spend mix across all 4 cost categories
- **Line chart** — Food & Packaging cost trend by fiscal period
- **Bar chart** — Toy giveaway cost per campaign
- **Doughnut chart** — Marketing spend by channel type (TV, Digital, Social, etc.)
- **Quick action buttons** — One-click to create any cost entry type

## CRUD Features (all 4 modules)
- Paginated list view with sortable columns
- Create new entry with validated form + live total calculator
- Edit existing row
- View detail page
- Delete with confirmation modal
- Flash success/error messages

---

## Seed Data Coverage

| Domain | Records |
|--------|---------|
| Regions | 5 (NA, EU, APAC, AUS, AF) |
| Countries | 18 across all regions |
| Fiscal Periods | 12 (FY2023–FY2025) |
| Cost Centers | 12 |
| Suppliers | 15 |
| Food Categories | 8 |
| Food Items | 20 SKUs |
| Food Cost Entries | 38 entries |
| Packaging Types | 10 |
| Packaging Items | 15 SKUs |
| Packaging Cost Entries | 23 entries |
| Toy Categories | 8 |
| Toy Items | 15 SKUs |
| Campaigns | 8 (global + regional) |
| Toy Allocations | 26 entries |
| Marketing Costs | 21 entries |

---

## Next Step — AI Enrichment ChatBot

This platform is the **simulated API backend** for a future AI-driven Excel enrichment POC.
The structured seed data mimics a real PowerBI report export that can be enriched with:
- AI-generated cost variance analysis
- Anomaly detection per supplier / region
- Suggested reallocation recommendations
- Natural language spend narratives per fiscal period

```
PowerBI Export (Excel)
        ↓
AI ChatBot (Python Flask + Claude)
        ↓
Enriched Excel with AI commentary
        ↓
Upload back / present to business
```
