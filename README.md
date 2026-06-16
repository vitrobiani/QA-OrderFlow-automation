# OrderFlow Automation Test Suite

Selenium WebDriver test automation for the **OrderFlow** web application, built as a course project following strict QA automation conventions.

## Prerequisites

- **JDK 17** (or later) on PATH
- **Maven 3.6+** on PATH
- **Google Chrome** (latest stable)
- ChromeDriver is handled automatically by Selenium Manager (4.16+)

## Quick Start

```bash
# Run all tests
mvn test

# Run a single test
mvn -q -Dtest=Test171_SubmitGoodOrder test

# Run navigation tests only
mvn -q -Dtest=Test153*,Test154*,Test155*,Test156*,Test157* test

# Run validation tests
mvn -q -Dtest=Test149*,Test165*,Test169* test

# Run with standalone main()
mvn exec:java -Dexec.mainClass="testCases.Test154_NewOrderNavigation"
```

## Project Structure

```
OrderFlow_Automation/
├── src/
│   ├── main/java/
│   │   └── api/
│   │       └── DummyJsonClient.java     # REST Assured wrapper for API
│   └── test/java/
│       ├── pages/                        # Page Object Model classes
│       │   ├── BasePage.java            # Base with wait helpers
│       │   ├── NavBar.java              # Navigation bar
│       │   ├── HomePage.java            # Home / landing page
│       │   ├── NewOrderPage.java        # Order form
│       │   ├── OrderHistoryPage.java    # Order history list
│       │   └── ReturnsPage.java         # Product returns
│       ├── flows/
│       │   └── OrderFlows.java          # Reusable composite flows
│       ├── utils/
│       │   ├── JsonData.java            # JSON test data loader
│       │   └── CsvUtil.java             # CSV export validator
│       └── testCases/
│           ├── base_test_class.java     # Driver setup, constants
│           ├── DomDiscovery.java        # Cold-state DOM explorer
│           ├── DomDiscovery2.java       # Hot-state DOM explorer
│           └── Test*.java               # All test classes
├── testdata/
│   ├── categories.json                   # Category slugs
│   ├── orders.json                       # Order test scenarios
│   └── search_queries.json               # Search test data
├── downloads/                            # CSV export destination
├── logs/
│   └── Mylogs.log                        # Runtime log output
├── discovery/                            # DOM discovery dumps
├── LOCATORS.md                           # Locator inventory
└── pom.xml                               # Maven configuration
```

## Test Classes → PractiTest Mapping

| Test Class | PractiTest ID | Requirement | Description |
|------------|---------------|-------------|-------------|
| `Test142_CategorySelectionCold` | #142 | #140 | Category dropdown in cold state |
| `Test146_CategorySelectionHot` | #146 | #140 | Category switch in hot state |
| `Test149_IllegalQuantityOrder` | #149 | #145 | R1: qty > stock blocked |
| `Test150_ProductAmountOptions` | #150 | #137 | +/−/trash buttons |
| `Test152_ReturnSingleOrder` | #152 | #150 | Return a product |
| `Test153_HomeNavigation` | #153 | #134 | Navigate to Home |
| `Test154_NewOrderNavigation` | #154 | #134 | Navigate to New Order |
| `Test155_OrderHistoryNavigation` | #155 | #134 | Navigate to Order History |
| `Test156_ReturnsNavigation` | #156 | #134 | Navigate to Returns |
| `Test157_LogoNavigation` | #157 | #134 | Logo click (SKIPPED) |
| `Test158_ReturnNonExistOrder` | #158 | #150 | Return non-ordered item (DERIVED) |
| `Test161_ApiProductFetch` | #161 | #140 | API product validation |
| `Test163_CancelOrder` | #163 | #145 | Cancel order flow |
| `Test165_OrderSumThreshold` | #165 | #145 | R2: sum > $10,000 blocked |
| `Test166_AddProductsToOrder` | #166 | #140 | Multi-product order |
| `Test168_OrderHistoryPresentationCold` | #168 | #148 | Empty history state |
| `Test169_NoFurnitureGroceries` | #169 | #145 | R3: category mix blocked |
| `Test170_OrderHistoryExportCold` | #170 | #148 | No export in cold state |
| `Test171_SubmitGoodOrder` | #171 | #145 | Valid order submission |
| `Test174_OrderHistoryExportHot` | #174 | #148 | CSV export |
| `Test175_ProductTextSearch` | #175 | #137 | Product search filter |
| `Test176_PriceRangeFilter` | #176 | #137 | Price slider filter |
| `Test177_InStockOnlyFilter` | #177 | #137 | In-stock toggle |
| `Test178_DescriptionViewing` | #178 | #137 | Product description toggle |
| `Test179_OrderApproval` | #179 | #147 | Order appears in history |
| `Test180_DataPersistence` | #180 | #149 | Session cleared on refresh |

## Business Rules Validated

| Rule | Description | Validated By |
|------|-------------|--------------|
| R1 | Order quantity cannot exceed available stock | Test #149 |
| R2 | Order sum cannot exceed $10,000 | Test #165 |
| R3 | Cannot combine furniture and groceries | Test #169 |

## Log Output

Logs are written to `logs/Mylogs.log` with format:
```
[INFO] 2026-06-17 12:34:56 [main] TestClass - [PASS] #NNN Test description
[ERROR] 2026-06-17 12:34:57 [main] TestClass - [FAIL] #NNN Expected X but got Y
```

## CSV Export

Test #174 downloads order history CSV to `downloads/`. The test:
1. Submits a valid order
2. Navigates to Order History
3. Clicks Export
4. Waits for CSV file
5. Validates content with `CsvUtil`

## Test Data

Test data is loaded from `testdata/` using `JsonData` utility:
- `categories.json` — array of category slugs
- `orders.json` — order test scenarios with expected outcomes
- `search_queries.json` — search terms for filter tests

## Notes

- **Test #157 (Logo navigation)** is `@Ignore`d — the logo element is not clickable in the current SUT
- **Test #158 (Return non-exist)** is a derived test — no detailed steps in PractiTest; behavior needs confirmation
- Hot-state locators (product cards, error popups) use heuristic patterns — run `DomDiscovery2` to confirm
- Session data is cleared on browser refresh (per spec)

## Running Discovery

To dump DOM elements for locator verification:

```bash
# Cold-state discovery (no interactions)
mvn -q -Dtest=DomDiscovery test

# Hot-state discovery (with category selection, order submission)
mvn -q -Dtest=DomDiscovery2 test
```

Review output in `discovery/*.elements.txt`.

---

*Generated for OrderFlow QA Automation Course Project*
