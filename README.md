# Statusplattformen – backend

## Hvorfor finnes løsningen?

Statusplattformen gir en samlet oversikt over driftsstatusen til NAVs tjenester.
Den gjør det enklere å se hva som fungerer, hvor det er problemer, og hvilke
tjenester og komponenter som henger sammen.

Backend er datagrunnlaget for denne oversikten: den tar imot statusmeldinger,
lagrer dem og leverer informasjonen til nettsiden. Selve visningen ligger i
frontend, som er et eget prosjekt.

## Hvordan fungerer det?

1. **Status kommer inn.** En ekstern poller (en jobb som jevnlig sjekker tjenester)
   sender resultater til API-et. API-et kan også ta imot enkeltvise statusmeldinger.
   Backend er altså ikke selve polleren.
2. **Status og historikk lagres.** PostgreSQL lagrer tjenestene, statusmeldingene
   og endringene i status, slik at man kan se både nåsituasjonen og tidligere problemer.
3. **Oversikten settes sammen.** Backend henter siste status og samler tjenester
   i områder og dashbord. Et område får den dårligste statusen blant tjenestene sine.
   API-et tilbyr også avhengigheter, åpningstider, driftsmeldinger og planlagt vedlikehold.
4. **Frontend viser resultatet.** Nettsiden henter data fra REST-API-et under `/rest`.
   Ved statusendringer fra polleren legges e-postvarsler i kø for abonnenter;
   en bakgrunnsjobb sender dem.

For eksempel: Når polleren melder at en tjeneste er nede, lagres den nye statusen.
Neste gang nettsiden henter dashbordet, vises problemet både på tjenesten og på
området den tilhører. Abonnenter kan få beskjed uten å følge med på nettsiden hele tiden.

## Hvor finner jeg koden?

Backend er skrevet i Java 21 og bygget med Maven.

| Modul | Ansvar |
| --- | --- |
| `api` | REST-endepunkter og logikken som setter sammen svar til frontend. |
| `core` | Datamodeller, databasespørringer og Flyway-migreringer. |
| `jobs` | Bakgrunnsjobber for åpningstider, e-postvarsler og opprydding av engangskoder. |
| `infrastructure` | Innlogging, tilgangskontroll og håndtering av webforespørsler. |
| `config` | Konfigurasjon fra miljøvariabler. |
| `server` | Starter Jetty-serveren og kobler API, database og bakgrunnsjobber sammen. |

Et godt sted å begynne er [DashboardControllerHelper](api/src/main/java/no/nav/statusplattform/api/Helpers/DashboardControllerHelper.java)
for å se hvordan oversikten bygges, og [RecordControllerHelper](api/src/main/java/no/nav/statusplattform/api/Helpers/RecordControllerHelper.java)
for å se hvordan innkommende status behandles.

## Lokal kjøring

Du trenger Java 21 og PostgreSQL med databasen `navstatus`.
`docker compose up -d postgres-db` starter en lokal database på port `3432`.
Bygg backend med `./mvnw clean install`, og start
`no.nav.statusplattform.server.StatusplattformServer` fra IDE-en.

Sett `DB_JDBC_URL=jdbc:postgresql://localhost:3432/navstatus?sslmode=disable`
for denne lokale databasen. Sett også `ENV=local`, `AZURE_APP_CLIENT_ID=local`
og `AZURE_APP_CLIENT_SECRET=local` som lokale plassholdere, ikke ekte innloggingsverdier.
Se [AppConfig.java](config/src/main/java/no/nav/statusplattform/AppConfig.java) for øvrige miljøvariabler.

Serveren bruker port `3005` som standard. API-dokumentasjonen finnes på
`http://localhost:3005/doc`. Databasemigreringer kjøres automatisk ved oppstart.
