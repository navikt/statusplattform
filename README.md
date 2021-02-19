
## Development

1. Start SQL Server with docker:
   * `docker run --name brukergrupper-db -e "ACCEPT_EULA=y" -e "SA_PASSWORD=yourStrong(!)Password" -p 1433:1433 -d mcr.microsoft.com/mssql/server:2019-latest`
   * (To restart, simply run `docker start brukergrupper-db`)
2. Copy `brukergrupper.properties.template` to `brukergrupper.properties` and fill in the missing properties 
3. Start `PortalServer`


## Building and deploying 

1. Build the whole project from this directory with `mvn package`
   * This builds the React code and runs frontend tests
   * This packages the whole application together in `target/brukergrupper-server-*.jar`
