-- =============================================================
-- CostIQ Database Schema  v1.1  (Hibernate-validate compatible)
-- DBA-owned DDL — App uses validate only, NO schema privileges
-- Database: costiq_db
--
-- Type mapping rules (Hibernate 6 / MySQL 8):
--   Java long/Long   -> BIGINT          (not BIGINT UNSIGNED)
--   Java int/Integer -> INT             (not SMALLINT, TINYINT, INT UNSIGNED)
--   Java boolean     -> TINYINT(1)      (OK — Hibernate maps this correctly)
--   Java BigDecimal  -> DECIMAL(p,s)    (exact match required)
--   Generated cols   -> insertable=false, updatable=false in entity
-- =============================================================

CREATE DATABASE IF NOT EXISTS costiq_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE costiq_db;

-- ---- USER SETUP (run as root) --------------------------------
CREATE USER IF NOT EXISTS 'costiq_user'@'%' IDENTIFIED BY 'costiq_pass';
GRANT SELECT, INSERT, UPDATE, DELETE ON costiq_db.* TO 'costiq_user'@'%';
FLUSH PRIVILEGES;

-- =============================================================
-- REFERENCE / LOOKUP TABLES
-- =============================================================

CREATE TABLE regions (
                         id          BIGINT          NOT NULL AUTO_INCREMENT,
                         code        VARCHAR(10)     NOT NULL UNIQUE,
                         name        VARCHAR(100)    NOT NULL,
                         currency    VARCHAR(3)      NOT NULL DEFAULT 'USD',
                         created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE countries (
                           id          BIGINT          NOT NULL AUTO_INCREMENT,
                           region_id   BIGINT          NOT NULL,
                           code        VARCHAR(3)      NOT NULL UNIQUE,
                           name        VARCHAR(100)    NOT NULL,
                           created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (id),
                           CONSTRAINT fk_country_region FOREIGN KEY (region_id) REFERENCES regions(id)
) ENGINE=InnoDB;

CREATE TABLE fiscal_periods (
                                id           BIGINT          NOT NULL AUTO_INCREMENT,
    -- INT (not SMALLINT UNSIGNED) — maps to Java int
                                fiscal_year  INT             NOT NULL,
    -- INT (not TINYINT UNSIGNED)  — maps to Java int
                                quarter      INT             NOT NULL COMMENT '1-4',
                                period_name  VARCHAR(20)     NOT NULL,
                                start_date   DATE            NOT NULL,
                                end_date     DATE            NOT NULL,
    -- TINYINT(1) -> Java boolean — correct
                                is_closed    TINYINT(1)      NOT NULL DEFAULT 0,
                                PRIMARY KEY (id),
                                UNIQUE KEY uq_fiscal (fiscal_year, quarter)
) ENGINE=InnoDB;

CREATE TABLE cost_centers (
                              id           BIGINT          NOT NULL AUTO_INCREMENT,
                              code         VARCHAR(20)     NOT NULL UNIQUE,
                              name         VARCHAR(150)    NOT NULL,
                              department   VARCHAR(100)    NOT NULL,
                              manager_name VARCHAR(100),
                              budget_usd   DECIMAL(18,2)   NOT NULL DEFAULT 0.00,
                              created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE suppliers (
                           id              BIGINT          NOT NULL AUTO_INCREMENT,
                           supplier_code   VARCHAR(20)     NOT NULL UNIQUE,
                           name            VARCHAR(200)    NOT NULL,
                           category        ENUM('FOOD','PACKAGING','TOYS','LOGISTICS','MARKETING','OTHER') NOT NULL,
                           country_id      BIGINT          NOT NULL,
                           contact_email   VARCHAR(150),
                           contract_tier   ENUM('PREFERRED','APPROVED','PROVISIONAL') NOT NULL DEFAULT 'APPROVED',
                           is_active       TINYINT(1)      NOT NULL DEFAULT 1,
                           created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (id),
                           CONSTRAINT fk_supplier_country FOREIGN KEY (country_id) REFERENCES countries(id)
) ENGINE=InnoDB;

-- =============================================================
-- FOOD ITEMS
-- =============================================================

CREATE TABLE food_categories (
                                 id          BIGINT          NOT NULL AUTO_INCREMENT,
                                 code        VARCHAR(20)     NOT NULL UNIQUE,
                                 name        VARCHAR(100)    NOT NULL,
                                 description TEXT,
                                 PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE food_items (
                            id                BIGINT          NOT NULL AUTO_INCREMENT,
                            sku               VARCHAR(30)     NOT NULL UNIQUE,
                            name              VARCHAR(200)    NOT NULL,
                            category_id       BIGINT          NOT NULL,
                            unit_of_measure   VARCHAR(20)     NOT NULL DEFAULT 'KG',
                            base_cost_usd     DECIMAL(12,4)   NOT NULL,
                            calories_per_unit DECIMAL(8,2),
                            is_allergen_free  TINYINT(1)      NOT NULL DEFAULT 0,
                            is_active         TINYINT(1)      NOT NULL DEFAULT 1,
                            created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (id),
                            CONSTRAINT fk_food_category FOREIGN KEY (category_id) REFERENCES food_categories(id)
) ENGINE=InnoDB;

CREATE TABLE food_cost_entries (
                                   id               BIGINT          NOT NULL AUTO_INCREMENT,
                                   food_item_id     BIGINT          NOT NULL,
                                   supplier_id      BIGINT          NOT NULL,
                                   cost_center_id   BIGINT          NOT NULL,
                                   fiscal_period_id BIGINT          NOT NULL,
                                   country_id       BIGINT          NOT NULL,
                                   quantity         DECIMAL(14,3)   NOT NULL,
                                   unit_cost_usd    DECIMAL(12,4)   NOT NULL,
                                   total_cost_usd   DECIMAL(18,2)   GENERATED ALWAYS AS (quantity * unit_cost_usd) STORED,
                                   invoice_ref      VARCHAR(50),
                                   po_number        VARCHAR(50),
                                   notes            TEXT,
                                   entry_date       DATE            NOT NULL,
                                   created_by       VARCHAR(100)    NOT NULL,
                                   updated_by       VARCHAR(100),
                                   created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   PRIMARY KEY (id),
                                   CONSTRAINT fk_fce_food     FOREIGN KEY (food_item_id)     REFERENCES food_items(id),
                                   CONSTRAINT fk_fce_supplier FOREIGN KEY (supplier_id)       REFERENCES suppliers(id),
                                   CONSTRAINT fk_fce_cc       FOREIGN KEY (cost_center_id)    REFERENCES cost_centers(id),
                                   CONSTRAINT fk_fce_period   FOREIGN KEY (fiscal_period_id)  REFERENCES fiscal_periods(id),
                                   CONSTRAINT fk_fce_country  FOREIGN KEY (country_id)        REFERENCES countries(id),
                                   INDEX idx_fce_entry_date   (entry_date),
                                   INDEX idx_fce_fiscal       (fiscal_period_id),
                                   INDEX idx_fce_country      (country_id)
) ENGINE=InnoDB;

-- =============================================================
-- PACKAGING
-- =============================================================

CREATE TABLE packaging_types (
                                 id            BIGINT          NOT NULL AUTO_INCREMENT,
                                 code          VARCHAR(20)     NOT NULL UNIQUE,
                                 name          VARCHAR(100)    NOT NULL,
                                 material      ENUM('PAPER','PLASTIC','CARDBOARD','FOIL','BIODEGRADABLE','GLASS','METAL') NOT NULL,
                                 is_recyclable TINYINT(1)      NOT NULL DEFAULT 0,
                                 PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE packaging_items (
                                 id                BIGINT          NOT NULL AUTO_INCREMENT,
                                 sku               VARCHAR(30)     NOT NULL UNIQUE,
                                 name              VARCHAR(200)    NOT NULL,
                                 packaging_type_id BIGINT          NOT NULL,
                                 dimensions_cm     VARCHAR(50)     COMMENT 'LxWxH',
                                 weight_grams      DECIMAL(8,2),
                                 base_cost_usd     DECIMAL(12,4)   NOT NULL,
    -- INT (not INT UNSIGNED) — maps to Java int
                                 min_order_qty     INT             NOT NULL DEFAULT 1000,
                                 is_active         TINYINT(1)      NOT NULL DEFAULT 1,
                                 created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (id),
                                 CONSTRAINT fk_pkg_type FOREIGN KEY (packaging_type_id) REFERENCES packaging_types(id)
) ENGINE=InnoDB;

CREATE TABLE packaging_cost_entries (
                                        id                BIGINT          NOT NULL AUTO_INCREMENT,
                                        packaging_item_id BIGINT          NOT NULL,
                                        supplier_id       BIGINT          NOT NULL,
                                        cost_center_id    BIGINT          NOT NULL,
                                        fiscal_period_id  BIGINT          NOT NULL,
                                        country_id        BIGINT          NOT NULL,
    -- BIGINT (not BIGINT UNSIGNED) — maps to Java Long
                                        quantity          BIGINT          NOT NULL,
                                        unit_cost_usd     DECIMAL(12,4)   NOT NULL,
                                        total_cost_usd    DECIMAL(18,2)   GENERATED ALWAYS AS (quantity * unit_cost_usd) STORED,
                                        invoice_ref       VARCHAR(50),
                                        po_number         VARCHAR(50),
                                        notes             TEXT,
                                        entry_date        DATE            NOT NULL,
                                        created_by        VARCHAR(100)    NOT NULL,
                                        updated_by        VARCHAR(100),
                                        created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        PRIMARY KEY (id),
                                        CONSTRAINT fk_pce_pkg     FOREIGN KEY (packaging_item_id) REFERENCES packaging_items(id),
                                        CONSTRAINT fk_pce_supp    FOREIGN KEY (supplier_id)       REFERENCES suppliers(id),
                                        CONSTRAINT fk_pce_cc      FOREIGN KEY (cost_center_id)    REFERENCES cost_centers(id),
                                        CONSTRAINT fk_pce_period  FOREIGN KEY (fiscal_period_id)  REFERENCES fiscal_periods(id),
                                        CONSTRAINT fk_pce_country FOREIGN KEY (country_id)        REFERENCES countries(id),
                                        INDEX idx_pce_entry_date  (entry_date),
                                        INDEX idx_pce_fiscal      (fiscal_period_id)
) ENGINE=InnoDB;

-- =============================================================
-- PROMOTIONAL CAMPAIGNS
-- =============================================================

CREATE TABLE toy_categories (
                                id        BIGINT          NOT NULL AUTO_INCREMENT,
                                code      VARCHAR(20)     NOT NULL UNIQUE,
                                name      VARCHAR(100)    NOT NULL,
                                age_range VARCHAR(20)     COMMENT 'e.g. 3-8',
                                PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE toy_items (
                           id               BIGINT          NOT NULL AUTO_INCREMENT,
                           sku              VARCHAR(30)     NOT NULL UNIQUE,
                           name             VARCHAR(200)    NOT NULL,
                           toy_category_id  BIGINT          NOT NULL,
                           licensed_ip      VARCHAR(100)    COMMENT 'e.g. Marvel, Disney',
                           material         VARCHAR(50),
                           safety_certified TINYINT(1)      NOT NULL DEFAULT 0,
                           unit_cost_usd    DECIMAL(12,4)   NOT NULL,
                           is_active        TINYINT(1)      NOT NULL DEFAULT 1,
                           created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (id),
                           CONSTRAINT fk_toy_cat FOREIGN KEY (toy_category_id) REFERENCES toy_categories(id)
) ENGINE=InnoDB;

CREATE TABLE campaigns (
                           id            BIGINT          NOT NULL AUTO_INCREMENT,
                           campaign_code VARCHAR(30)     NOT NULL UNIQUE,
                           name          VARCHAR(200)    NOT NULL,
                           description   TEXT,
                           campaign_type ENUM('GLOBAL','REGIONAL','LOCAL') NOT NULL DEFAULT 'REGIONAL',
                           status        ENUM('PLANNED','ACTIVE','PAUSED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PLANNED',
                           start_date    DATE            NOT NULL,
                           end_date      DATE            NOT NULL,
                           target_region VARCHAR(10)     COMMENT 'Region code or ALL',
                           budget_usd    DECIMAL(18,2)   NOT NULL DEFAULT 0.00,
                           created_by    VARCHAR(100)    NOT NULL,
                           created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE campaign_toy_allocations (
                                          id                   BIGINT          NOT NULL AUTO_INCREMENT,
                                          campaign_id          BIGINT          NOT NULL,
                                          toy_item_id          BIGINT          NOT NULL,
                                          country_id           BIGINT          NOT NULL,
                                          supplier_id          BIGINT          NOT NULL,
                                          fiscal_period_id     BIGINT          NOT NULL,
    -- BIGINT (not BIGINT UNSIGNED) — maps to Java Long
                                          quantity             BIGINT          NOT NULL,
                                          unit_cost_usd        DECIMAL(12,4)   NOT NULL,
                                          total_cost_usd       DECIMAL(18,2)   GENERATED ALWAYS AS (quantity * unit_cost_usd) STORED,
                                          distribution_channel ENUM('RETAIL','DIRECT','ONLINE','POPUP') NOT NULL DEFAULT 'RETAIL',
                                          invoice_ref          VARCHAR(50),
                                          po_number            VARCHAR(50),
                                          notes                TEXT,
                                          entry_date           DATE            NOT NULL,
                                          created_by           VARCHAR(100)    NOT NULL,
                                          updated_by           VARCHAR(100),
                                          created_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          updated_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          PRIMARY KEY (id),
                                          CONSTRAINT fk_cta_campaign FOREIGN KEY (campaign_id)      REFERENCES campaigns(id),
                                          CONSTRAINT fk_cta_toy      FOREIGN KEY (toy_item_id)      REFERENCES toy_items(id),
                                          CONSTRAINT fk_cta_country  FOREIGN KEY (country_id)       REFERENCES countries(id),
                                          CONSTRAINT fk_cta_supplier FOREIGN KEY (supplier_id)      REFERENCES suppliers(id),
                                          CONSTRAINT fk_cta_period   FOREIGN KEY (fiscal_period_id) REFERENCES fiscal_periods(id),
                                          INDEX idx_cta_campaign     (campaign_id),
                                          INDEX idx_cta_country      (country_id),
                                          INDEX idx_cta_period       (fiscal_period_id)
) ENGINE=InnoDB;

CREATE TABLE campaign_marketing_costs (
                                          id               BIGINT          NOT NULL AUTO_INCREMENT,
                                          campaign_id      BIGINT          NOT NULL,
                                          cost_center_id   BIGINT          NOT NULL,
                                          fiscal_period_id BIGINT          NOT NULL,
                                          cost_type        ENUM('ADVERTISING','SOCIAL_MEDIA','TV','PRINT','DIGITAL','EVENTS','AGENCY') NOT NULL,
                                          amount_usd       DECIMAL(18,2)   NOT NULL,
                                          vendor_name      VARCHAR(200),
                                          invoice_ref      VARCHAR(50),
                                          description      TEXT,
                                          entry_date       DATE            NOT NULL,
                                          created_by       VARCHAR(100)    NOT NULL,
                                          updated_by       VARCHAR(100),
                                          created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          PRIMARY KEY (id),
                                          CONSTRAINT fk_cmc_campaign FOREIGN KEY (campaign_id)      REFERENCES campaigns(id),
                                          CONSTRAINT fk_cmc_cc       FOREIGN KEY (cost_center_id)   REFERENCES cost_centers(id),
                                          CONSTRAINT fk_cmc_period   FOREIGN KEY (fiscal_period_id) REFERENCES fiscal_periods(id)
) ENGINE=InnoDB;

-- =============================================================
-- AUDIT LOG
-- =============================================================

CREATE TABLE audit_log (
                           id          BIGINT          NOT NULL AUTO_INCREMENT,
                           entity_type VARCHAR(50)     NOT NULL,
                           entity_id   BIGINT          NOT NULL,
                           action      ENUM('CREATE','UPDATE','DELETE') NOT NULL,
                           changed_by  VARCHAR(100)    NOT NULL,
                           changed_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           old_values  JSON,
                           new_values  JSON,
                           PRIMARY KEY (id),
                           INDEX idx_audit_entity (entity_type, entity_id),
                           INDEX idx_audit_date   (changed_at)
) ENGINE=InnoDB;