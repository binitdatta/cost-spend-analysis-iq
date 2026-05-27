# CostIQ — Enterprise Cost Spend Analysis Platform

A full-stack enterprise POC demonstrating AI-powered cost spend analysis for **GlobalBite Foods Inc.**,
a fictional global fast-food chain. The platform tracks food ingredient procurement, packaging materials,
campaign toy allocations and marketing spend across 5 global regions.

---

## Repository Structure

```
cost-spend-analysis-iq/
├── costiq/                  Spring Boot 3.5 — main application (port 8085)
└── costiq-chatbot/          Python 3.12 Flask — AI ChatBot (port 5001)
```

---

## Architecture Overview

```
┌─────────────────────┐     PKCE Auth      ┌──────────────────────┐
│  Flask ChatBot       │◄──────────────────►│   Keycloak 26        │
│  Python 3.12         │                    │   Port 8080          │
│  Port 5001           │                    │   Realm: costiq-realm│
│                      │   Bearer JWT       └──────────────────────┘
│  - Claude claude-opus-4-5 AI│◄──────────────────►│
│  - SSE streaming     │                    ┌──────────────────────┐
│  - Excel export      │   REST API calls   │  Spring Boot 3.5     │
│  - Keycloak PKCE     │◄──────────────────►│  Port 8085           │
└─────────────────────┘                    │  /costiq context     │
                                           │                      │
                                           │  - Thymeleaf UI      │
                                           │  - 11 REST endpoints │
                                           │  - Apache POI Excel  │
                                           │  - JWT Resource Srv  │
                                           └──────────┬───────────┘
                                                      │ JPA/Hibernate
                                                      ▼
                                           ┌──────────────────────┐
                                           │  MySQL 8             │
                                           │  Port 3306           │
                                           │  DB: costiq_db       │
                                           └──────────────────────┘
```

---

## Technology Stack

### Spring Boot Application (`costiq/`)
| Component | Version |
|---|---|
| Spring Boot | 3.5.x |
| Spring Security | 6.5.x |
| Spring Data JPA | 3.5.x |
| Hibernate ORM | 6.6.x |
| Thymeleaf | 3.1.x |
| Apache POI | 5.3.0 |
| MySQL Connector/J | 8.x |
| Java | 21 |
| Maven | 3.9+ |

### Flask ChatBot (`costiq-chatbot/`)
| Component | Version |
|---|---|
| Python | 3.12 |
| Flask | 3.1.0 |
| Anthropic SDK | 0.40.0 |
| pandas | 2.2.3 |
| openpyxl | 3.1.5 |
| requests | 2.32.3 |

### Infrastructure
| Component | Version |
|---|---|
| Keycloak | 26.x |
| MySQL | 8.0.x |

---

## Prerequisites

