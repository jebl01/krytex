# Krytex
The amazing http status service

### How to run
```bash
git clone git@github.com:jebl01/krytex.git
cd krytex
./mvnw clean package
java -jar target/krytex-1.0.0-SNAPSHOT-fat.jar

```

### Operations
* HTTP GET /service returns all services and their status.
* HTTP POST /service adds a new service to check with name and URL and assigns a random id.
* HTTP DELETE /service/{service_id} removes the service with the specified service_id.

### No client?
Nope, use Postman ;-)
