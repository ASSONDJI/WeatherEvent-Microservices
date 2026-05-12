# Configuration des services

## Configuration locale

1. Créer les fichiers `application-local.yml` dans chaque service:

### Weather Service
```bash
cp services/weather-service/src/main/resources/application-local.template.yml \
   services/weather-service/src/main/resources/application-local.yml
# Éditer avec ta clé OpenWeatherMap
cp services/event-service/src/main/resources/application-local.template.yml \
   services/event-service/src/main/resources/application-local.yml
# Éditer avec ta clé Ticketmaster
# Avec profile local (utilise application-local.yml)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Avec profile prod (utilise Kubernetes secrets)
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Avec Docker
docker-compose up