- Java 21 (Apple Silicon: `brew install openjdk@21`)
- Maven 3.9+ (`brew install maven`)
- Python 3.12 (`brew install python@3.12`)
- MySQL 8.x (`brew install mysql`)
- Keycloak 26 (download from [keycloak.org](https://www.keycloak.org/downloads))
- Anthropic API key ([console.anthropic.com](https://console.anthropic.com))

---

## Step 1 — MySQL Database Setup

### 1.1 Start MySQL
```bash
brew services start mysql
# or
mysql.server start
```

### 1.2 Connect as root
```bash
mysql -u root -p
```

### 1.3 Run schema script
```sql
source /path/to/cost-spend-analysis-iq/costiq/src/main/resources/db/01_schema.sql;
```

This script:
- Creates database `costiq_db` with `utf8mb4` charset
- Creates user `costiq_user` with SELECT/INSERT/UPDATE/DELETE grants
- Creates all 17 tables: `regions`, `countries`, `fiscal_periods`, `cost_centers`,
  `suppliers`, `food_categories`, `food_items`, `food_cost_entries`,
  `packaging_types`, `packaging_items`, `packaging_cost_entries`,
  `toy_categories`, `toy_items`, `campaigns`, `campaign_toy_allocations`,
  `campaign_marketing_costs`, `audit_log`

### 1.4 Run type fix script (required for Hibernate validate)
```sql
source /path/to/cost-spend-analysis-iq/costiq/src/main/resources/db/03_alter_fix_types.sql;
```

### 1.5 Load seed data
```sql
source /path/to/cost-spend-analysis-iq/costiq/src/main/resources/db/02_seed_data.sql;
```

Seed data includes:
- 5 global regions, 18 countries
- 12 fiscal periods (FY2023–FY2025)
- 12 cost centers, 15 suppliers
- 20 food SKUs, 15 packaging SKUs, 15 toy SKUs
- 8 campaigns, 38 food cost entries, 23 packaging entries
- 26 toy allocations, 21 marketing cost entries
- **Grand total tracked spend: ~$74M USD**

### 1.6 Verify
```sql
USE costiq_db;
SELECT COUNT(*) FROM food_cost_entries;   -- expect 38
SELECT COUNT(*) FROM suppliers;           -- expect 15
SELECT COUNT(*) FROM campaigns;           -- expect 8
```

### 1.7 application.yml database credentials
The Spring Boot app connects as `root` by default for the POC.
Edit `costiq/src/main/resources/application.yml` to change:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/costiq_db
    username: root
    password: localroot
```

> **Note:** `ddl-auto: validate` — the app only validates schema, never modifies it.
> All DDL is DBA-owned via the SQL scripts above.

---

## Step 2 — Keycloak Setup

### 2.1 Start Keycloak
```bash
cd /path/to/keycloak-26.x
bin/kc.sh start-dev --http-port=8080
```

### 2.2 Import realm
1. Open `http://localhost:8080`
2. Sign in as `admin` / `admin`
3. Click **Create realm** → **Browse** → select:
   ```
   costiq/src/main/resources/db/keycloak-costiq-realm.json
   ```
4. Click **Create**

This creates:
- Realm: `costiq-realm`
- Client: `costiq-app` (public, PKCE S256, Standard flow)
- Valid redirect URIs: `http://localhost:8085/costiq/*` and `http://localhost:5001/auth/callback`
- Demo users (password `Demo1234!`): `analyst.demo`, `manager.demo`, `procurement.demo`, `admin.demo`

### 2.3 Add Flask chatbot redirect URIs (if not already present)
1. Go to **Clients** → **costiq-app** → **Settings**
2. Under **Valid redirect URIs** add: `http://localhost:5001/auth/callback`
3. Under **Valid post logout redirect URIs** add: `http://localhost:5001/`
4. Under **Web origins** add: `http://localhost:5001`
5. Click **Save**

---

## Step 3 — Spring Boot Application

### 3.1 Build
```bash
cd costiq
mvn clean install
```

### 3.2 Run
```bash
java -jar target/costiq-1.0.0.jar
```

### 3.3 Verify
Open `http://localhost:8085/costiq` — you should see the CostIQ home page.
Click **Sign In** — you will be redirected to Keycloak.
Log in with `admin.demo` / `Demo1234!`.

### 3.4 REST API endpoints (used by ChatBot)
All endpoints are under `/costiq/api/` and require a Keycloak Bearer token.

| Endpoint | Description |
|---|---|
| `GET /api/food-costs` | All food cost entries (JOIN FETCH) |
| `GET /api/packaging-costs` | All packaging cost entries |
| `GET /api/toy-allocations` | All campaign toy allocations |
| `GET /api/marketing-costs` | All marketing cost entries |
| `GET /api/campaigns` | All campaigns |
| `GET /api/suppliers` | All suppliers with country/region |
| `GET /api/food-items` | Food item catalogue |
| `GET /api/packaging-items` | Packaging item catalogue |
| `GET /api/countries` | Countries with regions |
| `GET /api/fiscal-periods` | Fiscal periods |
| `GET /api/cost-centers` | Cost centers |
| `GET /api/export/excel` | Augmented 8-sheet Apache POI workbook |

---

## Step 4 — Flask AI ChatBot

### 4.1 Create virtual environment
```bash
cd costiq-chatbot
python3 -m venv .venv
source .venv/bin/activate
```

### 4.2 Install dependencies
```bash
pip install -r requirements.txt
```

### 4.3 Configure environment
```bash
cp .env.template .env
```

Edit `.env` and fill in:
```env
FLASK_SECRET_KEY=any-random-string-here
FLASK_PORT=5001
FLASK_DEBUG=true

ANTHROPIC_API_KEY=sk-ant-your-real-key-here

KEYCLOAK_BASE_URL=http://localhost:8080
KEYCLOAK_REALM=costiq-realm
KEYCLOAK_CLIENT_ID=costiq-app
KEYCLOAK_REDIRECT_URI=http://localhost:5001/auth/callback

COSTIQ_API_BASE=http://localhost:8085/costiq/api
```

### 4.4 Run
```bash
python run.py
```

### 4.5 Verify
Open `http://localhost:5001` — you should see the CostIQ AI ChatBot home page.
Click **Sign In** — Keycloak login → redirects to chat interface.

---

## Startup Order

Services must start in this order:

```
1. MySQL          (always running via brew services)
2. Keycloak       (bin/kc.sh start-dev)
3. Spring Boot    (java -jar target/costiq-1.0.0.jar)
4. Flask ChatBot  (python run.py)
```

---

## Features

### Spring Boot UI (`http://localhost:8085/costiq`)
- **Dashboard** — 8 Chart.js charts: spend by region, spend mix doughnut, cost trend by fiscal period, toy cost by campaign, marketing spend by type
- **Food Costs** — full CRUD with pagination
- **Packaging Costs** — full CRUD with pagination
- **Toy Allocations** — full CRUD with pagination
- **Marketing Costs** — full CRUD with pagination
- **Security** — Keycloak 26 PKCE, dual filter chain (UI sessions + JWT Bearer for API)

### Flask AI ChatBot (`http://localhost:5001`)
- **Natural language queries** — powered by Claude claude-opus-4-5 via Anthropic API
- **SSE streaming** — responses stream token-by-token
- **Live data** — fetches from 11 Spring Boot REST endpoints on every chat turn
- **Excel export** — calls Spring Boot Apache POI endpoint, produces 8-sheet workbook
- **Anthropic audit logging** — every request/response logged with cost in USD
- **Quick questions sidebar** — 6 pre-built question shortcuts

### Excel Workbook (8 sheets)
| Sheet | Contents |
|---|---|
| 📊 Executive Summary | KPIs, grand total, active campaigns/suppliers, metadata |
| 🥩 Food Costs | 28 columns — base data + supplier tier, cost vs baseline, % of total |
| 📦 Packaging Costs | 26 columns — recyclability, order vs min qty, cost vs baseline |
| 🎁 Toy Allocations | 26 columns — campaign budget utilisation, licensed IP, age range |
| 📢 Marketing Costs | 19 columns — campaign budget remaining, cost type breakdown |
| 🏆 Supplier Scorecard | Cross-category spend per supplier — fully computed |
| 🌍 Regional Summary | Spend by region × category matrix — fully computed |
| 🎯 Campaign Summary | Budget vs actual + toy/marketing ratio — fully computed |

---

## Log Files

The Flask ChatBot writes two rotating log files under `costiq-chatbot/logs/`:

| File | Contents | Max Size |
|---|---|---|
| `costiq_chatbot.log` | All application logs | 10MB × 5 backups |
| `anthropic_audit.log` | Every Anthropic request, response, token count and USD cost | 20MB × 10 backups |

---

## Security Notes

- **Never commit `.env`** — the `.gitignore` excludes it
- `application.yml` uses `root/localroot` credentials — acceptable for local POC only
- Keycloak PKCE S256 — no client secret stored anywhere
- JWT Bearer token validation on all `/api/**` endpoints via `spring-boot-starter-oauth2-resource-server`
- Flask session stores only `username`, `display_name`, `email`, `authenticated` — tokens are server-side only

---

## Demo Users

All users have password `Demo1234!`

| Username | Role |
|---|---|
| `admin.demo` | Admin |
| `manager.demo` | Manager |
| `analyst.demo` | Analyst |
| `procurement.demo` | Procurement |

---

## Sample Chat Queries

```
What is the grand total spend across all categories?
Show me a breakdown of food costs by region
Which campaign has the highest toy allocation cost?
List all active suppliers and their countries
Compare marketing spend vs toy allocation spend
What are the top 5 food cost entries by total cost?
Generate an Excel report
```

---

## Author

**Binit Datta** — Enterprise Solutions Architect  
POC built to stress-test AI capability claims against enterprise implementation reality.
