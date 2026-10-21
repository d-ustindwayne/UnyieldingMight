# Builds and runs the Unyielding Might API.
#   docker build -f server/Dockerfile -t unyielding-might-api .
#   docker run -p 8080:8080 -e MYSQL_URL=... -e MYSQL_USER=... -e MYSQL_PASSWORD=... -e JWT_SECRET=... unyielding-might-api
# Run from the repository root (the server depends on the :shared module).
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN ./gradlew :server:installDist --no-daemon --console=plain

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/server/build/install/server /app
ENV PORT=8080 SEED_DEMO_DATA=false
EXPOSE 8080
CMD ["/app/bin/server"]
