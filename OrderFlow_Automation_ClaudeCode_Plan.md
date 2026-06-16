# OrderFlow – Selenium Automation Build Plan (for Claude Code)

> **What this file is.** A complete, self-contained build spec. Claude Code should read it top to
> bottom and generate the whole Maven + Selenium + JUnit project described here, following the
> **course conventions** in §3 exactly (this is a graded QA course assignment — the code must *look*
> like the course's code, not like generic "best-practice" Selenium).
>
> **Source of truth for test cases:** the team's PractiTest project *OrderFlow* (exported in
> `requirements_-_orderflow_2026-06-16.xlsx` and `Tests_-_orderflow_2026-06-16.xlsx`). Every
> automated class maps to one PractiTest **Test #ID** and is named accordingly so the assignment's
> "explain / export from PractiTest the cases you implemented in code" requirement is satisfied.

---

## 1. System Under Test (SUT)

**App:** `https://nano-flow-order-direct.base44.app` (a base44 / React single-page app).
**Backing API:** `https://dummyjson.com/products/category/<category_name>?limit=10`.

**Flow (per spec + state diagram):**

1. **New order** → choose a **category** from a dropdown. Categories:
   `furniture`, `groceries`, `laptops`, `mobile-accessories`, `smartphones`, `sports-accessories`.
2. Products load from the API; each shows **name, description, image, price, stock**.
3. User adds products + quantities → each adds a line to the **order summary**; running **sum** shown.
4. **Submit** triggers three validation rules:
   - **R1 – Stock:** ordered quantity must not exceed available stock.
   - **R2 – Sum cap:** total (Σ price × qty) must not exceed **10,000**.
   - **R3 – Category mix:** **furniture** and **groceries** must not appear in the same order.
5. On failure → error popup; user can **Update** or **Cancel**, then re-Submit.
6. On success → local **stock decremented** (session only), order saved to **history**.
7. **Order history**: products, quantities, status (Approved / Cancelled), date-time, **export to CSV**.
8. **Returns** form: pick a product, enter returned units → local stock **increased** (session only).
9. **Session only** — nothing persists across refresh / runs.

**Implication for tests:** "Cold" = fresh/empty session state; "Hot" = state after data exists
(e.g. a submitted order). Tests that need a prior order must create it in the same session
(reuse the "submit good order" flow — see §7).

---

## 2. Tech stack & versions (match the course)

