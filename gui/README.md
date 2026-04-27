# PortfolioDB Java Swing + JDBC GUI

This is a basic DBMS project frontend for your MySQL `portfoliodb` schema.

## Implemented Features

- Dashboard (6 live SQL metrics + refresh)
- Add Investor
- Add Asset
- Add Transaction
- View Portfolio (via `CALL get_portfolio()`)
- View Total Investment by Investor (via `SELECT total_investment(?)`)
- View/Filter Transactions
- Optional operations: update transaction quantity, delete transaction

## Project Structure

- `src/main/java/com/portfoliodb/App.java` -> app entry point
- `src/main/java/com/portfoliodb/config/DBConfig.java` -> DB URL/user/password config
- `src/main/java/com/portfoliodb/db/DBConnection.java` -> JDBC connection helper
- `src/main/java/com/portfoliodb/dao/PortfolioDAO.java` -> SQL operations
- `src/main/java/com/portfoliodb/ui/MainFrame.java` -> dashboard + navigation shell
- `src/main/java/com/portfoliodb/ui/panels/*` -> feature panels

## Database Requirement

Run your schema first (from `db/schema.txt`) in MySQL:

- database: `portfoliodb`
- tables: investors, assets, transactions, prices
- trigger/function/procedure already included in your script

Important:
- Dashboard current value/profit queries need matching rows in `prices`.
- If `prices` is empty, these metrics will appear as zero.

## Connection Settings

By default, the app uses:

- URL: `jdbc:mysql://localhost:3306/portfoliodb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- User: `root`
- Password: `root`

Override these using environment variables:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`

PowerShell example:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/portfoliodb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER="root"
$env:DB_PASSWORD="your_password"
```

## Build & Run

### Option A: Maven (recommended)

Install Maven and then run:

```powershell
cd gui
mvn clean compile
mvn exec:java
```

### Option B: VS Code Java Extension

- Open `App.java`
- Click Run Java (or run from the VS Code run icon)

Make sure MySQL is running locally before starting the app.

## Copilot Run Instructions (Any Machine)

Use this when asking Copilot to run the app in VS Code so it follows a reliable sequence.

### 1. Open the correct folder in VS Code

- Open the workspace root: `portfoliodb`
- Ensure the project folder `gui` exists and contains `pom.xml`

### 2. Set DB credentials first (same terminal session)

From VS Code terminal:

```powershell
cd gui
```

Then either:

- Run script and fill placeholders first:

```powershell
.\scripts\set-env.example.ps1
```

- Or set variables manually:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/portfoliodb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER="root"
$env:DB_PASSWORD="<your_real_password>"
```

Important:
- Do not open a new terminal after setting env variables.
- Run Maven in the same terminal where env variables were set.

### 3. Ask Copilot with this exact prompt

```text
Run the PortfolioDB app from the gui folder. First verify Java and Maven, then compile and run using the same terminal session. If mvn is not on PATH, use the local mvn.cmd path. Use existing DB_URL/DB_USER/DB_PASSWORD environment variables and do not reset them.
```

### 4. Commands Copilot should execute

```powershell
cd gui
mvn clean compile
mvn exec:java
```

Fallback if Maven is not recognized:

```powershell
& "<your-maven-install-path>\bin\mvn.cmd" clean compile
& "<your-maven-install-path>\bin\mvn.cmd" exec:java
```

### 5. Quick error fixes

- Error: `no POM in this directory`
	- Fix: run from `gui` folder or use `mvn -f gui\pom.xml ...`
- Error: `Access denied for user ...`
	- Fix: DB credentials are wrong or missing in this terminal session
- App opens but dashboard values are 0
	- Fix: ensure `prices` table has data

## Teammate Copilot Prompt (One-Try, Reactor-Safe Run)

Copy this prompt and give it to GitHub Copilot Chat in VS Code:

```text
Run my Java app from this repository using Maven, and make it run correctly on the first try with no retries.

Workspace root on my machine is:
C:\Users\<username>\OneDrive\Desktop\portfoliodb

Important repo detail:
There is a root .mvn/maven.config that contains:
-pl
gui
So you must run from workspace root through the reactor, not by forcing -f gui\pom.xml.

DB credentials are already present in:
C:\Users\<username>\OneDrive\Desktop\portfoliodb\db_cloud_cred\cred.txt

Do these steps in order, in ONE PowerShell terminal session and do not skip any step:
1) Set location to the workspace root path above.
2) Verify required files exist with Test-Path:
	- .\pom.xml
	- .\gui\pom.xml
	- .\db_cloud_cred\cred.txt
	If any are missing, stop and print a clear error.
3) Verify Java and Maven are available with:
	java -version
	mvn -version
4) Read db_cloud_cred\cred.txt and set environment variables in the same terminal session:
	- DB_URL from host+port as jdbc:mysql://<host>:<port>/portfoliodb?useSSL=true&requireSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
	- DB_USER from username
	- DB_PASSWORD from password
5) Compile from workspace root (reactor-aware):
	mvn -q -DskipTests compile
6) Run GUI module from workspace root:
	mvn -pl gui exec:java
7) If mvn is not recognized, ask for Maven install path and use:
	& "<maven install path>\\bin\\mvn.cmd" -q -DskipTests compile
	& "<maven install path>\\bin\\mvn.cmd" -pl gui exec:java

Rules:
- Do not run from random folders.
- Keep compile and run in the same terminal session.
- Do not use -f gui\\pom.xml for this repo because root maven.config already selects module gui.
- If any step fails, stop and report exact error plus the next fix command.
```

