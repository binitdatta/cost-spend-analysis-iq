package com.enterprise.costiq.service;

import com.enterprise.costiq.entity.*;
import com.enterprise.costiq.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds an augmented multi-sheet Excel workbook using Apache POI.
 *
 * The workbook fetches all cost data from the database (same queries as
 * ApiController), then augments each row with reference data columns that
 * are not available from any single entity — supplier tier, cost vs baseline,
 * campaign budget utilisation, regional breakdowns, etc.
 *
 * Sheet structure:
 *   1. Executive Summary   — KPIs and metadata
 *   2. Food Costs          — 28 cols (12 augmented)
 *   3. Packaging Costs     — 26 cols (11 augmented)
 *   4. Toy Allocations     — 26 cols (12 augmented)
 *   5. Marketing Costs     — 19 cols  (8 augmented)
 *   6. Supplier Scorecard  — cross-category spend per supplier (fully computed)
 *   7. Regional Summary    — spend by region × category matrix (fully computed)
 *   8. Campaign Summary    — budget vs actual + ratio per campaign (fully computed)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExcelExportService {

    @PersistenceContext
    private EntityManager em;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    // ── Colour palette ──────────────────────────────────────────────────────
    private static final String NAVY   = "1E3A5F";
    private static final String GREEN  = "166534";
    private static final String TEAL   = "0E7490";
    private static final String AMBER  = "92400E";
    private static final String PURPLE = "6B21A8";
    private static final String VIOLET = "7C3AED";
    private static final String WHITE  = "FFFFFF";
    private static final String SILVER = "F1F5F9";
    private static final String LGRAY  = "E2E8F0";
    private static final String DTEXT  = "1E293B";

    // ── Workbook entry point ─────────────────────────────────────────────────

    public byte[] buildWorkbook() throws IOException {

        // Load all data within this transaction
        List<FoodCostEntry>         foodEntries = fetchFoodCosts();
        List<PackagingCostEntry>    pkgEntries  = fetchPackagingCosts();
        List<CampaignToyAllocation> toyEntries  = fetchToyAllocations();
        List<CampaignMarketingCost> mktEntries  = fetchMarketingCosts();
        List<Campaign>              campaigns   = fetchCampaigns();
        List<Supplier>              suppliers   = fetchSuppliers();
        List<Country>               countries   = fetchCountries();
        List<FiscalPeriod>          periods     = fetchFiscalPeriods();

        Map<Long, Campaign> campaignMap = campaigns.stream()
                .collect(Collectors.toMap(Campaign::getId, c -> c));

        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            writeExecutiveSummary(wb, foodEntries, pkgEntries,
                    toyEntries, mktEntries,
                    campaigns, suppliers, countries, periods);

            writeFoodCosts(wb, foodEntries);
            writePackagingCosts(wb, pkgEntries);
            writeToyAllocations(wb, toyEntries, campaignMap);
            writeMarketingCosts(wb, mktEntries, campaignMap);
            writeSupplierScorecard(wb, foodEntries, pkgEntries, toyEntries, suppliers);
            writeRegionalSummary(wb, foodEntries, pkgEntries, toyEntries, mktEntries, campaignMap);
            writeCampaignSummary(wb, campaigns, toyEntries, mktEntries);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ── Data fetchers (same JOIN FETCH pattern as ApiController) ────────────

    private List<FoodCostEntry> fetchFoodCosts() {
        return em.createQuery("""
            SELECT e FROM FoodCostEntry e
            JOIN FETCH e.foodItem fi JOIN FETCH fi.category
            JOIN FETCH e.supplier s JOIN FETCH s.country sc JOIN FETCH sc.region
            JOIN FETCH e.costCenter JOIN FETCH e.fiscalPeriod
            JOIN FETCH e.country c JOIN FETCH c.region
            ORDER BY e.entryDate DESC""", FoodCostEntry.class).getResultList();
    }

    private List<PackagingCostEntry> fetchPackagingCosts() {
        return em.createQuery("""
            SELECT e FROM PackagingCostEntry e
            JOIN FETCH e.packagingItem pi JOIN FETCH pi.packagingType
            JOIN FETCH e.supplier s JOIN FETCH s.country sc JOIN FETCH sc.region
            JOIN FETCH e.costCenter JOIN FETCH e.fiscalPeriod
            JOIN FETCH e.country c JOIN FETCH c.region
            ORDER BY e.entryDate DESC""", PackagingCostEntry.class).getResultList();
    }

    private List<CampaignToyAllocation> fetchToyAllocations() {
        return em.createQuery("""
            SELECT a FROM CampaignToyAllocation a
            JOIN FETCH a.campaign
            JOIN FETCH a.toyItem ti JOIN FETCH ti.toyCategory
            JOIN FETCH a.supplier s JOIN FETCH s.country sc JOIN FETCH sc.region
            JOIN FETCH a.fiscalPeriod
            JOIN FETCH a.country c JOIN FETCH c.region
            ORDER BY a.entryDate DESC""", CampaignToyAllocation.class).getResultList();
    }

    private List<CampaignMarketingCost> fetchMarketingCosts() {
        return em.createQuery("""
            SELECT m FROM CampaignMarketingCost m
            JOIN FETCH m.campaign
            JOIN FETCH m.costCenter JOIN FETCH m.fiscalPeriod
            ORDER BY m.entryDate DESC""", CampaignMarketingCost.class).getResultList();
    }

    private List<Campaign> fetchCampaigns() {
        return em.createQuery(
                        "SELECT c FROM Campaign c ORDER BY c.startDate DESC", Campaign.class)
                .getResultList();
    }

    private List<Supplier> fetchSuppliers() {
        return em.createQuery("""
            SELECT s FROM Supplier s
            JOIN FETCH s.country c JOIN FETCH c.region
            ORDER BY s.name""", Supplier.class).getResultList();
    }

    private List<Country> fetchCountries() {
        return em.createQuery(
                "SELECT c FROM Country c JOIN FETCH c.region ORDER BY c.name",
                Country.class).getResultList();
    }

    private List<FiscalPeriod> fetchFiscalPeriods() {
        return em.createQuery(
                "SELECT fp FROM FiscalPeriod fp ORDER BY fp.fiscalYear DESC, fp.quarter DESC",
                FiscalPeriod.class).getResultList();
    }

    // ── Style helpers ────────────────────────────────────────────────────────

    private XSSFCellStyle headerStyle(XSSFWorkbook wb, String bgHex) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(hexToBytes(bgHex), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont();
        f.setColor(new XSSFColor(hexToBytes(WHITE), null));
        f.setBold(true);
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        setBorder(s);
        return s;
    }

    private XSSFCellStyle dataStyle(XSSFWorkbook wb, boolean alternate) {
        XSSFCellStyle s = wb.createCellStyle();
        String bg = alternate ? SILVER : WHITE;
        s.setFillForegroundColor(new XSSFColor(hexToBytes(bg), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont();
        f.setColor(new XSSFColor(hexToBytes(DTEXT), null));
        f.setFontHeightInPoints((short) 9);
        s.setFont(f);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s);
        return s;
    }

    private XSSFCellStyle numStyle(XSSFWorkbook wb, boolean alternate) {
        XSSFCellStyle s = dataStyle(wb, alternate);
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private XSSFCellStyle totalStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(hexToBytes(NAVY), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont();
        f.setColor(new XSSFColor(hexToBytes(WHITE), null));
        f.setBold(true);
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        setBorder(s);
        return s;
    }

    private XSSFCellStyle sectionStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(hexToBytes(LGRAY), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont();
        f.setColor(new XSSFColor(hexToBytes(NAVY), null));
        f.setBold(true);
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        setBorder(s);
        return s;
    }

    private void setBorder(XSSFCellStyle s) {
        BorderStyle thin = BorderStyle.THIN;
        short gray = IndexedColors.GREY_25_PERCENT.getIndex();
        s.setBorderTop(thin);    s.setTopBorderColor(gray);
        s.setBorderBottom(thin); s.setBottomBorderColor(gray);
        s.setBorderLeft(thin);   s.setLeftBorderColor(gray);
        s.setBorderRight(thin);  s.setRightBorderColor(gray);
    }

    private byte[] hexToBytes(String hex) {
        return new byte[]{
                (byte) Integer.parseInt(hex.substring(0, 2), 16),
                (byte) Integer.parseInt(hex.substring(2, 4), 16),
                (byte) Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    // ── Generic sheet writer ─────────────────────────────────────────────────

    private SheetWriter newSheet(XSSFWorkbook wb, String name, String headerColor,
                                 String... headers) {
        XSSFSheet sheet = wb.createSheet(name);
        sheet.createFreezePane(0, 1);

        XSSFCellStyle hStyle = headerStyle(wb, headerColor);
        Row hRow = sheet.createRow(0);
        hRow.setHeightInPoints(28);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(hStyle);
        }
        return new SheetWriter(sheet, wb, headers.length);
    }

    /** Fluent row builder returned by newSheet() */
    class SheetWriter {
        final XSSFSheet sheet;
        final XSSFWorkbook wb;
        final int colCount;
        int rowNum = 1;

        SheetWriter(XSSFSheet sheet, XSSFWorkbook wb, int colCount) {
            this.sheet    = sheet;
            this.wb       = wb;
            this.colCount = colCount;
        }

        void writeRow(Object... values) {
            boolean alt = (rowNum % 2 == 0);
            XSSFCellStyle ds = dataStyle(wb, alt);
            XSSFCellStyle ns = numStyle(wb, alt);
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(18);
            for (int i = 0; i < values.length && i < colCount; i++) {
                Cell cell = row.createCell(i);
                Object v  = values[i];
                if (v == null) {
                    cell.setCellValue("");
                    cell.setCellStyle(ds);
                } else if (v instanceof Number n) {
                    cell.setCellValue(n.doubleValue());
                    cell.setCellStyle(ns);
                } else if (v instanceof Boolean b) {
                    cell.setCellValue(b ? "Yes" : "No");
                    cell.setCellStyle(ds);
                } else if (v instanceof LocalDate ld) {
                    cell.setCellValue(ld.format(DATE_FMT));
                    cell.setCellStyle(ds);
                } else if (v instanceof LocalDateTime ldt) {
                    cell.setCellValue(ldt.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")));
                    cell.setCellStyle(ds);
                } else {
                    cell.setCellValue(v.toString());
                    cell.setCellStyle(ds);
                }
            }
        }

        void writeSectionHeader(String label) {
            XSSFCellStyle ss = sectionStyle(wb);
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(20);
            Cell c = row.createCell(0);
            c.setCellValue(label);
            c.setCellStyle(ss);
            for (int i = 1; i < colCount; i++) {
                Cell fc = row.createCell(i);
                fc.setCellValue("");
                fc.setCellStyle(ss);
            }
        }

        void writeTotalsRow(Map<Integer, Double> colTotals) {
            XSSFCellStyle ts = totalStyle(wb);
            XSSFCellStyle ds = dataStyle(wb, false);
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(20);
            for (int i = 0; i < colCount; i++) {
                Cell c = row.createCell(i);
                if (i == 0) {
                    c.setCellValue("TOTAL");
                    XSSFCellStyle ls = wb.createCellStyle();
                    ls.cloneStyleFrom(ts);
                    ls.setAlignment(HorizontalAlignment.LEFT);
                    c.setCellStyle(ls);
                } else if (colTotals.containsKey(i)) {
                    c.setCellValue(colTotals.get(i));
                    c.setCellStyle(ts);
                } else {
                    c.setCellValue("");
                    c.setCellStyle(ds);
                }
            }
        }

        void autoWidth() {
            for (int i = 0; i < colCount; i++) {
                sheet.autoSizeColumn(i);
                int w = sheet.getColumnWidth(i);
                if (w > 12000) sheet.setColumnWidth(i, 12000);
                if (w < 2000)  sheet.setColumnWidth(i, 2000);
            }
        }
    }

    // ── Utility ──────────────────────────────────────────────────────────────

    private double bd(BigDecimal v) { return v == null ? 0.0 : v.doubleValue(); }
    private double pct(double part, double total) {
        return total == 0 ? 0.0 :
                BigDecimal.valueOf(part / total * 100)
                        .setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
    private double d2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
    private String s(Object v) { return v == null ? "" : v.toString(); }

    // ── Sheet 1: Executive Summary ───────────────────────────────────────────

    private void writeExecutiveSummary(XSSFWorkbook wb,
                                       List<FoodCostEntry> food, List<PackagingCostEntry> pkg,
                                       List<CampaignToyAllocation> toy, List<CampaignMarketingCost> mkt,
                                       List<Campaign> campaigns, List<Supplier> suppliers,
                                       List<Country> countries, List<FiscalPeriod> periods) {

        SheetWriter sw = newSheet(wb, "📊 Executive Summary", NAVY,
                "Metric", "Value", "Notes");

        double foodT  = food.stream().mapToDouble(e -> bd(e.getTotalCostUsd())).sum();
        double pkgT   = pkg.stream().mapToDouble(e -> bd(e.getTotalCostUsd())).sum();
        double toyT   = toy.stream().mapToDouble(e -> bd(e.getTotalCostUsd())).sum();
        double mktT   = mkt.stream().mapToDouble(e -> bd(e.getAmountUsd())).sum();
        double grand  = foodT + pkgT + toyT + mktT;
        long activeCamp = campaigns.stream()
                .filter(c -> "ACTIVE".equals(s(c.getStatus()))).count();
        long activeSup  = suppliers.stream().filter(Supplier::isActive).count();

        sw.writeSectionHeader("━━━  SPEND OVERVIEW  ━━━");
        sw.writeRow("Grand Total Spend",        String.format("$%,.2f", grand),   "All categories");
        sw.writeRow("Food Ingredient Costs",     String.format("$%,.2f", foodT),   pct(foodT, grand) + "% — " + food.size() + " entries");
        sw.writeRow("Packaging Material Costs",  String.format("$%,.2f", pkgT),    pct(pkgT,  grand) + "% — " + pkg.size()  + " entries");
        sw.writeRow("Campaign Toy Allocations",  String.format("$%,.2f", toyT),    pct(toyT,  grand) + "% — " + toy.size()  + " allocations");
        sw.writeRow("Campaign Marketing Spend",  String.format("$%,.2f", mktT),    pct(mktT,  grand) + "% — " + mkt.size()  + " entries");
        sw.writeRow("", "", "");

        sw.writeSectionHeader("━━━  OPERATIONS  ━━━");
        sw.writeRow("Active Campaigns",    activeCamp,              "of " + campaigns.size() + " total");
        sw.writeRow("Active Suppliers",    activeSup,               "Across all categories");
        sw.writeRow("Countries Covered",   countries.size(),         "Global procurement footprint");
        sw.writeRow("Fiscal Periods",      periods.size(),           "");
        sw.writeRow("", "", "");

        sw.writeSectionHeader("━━━  REPORT METADATA  ━━━");
        sw.writeRow("Generated At",   LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), "");
        sw.writeRow("Data Source",    "CostIQ Spring Boot REST API", "Live data");
        sw.writeRow("Authentication", "Keycloak 26 PKCE",            "");
        sw.writeRow("Platform",       "GlobalBite Foods Inc. — CostIQ v1.0.0", "");

        sw.autoWidth();
    }

    // ── Sheet 2: Food Costs ──────────────────────────────────────────────────

    private void writeFoodCosts(XSSFWorkbook wb, List<FoodCostEntry> entries) {
        SheetWriter sw = newSheet(wb, "🥩 Food Costs", GREEN,
                "Entry ID", "Entry Date", "Food Item", "SKU", "Category",
                "Unit of Measure", "Allergen Free",
                "Supplier", "Supplier Code", "Supplier Tier",      // AUGMENTED
                "Supplier Category", "Supplier Country",           // AUGMENTED
                "Procurement Country", "Procurement Region",
                "Region Currency",                                 // AUGMENTED
                "Fiscal Period", "Fiscal Year", "Quarter",         // AUGMENTED
                "Cost Center", "Cost Center Name",                 // AUGMENTED
                "Quantity", "Unit Cost (USD)", "Baseline Cost (USD)",  // AUGMENTED
                "Cost vs Baseline (USD)", "Cost vs Baseline (%)",  // AUGMENTED
                "Total Cost (USD)", "% of Food Total",             // AUGMENTED
                "Invoice Ref", "PO Number", "Notes");

        double grandFood = entries.stream()
                .mapToDouble(e -> bd(e.getTotalCostUsd())).sum();

        Map<Integer, Double> totals = new HashMap<>();
        int qtyCol = 20, totalCol = 25;

        for (FoodCostEntry e : entries) {
            FoodItem     fi  = e.getFoodItem();
            FoodCategory cat = fi.getCategory();
            Supplier     sup = e.getSupplier();
            Country      sc  = sup.getCountry();
            Country      c   = e.getCountry();
            Region       rgn = c.getRegion();
            FiscalPeriod fp  = e.getFiscalPeriod();
            CostCenter   cc  = e.getCostCenter();

            double qty      = bd(e.getQuantity());
            double unitCost = bd(e.getUnitCostUsd());
            double baseline = bd(fi.getBaseCostUsd());
            double total    = bd(e.getTotalCostUsd());
            if (total == 0) total = qty * unitCost;

            totals.merge(qtyCol,   qty,   Double::sum);
            totals.merge(totalCol, total, Double::sum);

            sw.writeRow(
                    e.getId(), e.getEntryDate(),
                    fi.getName(), fi.getSku(), cat.getName(),
                    fi.getUnitOfMeasure(), fi.isAllergenFree(),
                    sup.getName(), sup.getSupplierCode(),
                    s(sup.getContractTier()),                           // AUGMENTED
                    s(sup.getCategory()),                               // AUGMENTED
                    sc.getName(),                                       // AUGMENTED
                    c.getName(), rgn.getName(),
                    rgn.getCurrency(),                                  // AUGMENTED
                    fp.getPeriodName(), fp.getFiscalYear(), fp.getQuarter(),  // AUGMENTED
                    cc.getCode(), cc.getName(),                         // AUGMENTED
                    qty, unitCost, baseline,                            // AUGMENTED
                    d2(unitCost - baseline),                            // AUGMENTED
                    pct(unitCost - baseline, baseline == 0 ? 1 : baseline),  // AUGMENTED
                    d2(total),
                    pct(total, grandFood),                             // AUGMENTED
                    s(e.getInvoiceRef()), s(e.getPoNumber()), s(e.getNotes())
            );
        }
        sw.writeTotalsRow(totals);
        sw.autoWidth();
    }

    // ── Sheet 3: Packaging Costs ─────────────────────────────────────────────

    private void writePackagingCosts(XSSFWorkbook wb, List<PackagingCostEntry> entries) {
        SheetWriter sw = newSheet(wb, "📦 Packaging Costs", TEAL,
                "Entry ID", "Entry Date", "Packaging Item", "SKU",
                "Packaging Type", "Material", "Recyclable",             // AUGMENTED
                "Supplier", "Supplier Tier", "Supplier Country",        // AUGMENTED
                "Procurement Country", "Procurement Region",
                "Region Currency",                                      // AUGMENTED
                "Fiscal Period", "Fiscal Year", "Quarter",              // AUGMENTED
                "Cost Center", "Cost Center Name",                      // AUGMENTED
                "Quantity (units)", "Min Order Qty",                    // AUGMENTED
                "Order vs Min (%)",                                     // AUGMENTED
                "Unit Cost (USD)", "Baseline Cost (USD)",               // AUGMENTED
                "Cost vs Baseline (USD)",                               // AUGMENTED
                "Total Cost (USD)", "% of Packaging Total",            // AUGMENTED
                "Invoice Ref");

        double grand = entries.stream()
                .mapToDouble(e -> bd(e.getTotalCostUsd())).sum();

        Map<Integer, Double> totals = new HashMap<>();

        for (PackagingCostEntry e : entries) {
            PackagingItem pi  = e.getPackagingItem();
            PackagingType pt  = pi.getPackagingType();
            Supplier      sup = e.getSupplier();
            Country       sc  = sup.getCountry();
            Country       c   = e.getCountry();
            Region        rgn = c.getRegion();
            FiscalPeriod  fp  = e.getFiscalPeriod();
            CostCenter    cc  = e.getCostCenter();

            double qty      = e.getQuantity() == null ? 0 : e.getQuantity();
            double unitCost = bd(e.getUnitCostUsd());
            double baseline = bd(pi.getBaseCostUsd());
            double total    = bd(e.getTotalCostUsd());
            if (total == 0) total = qty * unitCost;
            double minOrder = pi.getMinOrderQty();

            totals.merge(18, qty,   Double::sum);
            totals.merge(25, total, Double::sum);

            sw.writeRow(
                    e.getId(), e.getEntryDate(),
                    pi.getName(), pi.getSku(),
                    pt.getName(), s(pt.getMaterial()), pt.isRecyclable(),  // AUGMENTED
                    sup.getName(), s(sup.getContractTier()), sc.getName(), // AUGMENTED
                    c.getName(), rgn.getName(),
                    rgn.getCurrency(),                                     // AUGMENTED
                    fp.getPeriodName(), fp.getFiscalYear(), fp.getQuarter(), // AUGMENTED
                    cc.getCode(), cc.getName(),                            // AUGMENTED
                    qty, minOrder,                                         // AUGMENTED
                    pct(qty, minOrder == 0 ? 1 : minOrder),               // AUGMENTED
                    unitCost, baseline,                                    // AUGMENTED
                    d2(unitCost - baseline),                               // AUGMENTED
                    d2(total),
                    pct(total, grand),                                     // AUGMENTED
                    s(e.getInvoiceRef())
            );
        }
        sw.writeTotalsRow(totals);
        sw.autoWidth();
    }

    // ── Sheet 4: Toy Allocations ─────────────────────────────────────────────

    private void writeToyAllocations(XSSFWorkbook wb,
                                     List<CampaignToyAllocation> entries, Map<Long, Campaign> campMap) {

        SheetWriter sw = newSheet(wb, "🎁 Toy Allocations", AMBER,
                "Allocation ID", "Entry Date",
                "Campaign", "Campaign Code", "Campaign Status",           // AUGMENTED
                "Campaign Budget (USD)",                                   // AUGMENTED
                "Campaign Start", "Campaign End",                         // AUGMENTED
                "Toy Item", "Toy SKU",                                     // AUGMENTED
                "Toy Category", "Age Range",                              // AUGMENTED
                "Licensed IP", "Safety Certified",                        // AUGMENTED
                "Supplier", "Supplier Tier",                              // AUGMENTED
                "Distribution Channel",
                "Country", "Region",
                "Fiscal Period", "Fiscal Year",                           // AUGMENTED
                "Quantity", "Unit Cost (USD)", "Total Cost (USD)",
                "% of Campaign Budget", "% of Toy Total");               // AUGMENTED

        double grandToy = entries.stream()
                .mapToDouble(e -> bd(e.getTotalCostUsd())).sum();

        Map<Integer, Double> totals = new HashMap<>();

        for (CampaignToyAllocation e : entries) {
            Campaign  camp = e.getCampaign();
            Campaign  full = campMap.getOrDefault(camp.getId(), camp);
            ToyItem   ti   = e.getToyItem();
            ToyCategory tc = ti.getToyCategory();
            Supplier  sup  = e.getSupplier();
            Country   c    = e.getCountry();
            Region    rgn  = c.getRegion();
            FiscalPeriod fp = e.getFiscalPeriod();

            double qty      = e.getQuantity() == null ? 0 : e.getQuantity();
            double unitCost = bd(e.getUnitCostUsd());
            double total    = bd(e.getTotalCostUsd());
            if (total == 0) total = qty * unitCost;
            double budget   = bd(full.getBudgetUsd());

            totals.merge(22, qty,   Double::sum);
            totals.merge(24, total, Double::sum);

            sw.writeRow(
                    e.getId(), e.getEntryDate(),
                    camp.getName(), s(full.getCampaignCode()), s(full.getStatus()), // AUGMENTED
                    budget,                                                          // AUGMENTED
                    full.getStartDate(), full.getEndDate(),                          // AUGMENTED
                    ti.getName(), ti.getSku(),                                       // AUGMENTED
                    tc.getName(), s(tc.getAgeRange()),                               // AUGMENTED
                    s(ti.getLicensedIp()), ti.isSafetyCertified(),                  // AUGMENTED
                    sup.getName(), s(sup.getContractTier()),                         // AUGMENTED
                    s(e.getDistributionChannel()),
                    c.getName(), rgn.getName(),
                    fp.getPeriodName(), fp.getFiscalYear(),                          // AUGMENTED
                    qty, unitCost, d2(total),
                    pct(total, budget),                                              // AUGMENTED
                    pct(total, grandToy)                                             // AUGMENTED
            );
        }
        sw.writeTotalsRow(totals);
        sw.autoWidth();
    }

    // ── Sheet 5: Marketing Costs ─────────────────────────────────────────────

    private void writeMarketingCosts(XSSFWorkbook wb,
                                     List<CampaignMarketingCost> entries, Map<Long, Campaign> campMap) {

        SheetWriter sw = newSheet(wb, "📢 Marketing Costs", PURPLE,
                "Cost ID", "Entry Date",
                "Campaign", "Campaign Code", "Campaign Status",    // AUGMENTED
                "Campaign Budget (USD)", "Target Region",          // AUGMENTED
                "Cost Type", "Vendor",
                "Cost Center", "Cost Center Dept",                 // AUGMENTED
                "Fiscal Period", "Fiscal Year", "Quarter",         // AUGMENTED
                "Amount (USD)",
                "% of Campaign Budget", "% of Marketing Total",   // AUGMENTED
                "Invoice Ref", "Description");

        double grandMkt = entries.stream()
                .mapToDouble(e -> bd(e.getAmountUsd())).sum();

        Map<Integer, Double> totals = new HashMap<>();

        for (CampaignMarketingCost e : entries) {
            Campaign camp = e.getCampaign();
            Campaign full = campMap.getOrDefault(camp.getId(), camp);
            CostCenter cc = e.getCostCenter();
            FiscalPeriod fp = e.getFiscalPeriod();

            double amount = bd(e.getAmountUsd());
            double budget = bd(full.getBudgetUsd());

            totals.merge(14, amount, Double::sum);

            sw.writeRow(
                    e.getId(), e.getEntryDate(),
                    camp.getName(), s(full.getCampaignCode()), s(full.getStatus()), // AUGMENTED
                    budget, s(full.getTargetRegion()),                              // AUGMENTED
                    s(e.getCostType()), s(e.getVendorName()),
                    cc.getCode(), s(cc.getDepartment()),                            // AUGMENTED
                    fp.getPeriodName(), fp.getFiscalYear(), fp.getQuarter(),        // AUGMENTED
                    d2(amount),
                    pct(amount, budget),                                            // AUGMENTED
                    pct(amount, grandMkt),                                          // AUGMENTED
                    s(e.getInvoiceRef()), s(e.getDescription())
            );
        }
        sw.writeTotalsRow(totals);
        sw.autoWidth();
    }

    // ── Sheet 6: Supplier Scorecard (fully computed) ─────────────────────────

    private void writeSupplierScorecard(XSSFWorkbook wb,
                                        List<FoodCostEntry> food, List<PackagingCostEntry> pkg,
                                        List<CampaignToyAllocation> toy, List<Supplier> suppliers) {

        SheetWriter sw = newSheet(wb, "🏆 Supplier Scorecard", NAVY,
                "Supplier", "Supplier Code", "Tier", "Category",
                "Country", "Region",
                "Food Spend (USD)", "Packaging Spend (USD)", "Toy Spend (USD)",
                "Total Spend (USD)", "% of Grand Total", "Entry Count");

        // Aggregate per supplier
        Map<Long, double[]> agg = new LinkedHashMap<>(); // [food, pkg, toy, count]
        Map<Long, Supplier> supMap = suppliers.stream()
                .collect(Collectors.toMap(Supplier::getId, s -> s));

        food.forEach(e -> {
            long id = e.getSupplier().getId();
            agg.computeIfAbsent(id, k -> new double[4]);
            agg.get(id)[0] += bd(e.getTotalCostUsd());
            agg.get(id)[3]++;
        });
        pkg.forEach(e -> {
            long id = e.getSupplier().getId();
            agg.computeIfAbsent(id, k -> new double[4]);
            agg.get(id)[1] += bd(e.getTotalCostUsd());
            agg.get(id)[3]++;
        });
        toy.forEach(e -> {
            long id = e.getSupplier().getId();
            agg.computeIfAbsent(id, k -> new double[4]);
            agg.get(id)[2] += bd(e.getTotalCostUsd());
            agg.get(id)[3]++;
        });

        double grandTotal = agg.values().stream()
                .mapToDouble(v -> v[0] + v[1] + v[2]).sum();

        // Sort by total spend descending
        List<Map.Entry<Long, double[]>> sorted = agg.entrySet().stream()
                .sorted((a, b) -> Double.compare(
                        b.getValue()[0]+b.getValue()[1]+b.getValue()[2],
                        a.getValue()[0]+a.getValue()[1]+a.getValue()[2]))
                .collect(Collectors.toList());

        Map<Integer, Double> totals = new HashMap<>();

        for (Map.Entry<Long, double[]> entry : sorted) {
            Supplier sup = supMap.get(entry.getKey());
            if (sup == null) continue;
            double[] v    = entry.getValue();
            double   tot  = v[0] + v[1] + v[2];

            totals.merge(6,  v[0], Double::sum);
            totals.merge(7,  v[1], Double::sum);
            totals.merge(8,  v[2], Double::sum);
            totals.merge(9,  tot,  Double::sum);

            sw.writeRow(
                    sup.getName(), sup.getSupplierCode(),
                    s(sup.getContractTier()), s(sup.getCategory()),
                    sup.getCountry().getName(), sup.getCountry().getRegion().getName(),
                    d2(v[0]), d2(v[1]), d2(v[2]),
                    d2(tot), pct(tot, grandTotal), (long) v[3]
            );
        }
        sw.writeTotalsRow(totals);
        sw.autoWidth();
    }

    // ── Sheet 7: Regional Summary (fully computed) ───────────────────────────

    private void writeRegionalSummary(XSSFWorkbook wb,
                                      List<FoodCostEntry> food, List<PackagingCostEntry> pkg,
                                      List<CampaignToyAllocation> toy, List<CampaignMarketingCost> mkt,
                                      Map<Long, Campaign> campMap) {

        SheetWriter sw = newSheet(wb, "🌍 Regional Summary", GREEN,
                "Region",
                "Food Spend (USD)", "Packaging Spend (USD)",
                "Toy Allocation (USD)", "Marketing Spend (USD)",
                "Total Spend (USD)");

        Map<String, double[]> regions = new TreeMap<>(); // [food, pkg, toy, mkt]

        food.forEach(e -> {
            String r = e.getCountry().getRegion().getName();
            regions.computeIfAbsent(r, k -> new double[4]);
            regions.get(r)[0] += bd(e.getTotalCostUsd());
        });
        pkg.forEach(e -> {
            String r = e.getCountry().getRegion().getName();
            regions.computeIfAbsent(r, k -> new double[4]);
            regions.get(r)[1] += bd(e.getTotalCostUsd());
        });
        toy.forEach(e -> {
            String r = e.getCountry().getRegion().getName();
            regions.computeIfAbsent(r, k -> new double[4]);
            regions.get(r)[2] += bd(e.getTotalCostUsd());
        });
        mkt.forEach(e -> {
            Campaign full = campMap.getOrDefault(e.getCampaign().getId(), e.getCampaign());
            String r = s(full.getTargetRegion());
            if (!r.isEmpty()) {
                regions.computeIfAbsent(r, k -> new double[4]);
                regions.get(r)[3] += bd(e.getAmountUsd());
            }
        });

        Map<Integer, Double> totals = new HashMap<>();

        for (Map.Entry<String, double[]> entry : regions.entrySet()) {
            double[] v   = entry.getValue();
            double   tot = v[0] + v[1] + v[2] + v[3];
            totals.merge(1, v[0], Double::sum);
            totals.merge(2, v[1], Double::sum);
            totals.merge(3, v[2], Double::sum);
            totals.merge(4, v[3], Double::sum);
            totals.merge(5, tot,  Double::sum);
            sw.writeRow(entry.getKey(),
                    d2(v[0]), d2(v[1]), d2(v[2]), d2(v[3]), d2(tot));
        }
        sw.writeTotalsRow(totals);
        sw.autoWidth();
    }

    // ── Sheet 8: Campaign Summary (fully computed) ───────────────────────────

    private void writeCampaignSummary(XSSFWorkbook wb,
                                      List<Campaign> campaigns,
                                      List<CampaignToyAllocation> toy,
                                      List<CampaignMarketingCost> mkt) {

        SheetWriter sw = newSheet(wb, "🎯 Campaign Summary", VIOLET,
                "Campaign", "Code", "Status", "Target Region",
                "Start Date", "End Date",
                "Budget (USD)",
                "Toy Cost (USD)", "Marketing Cost (USD)", "Total Spend (USD)",
                "Budget Utilisation (%)", "Remaining Budget (USD)",
                "Toy / Marketing Ratio");

        Map<Long, Double> toyByCamp = new HashMap<>();
        Map<Long, Double> mktByCamp = new HashMap<>();
        toy.forEach(e -> toyByCamp.merge(e.getCampaign().getId(),
                bd(e.getTotalCostUsd()), Double::sum));
        mkt.forEach(e -> mktByCamp.merge(e.getCampaign().getId(),
                bd(e.getAmountUsd()), Double::sum));

        List<Campaign> sorted = campaigns.stream()
                .sorted(Comparator.comparingDouble(
                                (Campaign c) -> toyByCamp.getOrDefault(c.getId(), 0.0)
                                        + mktByCamp.getOrDefault(c.getId(), 0.0))
                        .reversed())
                .collect(Collectors.toList());

        Map<Integer, Double> totals = new HashMap<>();

        for (Campaign c : sorted) {
            double toyS  = toyByCamp.getOrDefault(c.getId(), 0.0);
            double mktS  = mktByCamp.getOrDefault(c.getId(), 0.0);
            double total = toyS + mktS;
            double budget = bd(c.getBudgetUsd());
            String ratio  = mktS > 0
                    ? String.format("%.2fx", toyS / mktS) : "N/A";

            totals.merge(6,  budget, Double::sum);
            totals.merge(7,  toyS,   Double::sum);
            totals.merge(8,  mktS,   Double::sum);
            totals.merge(9,  total,  Double::sum);

            sw.writeRow(
                    c.getName(), s(c.getCampaignCode()), s(c.getStatus()),
                    s(c.getTargetRegion()),
                    c.getStartDate(), c.getEndDate(),
                    d2(budget),
                    d2(toyS), d2(mktS), d2(total),
                    pct(total, budget),
                    d2(budget - total),
                    ratio
            );
        }
        sw.writeTotalsRow(totals);
        sw.autoWidth();
    }
}