| Concern | Choice | Notes |
|---|---|---|
| Language / build | Java + **Maven**, Eclipse-compatible | course standard |
| Browser | Chrome via `ChromeDriver` | course standard |
| Driver mgmt | **Selenium Manager** (built into Selenium 4.16) | do **not** hard-code a `chromedriver.exe` path; keep the course's `System.setProperty(...)` line **commented out** for familiarity |
| Selenium | `org.seleniumhq.selenium:selenium-java:4.16.1` | same as course `Accessability_sel` project |
| Test runner | **JUnit 4.13** (`@Before/@Test/@After`) | course standard — *not* JUnit 5, *not* TestNG |
| API test (#161) | **REST Assured 4.4.0** + Hamcrest 2.2 | same as course `targil_rest_assured` |
| Data files | **json-simple 1.1.1** (`org.json.simple`) | exactly as taught in the data-driven lecture |
| Logging | **log4j2 2.13.3** (`log4j-api`, `log4j-core`) + `log4j2.properties` | exactly as taught |
| Usability (bonus) | `io.github.sridharbandi:java-a11y:3.0.4` | optional, see §9 |
| Waits | `WebDriverWait` + `ExpectedConditions` (explicit) | course taught implicit/explicit/fluent — **prefer explicit**, avoid bare `Thread.sleep` except where unavoidable |

Use the `pom.xml` in §11.1 verbatim.

---

## 3. Course conventions (MUST follow — these are graded)

These come from the course lecture notes and the two sample projects. Match them even where a
"cleaner" style exists.

1. **Page Object Model is mandatory.** Two source roots:
   - `src/test/java/pages/` — one class per screen, holding **`By` locators as fields** + **action
     methods**. Constructor takes the driver and stores it: `public XxxPage(WebDriver driver){ this.driver = driver; }`.
   - `src/test/java/testCases/` — one class per **PractiTest test**, named with the ID (see §8).
2. **`base_test_class`** (course name, lower-case) under `testCases/` with a static
   `initializeDriver()` that returns a configured `WebDriver`. Test classes call it from `@Before`.
3. **`BasePage`** under `pages/` holding the shared `WebDriver driver`, a `WebDriverWait`, and small
   shared helpers (`click(By)`, `type(By,text)`, `text(By)`, `waitVisible(By)`). Page classes extend it
   (mirrors the course's `base_prod_page` inheritance idea).
4. **The runner block.** Every test class keeps the course's `main(String[] args)` that runs itself via
   `JUnitCore` + `TextListener` and exits with a clear message + `System.exit(0/1)`:
   ```java
   public static void main(String[] args) {
       JUnitCore junit = new JUnitCore();
       junit.addListener(new TextListener(System.out));
       org.junit.runner.Result result = junit.run(Test154_NewOrderNavigation.class);
       if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
       else { System.out.println("Test finished successfully."); System.exit(0); }
   }
   ```
5. **Clear pass/fail indication** is explicitly required by the assignment. Two layers:
   - **JUnit assertions** decide pass/fail (so the runner block reports correctly), AND
   - **log4j2** writes a human line per test: `logger.info("[PASS] #154 New order navigation ...")`
     on success and `logger.error("[FAIL] #154 ...expected... but got...")` before a failing assert.
6. **Locators as `By` fields** in the page class, e.g. `By submitBtn = By.cssSelector("...");`. For
   PageFactory style the course also taught `@FindBy` — either is acceptable, but **be consistent**;
   default to the plain `By` style (it dominates the course examples).
7. **Dynamic / on-the-fly locators** for repeating items (product cards), built by string
   concatenation, exactly like the course's `click_add_to_cart(String YourProd)` and the
   `"#item_"+i+"_title_link > div"` pattern.
8. **Lists of elements** via `driver.findElements(...)` into `List<WebElement>` + iterate
   (`Iterator` / for-loop), as taught.
9. Comments may be mixed Hebrew/English — fine. Keep them explanatory like the course notes.
10. `driver.quit()` in `@After` — keep it active (course often commented it out; for a clean
    submission, quit the browser, but leave a commented note explaining the toggle).

---

## 4. Project structure to generate

```
OrderFlow_automation/
├── pom.xml                                  # §11.1
├── README.md                                # how to run (see §12)
├── LOCATORS.md                              # generated during locator discovery (see §6)
├── testdata/
│   ├── categories.json                      # §10
│   ├── orders.json                          # data-driven validation orders §10
│   └── search_queries.json                  # §10
├── downloads/                               # Chrome download dir for CSV export test (#174)
├── logs/
│   └── Mylogs.log                           # created at runtime by log4j2
└── src/
    ├── main/
    │   ├── java/
    │   │   └── api/
    │   │       └── DummyJsonClient.java      # REST Assured client for #161
    │   └── resources/
    │       └── log4j2.properties             # §11.2
    └── test/
        └── java/
            ├── pages/
            │   ├── BasePage.java
            │   ├── NavBar.java               # header: Home / New order / Order history / Returns / Logo / carousel
            │   ├── HomePage.java
            │   ├── NewOrderPage.java         # dropdown, grid, search, price filter, in-stock toggle, +/-/trash, summary, submit/cancel/update, error popup
            │   ├── OrderHistoryPage.java     # list, status, date, export CSV, empty state
            │   └── ReturnsPage.java          # product dropdown, qty input, submit return
            ├── flows/
            │   └── OrderFlows.java           # reusable composite flows (e.g. submitGoodOrder) — see §7
            ├── utils/
            │   ├── JsonData.java             # json-simple reader (data-driven)
            │   └── CsvUtil.java              # read/verify exported CSV for #174
            └── testCases/
                ├── base_test_class.java
                ├── Test142_CategorySelectionCold.java
                ├── Test146_CategorySelectionHot.java
                ├── Test149_IllegalQuantityOrder.java
                ├── Test150_ProductAmountOptions.java
                ├── Test152_ReturnSingleOrder.java
                ├── Test153_HomeNavigation.java
                ├── Test154_NewOrderNavigation.java
                ├── Test155_OrderHistoryNavigation.java
                ├── Test156_ReturnsNavigation.java
                ├── Test157_LogoNavigation.java
                ├── Test158_ReturnNonExistOrder.java        # derived — see §8 note
                ├── Test161_ApiProductFetch.java            # REST Assured
                ├── Test163_CancelOrder.java
                ├── Test165_OrderSumThreshold.java
                ├── Test166_AddProductsToOrder.java
                ├── Test168_OrderHistoryPresentationCold.java
                ├── Test169_NoFurnitureGroceries.java
                ├── Test170_OrderHistoryExportCold.java
                ├── Test171_SubmitGoodOrder.java
                ├── Test174_OrderHistoryExportHot.java
                ├── Test179_OrderApproval.java
                ├── Test180_DataPersistence.java
                └── Test_Usability_A11y.java                 # optional bonus, §9
```

> `pages/`, `flows/`, `utils/`, `testCases/` all under `src/test/java` so JUnit picks them up the
> same way the course projects do. `api/` (the REST Assured client) lives under `src/main/java`,
> mirroring the course `targil_rest_assured` project layout.

---

## 5. Constants / config

Create a tiny holder (either a `Config` class under `utils/` or `public static final` fields in
`base_test_class`):

```java
public static final String BASE_URL  = "https://nano-flow-order-direct.base44.app";
public static final String API_BASE  = "https://dummyjson.com";
public static final int    SUM_CAP   = 10000;          // R2
public static final int    WAIT_SECS = 15;             // explicit wait timeout
```

Categories live in `testdata/categories.json` (read via json-simple), **not** hard-coded in tests.

---

## 6. Locator strategy (Claude Code decides — then documents)

base44 renders a dynamic React DOM, so locators are **not known up front**. Claude Code should:

1. **Launch the live site** with Selenium (Selenium Manager handles the driver), navigate the flows,
   and **inspect the real DOM** to derive locators. Do this first, before finishing the page classes.
2. **Prefer stable selectors, in this order:**
   1. `data-*` / `id` if present and meaningful;
   2. accessible `role` + name, `aria-label`;
   3. **visible-text** locators for nav buttons / category items (React apps often have no stable id):
      `By.xpath("//button[normalize-space()='New Order']")` etc.;
   4. structural CSS as a last resort, built dynamically for repeating cards
      (course on-the-fly pattern: `By.cssSelector("[data-product='" + name + "'] .add-btn")` or an
      indexed xpath).
3. **Always wrap interactions in explicit waits** (`WebDriverWait` + `ExpectedConditions
   .elementToBeClickable / visibilityOfElementLocated`). React re-renders; never assume an element is
   ready. This is the course's "explicit wait" lesson — use it instead of `Thread.sleep`.
4. **Write `LOCATORS.md`** as you go: one row per element — page, logical name, chosen `By`, and a
   confidence flag. Mark anything fragile or guessed as `⚠ CONFIRM` so the user and Claude Code can
   review together (the user has agreed to discuss locators).
5. If a control can't be located confidently (e.g. the price-range slider, the CSV export button, the
   error-popup text container), leave the page method implemented against your best-guess `By`, log a
   clear `⚠` line, and list it in `LOCATORS.md` under "Needs confirmation".

> The user explicitly said *"let Claude Code decide (I can discuss with him about this issue)."* So:
> decide, implement, document, and surface the uncertain ones — don't block.

---

## 7. "Call To Test" → reusable methods / flows

PractiTest cases reuse each other via **"Call To Test"** steps. In code this becomes **method reuse**,
not duplicated steps:

| PractiTest "Call To Test" | Implement as |
|---|---|
| #154 New order navigation | `navBar.goNewOrder()` |
| #155 Order history navigation | `navBar.goOrderHistory()` |
| #142 Category selection | `newOrderPage.selectCategory(cat)` |
| #150 product amount options | `newOrderPage.setQuantity(product, n)` / `plus()/minus()/trash()` |
| #149 Illegal quantity order | `OrderFlows.buildIllegalQtyOrder(...)` |
| **#171 Submit good order** | **`OrderFlows.submitGoodOrder(...)`** — the key reusable flow |

`flows/OrderFlows.java` holds composite flows that several tests depend on. The most important:

```java
// Reusable: log in is not needed; just build + submit a valid order and return its summary.
public OrderResult submitGoodOrder(String category, String product, int qty) { ... }
```

Tests #174, #179, #180 all begin by calling `submitGoodOrder(...)` (Hot state), exactly as their
PractiTest steps "Call To Test #171" indicate.

---

## 8. Test inventory & mapping (PractiTest → automation)

Legend: **UI** = Selenium browser test · **API** = REST Assured · **type** = positive/negative.

### Req #134 – Navigation (UI)
| Test | Class | What it does | Key assertion |
|---|---|---|---|
| #153 Home navigation | `Test153_HomeNavigation` | From New order / History / Returns → click **Home** | URL/heading is Home each time |
| #154 New order navigation | `Test154_NewOrderNavigation` | From Home / History / Returns / **carousel** → New order | New-order screen shown |
| #155 Order history navigation | `Test155_OrderHistoryNavigation` | From Home / New order / Returns / carousel → History | History screen shown |
| #156 Returns navigation | `Test156_ReturnsNavigation` | From Home / New order / History / carousel → Returns | Returns screen shown |
| #157 Logo navigation | `Test157_LogoNavigation` | From any page → click **logo** | Home screen shown |

### Req #137 – Page interactions (UI)
| Test | Class | What it does | Key assertion |
|---|---|---|---|
| #150 product amount options | `Test150_ProductAmountOptions` | Add product; **+**, **−**, **trash**; sum re-tallies | qty & sum update correctly; trashed line removed |
| #175 Product text search | `Test175_ProductTextSearch` | Search term matching name/description; then a no-match term | only matching products show; no-match → none |
| #176 Price range filter | `Test176_PriceRangeFilter` | Move price slider mid-range; then below all | only cheaper products; below-all → none |
| #177 In-stock only filter | `Test177_InStockOnlyFilter` | Toggle "In stock only" | only in-stock products show |
| #178 Description viewing | `Test178_DescriptionViewing` | Toggle description on home carousel + on a product card | description shows/hides |

### Req #140 – Order process
| Test | Class | What it does | Key assertion |
|---|---|---|---|
| #142 Category selection – Cold | `Test142_CategorySelectionCold` | Open dropdown, pick category (none chosen yet) | products for that category load |
| #146 Category selection – Hot | `Test146_CategorySelectionHot` | Pick a category while another is already chosen | products switch to new category |
| #161 API product fetch | `Test161_ApiProductFetch` **(API)** | GET dummyjson per category | 200 + json has `title, description, images, price, stock` for each |
| #166 Add products to order | `Test166_AddProductsToOrder` | Add from same + different categories, change qty | summary lines + sum correct |

### Req #145 – Form Submission & Validation (UI)
| Test | Class | Type | What it does | Key assertion |
|---|---|---|---|---|
| #149 Illegal quantity order | `Test149_IllegalQuantityOrder` | negative | qty > stock → Submit | error popup shows submitted vs available qty; submit blocked |
| #165 Order sum threshold | `Test165_OrderSumThreshold` | negative | push sum > 10,000 → Submit | threshold error; submit blocked |
| #169 No furniture + groceries | `Test169_NoFurnitureGroceries` | negative | add 1 furniture + 1 groceries → Submit | error "cannot combine these categories"; blocked |
| #163 Cancel order | `Test163_CancelOrder` | — | after a validation error → **Cancel** | status → "Cancelled"; local stock reverted |
| #171 Submit good order | `Test171_SubmitGoodOrder` | positive | valid order → Submit | confirmed; inventory updated |
| #172 (umbrella, no steps) | *not automated* | — | parent grouping in PractiTest | covered by #149/#163/#165/#169/#171 |

### Req #147 – Order Approval (UI)
| Test | Class | What it does | Key assertion |
|---|---|---|---|
| #179 Order Approval | `Test179_OrderApproval` | `submitGoodOrder(...)` → open History | submitted order appears with correct info; stock reflects deduction |

### Req #148 – Order history (UI)
| Test | Class | State | What it does | Key assertion |
|---|---|---|---|---|
| #168 History presentation – cold | `Test168_OrderHistoryPresentationCold` | cold | open History with no orders | nothing presented (empty state) |
| #170 History export – cold | `Test170_OrderHistoryExportCold` | cold | open History with no orders | **no** export button present |
| #174 History export – hot | `Test174_OrderHistoryExportHot` | hot | `submitGoodOrder` → History → **Export** | CSV file downloaded to `downloads/`; `CsvUtil` confirms it contains the order |

### Req #149 – Information security (UI)
| Test | Class | What it does | Key assertion |
|---|---|---|---|
| #180 Data persistence | `Test180_DataPersistence` | `submitGoodOrder` → **refresh** | new empty session; submitted order gone |

### Req #150 – Product return (UI)
| Test | Class | What it does | Key assertion |
|---|---|---|---|
| #152 Return single order | `Test152_ReturnSingleOrder` | Returns → dropdown → pick product → qty below ordered → Submit | line disappears, no error; stock increased |
| #158 Return non-exist order | `Test158_ReturnNonExistOrder` | **derived** (no steps in export) | attempt to return a product/order that wasn't ordered | error / not allowed. **⚠ Flag to user — confirm intended behavior before finalizing the assertion.** |

### Req #165 – Error handling — **IGNORE** (marked IGNORE in PractiTest; no tests). Do not implement.

> **#158 note:** the test exists in the requirements traceability but has **no detailed steps** in the
> Tests export. Implement it from its title as a negative return test and clearly flag it (in code
> comments, `LOCATORS.md`, and the run log) as *derived / needs confirmation*.

---

## 9. Usability / accessibility (assignment bonus — optional)

The assignment encourages usability testing "דרך קוד a11y" and the course taught `java-a11y`
(`HtmlCsRunner` / `AxeRunner`). Add **`Test_Usability_A11y`** mirroring the course's
`MyFirstAccssblty`: `@Before` builds `ChromeDriver` + `HtmlCsRunner(driver)`; `@Test` navigates to the
New-order page; `@After` runs `htmlCsRunner.execute()` + `generateHtmlReport()`. Keep the java-a11y
dependency commented in `pom.xml` with a note, so the core suite builds even if the user skips it.

---

## 10. Data-driven inputs (json-simple, as taught)

Read with `org.json.simple` exactly like the data-driven lecture (`JSONParser`, `JSONArray`,
`JSONObject`, cast `(String)obj.get("...")`). Put files in `testdata/`.

**`categories.json`**
```json
["furniture","groceries","laptops","mobile-accessories","smartphones","sports-accessories"]
```

**`orders.json`** — drives the validation tests; each row is a scenario (equivalence partitioning +
boundary values on R1/R2/R3):
```json
[
  { "name":"valid_single",        "category":"laptops",   "product":"<fill at runtime>", "qty":1,    "expectPass":true,  "expectError":"" },
  { "name":"qty_over_stock",      "category":"laptops",   "product":"<fill at runtime>", "qty":99999,"expectPass":false, "expectError":"stock" },
  { "name":"sum_over_10000",      "category":"smartphones","product":"<fill at runtime>","qty":50,   "expectPass":false, "expectError":"10000" },
  { "name":"furniture_groceries", "category":"mix",       "product":"<fill at runtime>", "qty":1,    "expectPass":false, "expectError":"categories" }
]
```
> `product` and exact quantities should be resolved **at runtime** from the live product list (read
> the actual stock/price off the card), because dummyjson data changes. The JSON sets the *scenario
> intent*; the test reads real stock/price to pick numbers that cross the boundary. Document this in
> `README.md`.

**`search_queries.json`**
```json
{ "match": "phone", "noMatch": "zzqqxx_no_such_product_123" }
```

---

## 11. Verbatim files

### 11.1 `pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>orderflow_automation</groupId>
  <artifactId>orderflow_automation</artifactId>
  <version>0.0.1-SNAPSHOT</version>

  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <!-- Selenium (same version as the course project) -->
    <dependency>
      <groupId>org.seleniumhq.selenium</groupId>
      <artifactId>selenium-java</artifactId>
      <version>4.16.1</version>
    </dependency>

    <!-- JUnit 4 (course standard) -->
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.13</version>
      <scope>test</scope>
    </dependency>

    <!-- log4j2 (course logging lesson) -->
    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-api</artifactId>
      <version>2.13.3</version>
    </dependency>
    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-core</artifactId>
      <version>2.13.3</version>
    </dependency>

    <!-- json-simple (course data-driven lesson) -->
    <dependency>
      <groupId>com.googlecode.json-simple</groupId>
      <artifactId>json-simple</artifactId>
      <version>1.1.1</version>
    </dependency>

    <!-- REST Assured + Hamcrest for the API test #161 (course rest-assured project) -->
    <dependency>
      <groupId>io.rest-assured</groupId>
      <artifactId>rest-assured</artifactId>
      <version>4.4.0</version>
    </dependency>
    <dependency>
      <groupId>org.hamcrest</groupId>
      <artifactId>hamcrest</artifactId>
      <version>2.2</version>
    </dependency>

    <!-- OPTIONAL: usability / accessibility (course a11y lesson). Uncomment to enable Test_Usability_A11y. -->
    <!--
    <dependency>
      <groupId>io.github.sridharbandi</groupId>
      <artifactId>java-a11y</artifactId>
      <version>3.0.4</version>
    </dependency>
    -->
  </dependencies>
</project>
```

### 11.2 `src/main/resources/log4j2.properties`
(same shape as the course lesson; logs to console **and** `logs/Mylogs.log`, appended across runs)
```properties
name=PropertiesConfig
property.filename = logs

appenders = console,file

appender.console.type = Console
appender.console.name = STDOUT
appender.console.layout.type = PatternLayout
appender.console.layout.pattern = [%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %c{1} - %msg%n

appender.file.type = File
appender.file.name = LOGFILE
appender.file.fileName = logs/Mylogs.log
appender.file.layout.type = PatternLayout
appender.file.layout.pattern = [%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %c{1} - %msg%n
appender.file.append = true

loggers = file
logger.file.name = testCases
logger.file.level = debug
logger.file.appenderRefs = file
logger.file.appenderRef.file.ref = LOGFILE

rootLogger.level = info
rootLogger.appenderRefs = stdout
rootLogger.appenderRef.stdout.ref = STDOUT
```

### 11.3 `base_test_class.java` (template)
```java
package testCases;

import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class base_test_class {

    public static final String BASE_URL = "https://nano-flow-order-direct.base44.app";

    public static WebDriver initializeDriver() {
        // Selenium 4.16 has Selenium Manager built in - no chromedriver path needed.
        // (Old course style, kept for reference:)
        // System.setProperty("webdriver.chrome.driver", "C:\\...\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        // For the CSV export test (#174) set a known download dir:
        // java.util.HashMap<String,Object> prefs = new java.util.HashMap<>();
        // prefs.put("download.default_directory", new java.io.File("downloads").getAbsolutePath());
        // options.setExperimentalOption("prefs", prefs);
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();
        return driver;
    }
}
```

### 11.4 `BasePage.java` (template)
```java
package pages;

import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    protected WebElement waitVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }
    protected void click(By by) {
        wait.until(ExpectedConditions.elementToBeClickable(by)).click();
    }
    protected void type(By by, String text) {
        WebElement el = waitVisible(by);
        el.clear();
        el.sendKeys(text);
    }
    protected String text(By by) {
        return waitVisible(by).getText();
    }
    protected boolean isPresent(By by) {
        return !driver.findElements(by).isEmpty();
    }
}
```

### 11.5 Example page class — `NewOrderPage.java` (skeleton; fill locators in §6)
```java
package pages;

import org.openqa.selenium.*;
import java.util.List;

public class NewOrderPage extends BasePage {

    public NewOrderPage(WebDriver driver) { super(driver); }

    // ---- Locators (⚠ confirm against live DOM, see LOCATORS.md) ----
    By categoryDropdown = By.cssSelector("/* TODO */");
    By searchInput      = By.cssSelector("/* TODO */");
    By inStockToggle    = By.cssSelector("/* TODO */");
    By submitBtn        = By.xpath("//button[normalize-space()='Submit Order']");
    By cancelBtn        = By.xpath("//button[normalize-space()='Cancel']");
    By errorPopup       = By.cssSelector("/* TODO */");
    By orderSummaryRows = By.cssSelector("/* TODO each summary line */");
    By orderSumValue    = By.cssSelector("/* TODO running total */");

    // ---- Actions ----
    public void selectCategory(String category) {
        // course dropdown lesson: either Select(...) or click option by visible text
        click(categoryDropdown);
        click(By.xpath("//*[normalize-space()='" + category + "']"));
    }

    // on-the-fly locator per product (course pattern)
    public void addProduct(String productName) {
        By addBtn = By.xpath("//*[contains(@class,'product') and .//*[normalize-space()='"
                              + productName + "']]//button[contains(.,'Add')]");
        click(addBtn);
    }

    public void setQuantity(String productName, int qty) { /* + / - buttons in the summary line */ }
    public void plus(String productName)  { /* ... */ }
    public void minus(String productName) { /* ... */ }
    public void trash(String productName) { /* ... */ }

    public void submit() { click(submitBtn); }
    public void cancel() { click(cancelBtn); }

    public boolean hasError()        { return isPresent(errorPopup); }
    public String  errorText()       { return text(errorPopup); }
    public double  orderSum()        { return Double.parseDouble(text(orderSumValue).replaceAll("[^0-9.]","")); }
    public List<WebElement> summaryRows() { return driver.findElements(orderSummaryRows); }
}
```

### 11.6 Example test class — `Test154_NewOrderNavigation.java` (the canonical pattern)
```java
package testCases;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import pages.NavBar;
import pages.HomePage;

public class Test154_NewOrderNavigation {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test154_NewOrderNavigation.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
        driver.get(base_test_class.BASE_URL);
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();   // course sometimes commented this out; keep it for a clean run
    }

    @Test
    public void newOrderNavigation() {
        NavBar nav = new NavBar(driver);

        // From Home -> New order
        nav.goHome();
        nav.goNewOrder();
        boolean onNewOrder = nav.isOnNewOrder();
        if (onNewOrder) logger.info("[PASS] #154 Home -> New order");
        else            logger.error("[FAIL] #154 Home -> New order: not on new-order screen");
        assertTrue("Home -> New order failed", onNewOrder);

        // From Order history -> New order
        nav.goOrderHistory();
        nav.goNewOrder();
        assertTrue("History -> New order failed", nav.isOnNewOrder());

        // From Returns -> New order
        nav.goReturns();
        nav.goNewOrder();
        assertTrue("Returns -> New order failed", nav.isOnNewOrder());

        // From home carousel quick-nav -> New order
        nav.goHome();
        nav.carouselNewOrder();
        assertTrue("Carousel -> New order failed", nav.isOnNewOrder());

        logger.info("[PASS] #154 New order navigation - all entry points OK");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test154_NewOrderNavigation.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
```

### 11.7 Example API test — `Test161_ApiProductFetch.java` (REST Assured, course style)
```java
package testCases;

import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import io.restassured.response.Response;
import static org.junit.Assert.*;

import api.DummyJsonClient;
import utils.JsonData;
import java.util.List;

public class Test161_ApiProductFetch {

    @Test
    public void apiProductFetch() {
        List<String> categories = JsonData.readStringArray("testdata/categories.json");
        DummyJsonClient client = new DummyJsonClient();
        for (String cat : categories) {
            Response res = client.getCategory(cat);      // GET /products/category/{cat}?limit=10
            assertEquals("status for " + cat, 200, res.getStatusCode());
            // each product must expose the spec descriptors:
            assertTrue(cat + " missing title",       res.jsonPath().getList("products.title").size() > 0);
            assertNotNull(cat + " missing price",    res.jsonPath().get("products[0].price"));
            assertNotNull(cat + " missing stock",    res.jsonPath().get("products[0].stock"));
            assertNotNull(cat + " missing images",   res.jsonPath().get("products[0].images"));
            assertNotNull(cat + " missing desc",     res.jsonPath().get("products[0].description"));
            System.out.println("[PASS] #161 " + cat + " -> " +
                res.jsonPath().getList("products").size() + " products");
        }
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test161_ApiProductFetch.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
```

> Note dummyjson's `mobile-accessories` / `sports-accessories` are the real slugs; `smartphones`,
> `laptops`, `furniture`, `groceries` are valid categories. Verify each returns products and adjust the
> slug only if the live API differs.

---

## 12. README.md to generate

Include: prerequisites (JDK 17, Maven, Chrome), how to run a single test (`mvn -q -Dtest=Test154_NewOrderNavigation test` or run its `main`), how to run all, where logs land (`logs/Mylogs.log`), where the CSV export lands (`downloads/`), the data-driven note from §10, and a short table linking each `TestNNN_*` class → PractiTest **Test #NNN** (this table is what gets referenced in the STR appendix).

---

## 13. Build order for Claude Code

1. Generate `pom.xml`, `log4j2.properties`, `testdata/*.json`, `base_test_class`, `BasePage`,
   `utils/JsonData`, `utils/CsvUtil`, `api/DummyJsonClient`.
2. **Locator discovery pass:** launch the live app, walk the flows, fill page-class locators, write
   `LOCATORS.md`, flag `⚠ CONFIRM` items.
3. Implement page classes (`NavBar`, `HomePage`, `NewOrderPage`, `OrderHistoryPage`, `ReturnsPage`)
   and `flows/OrderFlows` (incl. `submitGoodOrder`).
4. Implement test classes in §8 order, each with the runner block + `[PASS]/[FAIL]` logging.
5. Run the API test (#161) first (no browser), then the navigation tests, then the rest. Fix locators
   surfaced as failures; update `LOCATORS.md`.
6. Produce a short run summary the user can paste into the STR's pass/fail column.

## 14. Acceptance checklist

- [ ] POM structure: `pages/` + `testCases/` + `base_test_class` + `BasePage` (course style).
- [ ] Every automated class maps 1:1 to a PractiTest **Test #ID** and is named with that ID.
- [ ] Each test: JUnit assertions **and** log4j `[PASS]/[FAIL]` lines **and** the `main()` runner block.
- [ ] Explicit waits everywhere; no stray `Thread.sleep` except justified spots.
- [ ] Data read from `testdata/*.json` via json-simple (no hard-coded categories).
- [ ] #161 implemented with REST Assured; #174 verifies a real downloaded CSV.
- [ ] #172 not implemented (umbrella); #165-req IGNORE not implemented; #158 implemented + flagged.
- [ ] `LOCATORS.md` lists every locator; fragile ones marked `⚠ CONFIRM`.
- [ ] `README.md` has the class → PractiTest-ID table for the STR appendix.

## 15. Open questions to raise with the user

1. **#158 Return non-exist order** — exact intended behavior (error message? button disabled?) — confirm before locking the assertion.
2. **CSV export (#174)** — is it a real file download, or an in-page render? Affects whether we need the Chrome download-dir option.
3. **Error popup** — is it a modal, a toast, or inline text? Determines the `errorText()` locator.
4. Any **known defects** already observed manually, so the automation can assert the *correct* expected behavior (and the STR can list the bug) rather than codifying the bug.
