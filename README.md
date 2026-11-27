# Statusplattformen – backend

## Hvorfor finnes løsningen?

Statusplattformen gir en samlet oversikt over driftsstatusen til NAVs tjenester.
Den gjør det enklere å se hva som fungerer, hvor det er problemer, og hvilke
tjenester og komponenter som henger sammen.

Backend er datagrunnlaget for denne oversikten: den tar imot statusmeldinger,
lagrer dem og leverer informasjonen til nettsiden. Selve visningen ligger i
frontend, som er et eget prosjekt.

## Hvordan fungerer det?

1. **Backend får beskjed om hvordan tjenestene fungerer.** Et eget program sjekker
   tjenestene jevnlig og sender resultatene til backend. Dette programmet kalles
   en poller og kjører utenfor backend. Backend kan også ta imot én statusmelding om gangen.
2. **Informasjonen lagres i databasen.** Backend lagrer statusen for hver tjeneste
   i PostgreSQL. Den tar også vare på tidligere meldinger og statusendringer,
   slik at man kan se hvordan tjenesten fungerer nå, og når det har vært problemer.
3. **Backend lager en samlet oversikt.** Den henter den siste kjente statusen for
   tjenestene og grupperer dem i områder på en oversiktsside, kalt et dashbord.
   Statusen for et område følger tjenesten som har størst problemer.
   Backend gir i tillegg informasjon om hvilke tjenester som er avhengige av
   hverandre, åpningstider, driftsmeldinger og planlagt vedlikehold.
4. **Nettsiden viser oversikten, og abonnenter kan få beskjed.** Nettsiden henter
   informasjonen fra backend gjennom API-et under `/rest` og viser den til brukeren.
   Når polleren melder om en statusendring, gjør backend klar e-postvarsler til dem
   som abonnerer på tjenesten. En egen jobb sender ut varslene.

For eksempel: Hvis polleren melder at en tjeneste ikke fungerer, lagrer backend
den nye statusen. Neste gang nettsiden henter oversikten, vises problemet på både
tjenesten og området den tilhører. De som abonnerer på tjenesten, kan få en e-post
og slipper å følge med på nettsiden hele tiden.

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