Example command pair:

```powershell
mvn -q -DskipTests compile
mvn -pl gui exec:java
```

## Team Setup (Railway + Local Java App)

If the entire `portfoliodb` folder is already shared with teammates, they only need to run the app.

### 1. Open PowerShell in the shared gui folder

```powershell
cd path\to\portfoliodb\gui
```

### 2. Load DB environment variables

```powershell
.\scripts\set-env.example.ps1
```

Important:
- Edit `scripts/set-env.example.ps1` and replace placeholders with your real DB user/password first.
- Run the script and `mvn exec:java` in the same PowerShell terminal window.

### 3. Compile and run

```powershell
mvn clean compile
mvn exec:java
```

### 4. If `mvn` is not recognized

Install Maven and reopen terminal, then run:

```powershell
mvn clean compile
mvn exec:java
```

If needed, run Maven using your own local Maven path on your device (example format):

```powershell
& "<your-maven-install-path>\bin\mvn.cmd" clean compile
& "<your-maven-install-path>\bin\mvn.cmd" exec:java
```

### 5. Expected result

- Desktop app opens: `PortfolioDB - Java Swing + JDBC`
- Dashboard loads Railway-backed data

## Final Teammate Handoff Checklist (Current Build)

Use this after sharing the full `portfoliodb` folder as zip.

### 1) Prerequisites on teammate machine

- Java 17+ installed
- Maven installed and available on PATH
- Internet access to Railway DB host/port

### 2) Open and run from workspace root

From PowerShell in extracted folder root:

```powershell
cd path\to\portfoliodb
mvn -q -DskipTests compile
mvn -pl gui exec:java
```

### 3) Credentials source

- Primary: `db_cloud_cred/cred.txt` (auto-read by app)
- If needed, override in same terminal session:

```powershell
$env:DB_URL="jdbc:mysql://<host>:<port>/portfoliodb?useSSL=true&requireSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER="<user>"
$env:DB_PASSWORD="<password>"
```

### 4) Current functional expectations

- Add Transaction supports `BUY` and `SELL`
- For `SELL`, app blocks quantity greater than currently owned units
- Transaction page filters assets by selected asset type
- View Investment shows only assets currently owned by selected investor
- View Portfolio is investor-based and shows owned assets, units, current value, total portfolio value, and realized P/L (sold-units based)
- Dashboard shows 6 synced metrics:
	- Total Investors
	- Assets Under Management
	- Total Transactions
	- Average Daily Trading Volume
	- Asset Types
	- Overall Profit/Loss (red when cumulative loss)

### 5) Quick smoke test before presentation

- Add one `BUY` transaction
- Add one valid `SELL` transaction for same investor+asset
- Open View Portfolio and verify units/current value/realized P/L update
- Open Dashboard and verify metrics refresh correctly

### 6) Common fixes

- `Could not find the selected project in the reactor: gui`
	- Run from workspace root and use `mvn -pl gui exec:java`
- DB auth/connect errors
	- Verify `db_cloud_cred/cred.txt` exists and credentials are valid
- Stale UI values
	- Use panel refresh buttons or re-open the panel

## Safe GUI Changes Checklist (Keep First-Run Reliability)

If you modify UI features, use this checklist before sharing with teammates.

### A) Keep run configuration stable

- Do not rename or remove `src/main/java/com/portfoliodb/App.java` without updating Maven main class config.
- Keep `gui/pom.xml` valid and buildable.
- Keep project path assumptions unchanged (`portfoliodb/gui/pom.xml`).

### B) Keep DB config behavior stable

- Do not break `DBConfig` / `DBConnection` / `DatabaseManager` wiring.
- Keep support for `DB_URL`, `DB_USER`, `DB_PASSWORD` from the active terminal session.
- If env vars are not set, keep credential fallback compatible with `db_cloud_cred/cred.txt`.

### C) Keep UI responsiveness stable

- For any new DB button action, use the existing background task pattern (`DatabaseTaskRunner.runDatabaseTask(...)`).
- Disable the source button while running DB work and restore it on completion.
- Do not run long SQL/network calls on the Swing UI thread.

### D) Verify before commit/share

- From workspace root: `mvn -q -DskipTests compile`
- Run app once using exact pom targeting: `mvn -f "<full path to gui\\pom.xml>" exec:java`
- Verify at least one action from each panel opens/loads without freezing.

### E) Fast rollback if run reliability breaks

- Revert only the last GUI behavior change.
- Re-run compile and launch commands above.
- If still failing, capture exact terminal error and fix one issue at a time.

## Presentation Security Checklist

- Never commit real DB passwords to GitHub.
- Keep only demo/non-sensitive data in Railway.
- Share credentials only with teammates.
- Rotate password after presentation.
- Optionally drop the demo DB user after viva.

## Why this is best for your project

- Everyone runs the same Java Swing app locally.
- Everyone sees the same data from one shared DB.
- No local MySQL installation required for teammates.
- No need for Vercel deployment for this desktop architecture.

## Notes for Viva / Evaluation

Each UI action maps to one DB concept:

- Add Investor -> INSERT + unique constraint
- Add Asset -> INSERT + type constraint
- Add Transaction -> FK + trigger validation
- View Portfolio -> JOIN + GROUP BY + procedure
- View Investment -> function call
- Transactions filter -> SELECT + WHERE
- Dashboard -> aggregate SQL summaries
