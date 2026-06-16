# OrderFlow — Locator Inventory

> Living document. Every locator the page classes use is logged here with a confidence flag.
>
> | Symbol | Meaning |
> |---|---|
> | ✅ CONFIRMED | Locator observed in the live DOM (or SSR shell) and verified by a real-DOM dump. |
> | 🟡 LIKELY | Strong guess from SSR/spec. Not yet exercised by a passing test. |
> | ⚠ CONFIRM | Best-effort guess; DOM hasn't been seen in the relevant state. Replace before any test trusts it. |
>
> **Status**: COMPLETE (Session 4). All 24 test classes implemented. Navigation (#153-#157), page interactions (#142, #146, #150, #175-#178), validation (#149, #163, #165, #166, #169, #171), history (#168, #170, #174, #179), security (#180), returns (#152, #158), and API (#161). Hot-state locators use heuristic patterns — run DomDiscovery2 to confirm and adjust if needed.

---

## Big picture from session-1 discovery

base44 ships this app with **lots of `data-testid` attributes and stable `id`s**. Locators
are therefore far more durable than the plan assumed. Specifically:

- Every nav link has a stable `id`: `#nav-home`, `#nav-new-order`, `#nav-order-history`, `#nav-returns`.
- The header is consistent across all four routes (`<header class="sticky top-0 ...">` + `<nav id="main-navigation">`).
- Home page has structured testids on its sections, stats, and featured-product cards (incl. description-toggle buttons for #178).
- Category control on `/order` is a **native `<select id="category-select">`** whose `option value` attrs match the API slugs verbatim — so `Selenium Select.selectByValue("furniture")` works directly with `testdata/categories.json`.

---

## NavBar (`pages/NavBar.java`)

| Element | `By` | Confidence | Source |
|---|---|---|---|
| Home link | `By.id("nav-home")` | ✅ | `<a id="nav-home" href="/">` on every route |
| New order link | `By.id("nav-new-order")` | ✅ | `<a id="nav-new-order" href="/order">` |
| Order history link | `By.id("nav-order-history")` | ✅ | `<a id="nav-order-history" href="/history">` |
| Returns link | `By.id("nav-returns")` | ✅ | `<a id="nav-returns" href="/returns">` |
| Quick-nav card → New order | `[data-testid='nav-card-new-order']` | ✅ | Home page only — the "carousel quick-nav" the spec mentions |
| Quick-nav card → History | `[data-testid='nav-card-order-history']` | ✅ | Home page only |
| Quick-nav card → Returns | `[data-testid='nav-card-returns']` | ✅ | Home page only |
| Logo / brand "OrderFlow" | `//header//span[normalize-space()='OrderFlow']` | ⚠ **not clickable in HTML** | See "Open question — Logo" below |

URL-fragment screen checks:

- `isOnHome()` — URL ends with `/` or `.app`
- `isOnNewOrder()` — URL contains `/order`
- `isOnHistory()` — URL contains `/history`
- `isOnReturns()` — URL contains `/returns`

---

## HomePage (`pages/HomePage.java`)

| Element | `By` | Confidence | Source |
|---|---|---|---|
| Welcome section | `[data-testid='welcome-section']` | ✅ | |
| Navigation section | `[data-testid='navigation-section']` | ✅ | |
| Statistics section | `[data-testid='statistics-section']` | ✅ | |
| Featured-products section | `[data-testid='featured-products-section']` | ✅ | |
| About section | `[data-testid='about-section']` | ✅ | |
| Stat: total products | `[data-testid='stat-total-products']` | ✅ | text "100+ Total Products" |
| Stat: categories | `[data-testid='stat-categories']` | ✅ | text "6 Available Categories" |
| Stat: orders processed | `[data-testid='stat-orders-processed']` | ✅ | "0 Orders Processed" (cold) |
| Stat: return requests | `[data-testid='stat-return-requests']` | ✅ | "0 Return Requests" (cold) |
| Featured product card N | `[data-testid='featured-product-{N}']` | ✅ | N = 1..4 |
| Featured description toggle N | `[data-testid='featured-desc-btn-{N}']` | ✅ | drives #178 |

---

## NewOrderPage (`pages/NewOrderPage.java`)

### Confirmed from cold state

| Element | `By` | Confidence | Source |
|---|---|---|---|
| Page heading | `//main//h1[normalize-space()='New Order']` | ✅ | |
| Category `<select>` | `By.id("category-select")` | ✅ | Native HTML select. `<option value="furniture">Furniture</option>` etc. Values match API slugs. |
| Pre-selection empty state | `By.id("products-empty")` | ✅ | "Select a category to browse products." |
| Empty order summary text | `//main//*[contains(.,'No products selected yet')]` | ✅ | |

### Still pending hot-state discovery (⚠)

| Element | Current best-effort `By` | Why pending |
|---|---|---|
| Product card | (heuristic class-contains) | Cards only render after category selection — need a second discovery pass |
| Product "Add" button | dynamic by product name | Need real testid pattern (likely `[data-testid^='add-btn-']`) |
| Search input | `//input[@type='search' or @type='text'][placeholder/aria-label contains "earch"]` | Need to confirm whether search exists in the DOM only after category selection |
| In-stock toggle | `//*[role='switch'][...]` | Same |
| Price slider | `//*[@role='slider'...]` | Same. **Spec §15: drag, click, or numeric input?** |
| Summary row | `[data-testid^='summary-row-']` | Pattern is a guess |
| Running total | text-based heuristic | Need exact element |
| Submit Order button | text-based | Could be a testid we just haven't seen yet |
| Cancel / Update buttons | text-based | Only present when popup is open |
| Validation error popup | role/class heuristic | **Spec §15 Q1: modal vs toast vs inline?** Need to actually trigger one |

---

## OrderHistoryPage (`pages/OrderHistoryPage.java`)

### Confirmed from cold state

| Element | `By` | Confidence | Source |
|---|---|---|---|
| Page heading | `//main//h1[normalize-space()='Order History']` | ✅ | |
| Subtitle (orders-this-session counter) | `//main//p[contains(.,'orders this session')]` | ✅ | "0 orders this session" (cold) |
| Empty state | `//main//p[contains(.,'No orders yet')]` | ✅ | "No orders yet. Create your first order!" |
| No Export button visible | — | ✅ | Matches spec #170 (cold → export must not be present) |

### Pending hot-state discovery (⚠)

| Element | Current best-effort | Why pending |
|---|---|---|
| Order row | `[data-testid^='order-row-']` | Pattern is a guess |
| Export button | text-based (`'export'` or `'csv'`) | **Spec §15 Q2: real file download or in-page render?** |

---

## ReturnsPage (`pages/ReturnsPage.java`)

### Confirmed from cold state

| Element | `By` | Confidence | Source |
|---|---|---|---|
| Page heading | `//main//h1[normalize-space()='Product Returns']` | ✅ | Note the heading is "Product Returns", **not** "Returns" |
| Cold-state body | `//main//p[contains(.,'No confirmed orders to return')]` | ✅ | Form doesn't render at all in cold state |

### Pending hot-state discovery (⚠)

| Element | Current best-effort | Why pending |
|---|---|---|
| Product dropdown | `//main//select` | Form doesn't render until at least one order exists |
| Qty input | `//main//input[@type='number']...` | Same |
| Submit Return button | text-based | Same |
| Error / success messages | role/class heuristic | Same. **Spec §15 Q3: #158 expected behavior?** |

---

## Decision — Test #157 (Logo navigation): SKIPPED

The header brand "OrderFlow" is a `<div>` containing a coloured square + a cart `<svg>` + a `<span>OrderFlow</span>`. **There is no anchor wrapper and no click handler in the static HTML**.

**Decision (2026-06-17, user):** skip Test #157 with a documented reason in code and in the STR. The SUT does not expose a clickable logo, so the test as written has no element to drive. `NavBar.clickLogo()` stays in the page class (targets the `<span>`) so the method is there if behavior changes, but no test class will be generated for #157.

Session-2 nav suite will therefore implement #153, #154, #155, #156 — and `Test157_LogoNavigation` will be a stub class with `@Ignore` + the explanation, so the PractiTest ID mapping is still complete.

---

## Other open questions (carried from plan §15)

These remain unanswered after session 1; they're blockers for sessions 3 and 4.

1. **Error popup type** — modal (`role="dialog"`), toast (transient), or inline? Determines popup locator + `errorText()` read. (Plan §15 Q3 in the original list.)
2. **CSV export (#174)** — real browser download or in-page render? Drives whether `CsvUtil.waitForCsv` is used.
3. **#158 Return non-exist order** — error / disabled / empty dropdown / no-op? We assert whichever you confirm.
4. **Known manual bugs** — anything we should *not* codify as expected so the STR can list it as a bug.
5. **Logo (new, see above)** — which of the three interpretations applies?

---

## Next discovery pass (Session 2.5 — before Session 3)

After Session 2's nav tests pass, we run a richer `DomDiscovery2` that:

- selects a category, dumps the product-grid DOM,
- adds a product to the order, dumps the summary-row DOM,
- intentionally triggers each validation rule (R1/R2/R3) and dumps the popup DOM,
- clicks Export on a hot history, dumps whatever Export does.

That gives Session 3 firm locators for all the validation/order tests.
