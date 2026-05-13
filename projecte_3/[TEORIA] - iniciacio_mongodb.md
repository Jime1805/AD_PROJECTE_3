# Iniciació a MongoDB amb Spring Boot
## Guia pràctica per al Projecte 3

---

## Què és MongoDB?

MongoDB és una base de dades **orientada a documents**. En comptes de guardar les dades en taules amb files i columnes (com MySQL), MongoDB guarda les dades com a **documents JSON**.

### MySQL vs MongoDB — la diferència clau

| Concepte MySQL | Equivalent MongoDB | Descripció |
|---------------|-------------------|------------|
| Taula (`TABLE`) | Col·lecció (`Collection`) | Un grup de dades relacionades |
| Fila (`ROW`) | Document (`Document`) | Una entrada de dades |
| Columna (`COLUMN`) | Camp (`Field`) | Un valor dins el document |
| ID numèric (`BIGINT`) | ID de text (`String`) | MongoDB genera IDs com `"64f1a2b3c4d5e6f7a8b9c0d1"` |

### Exemple visual

**A MySQL** (taula `users`):
```
| id | username | action  |
|----|----------|---------|
|  1 | anna     | CREATE  |
|  2 | joan     | DELETE  |
```

**A MongoDB** (col·lecció `user_logs`):
```json
{ "_id": "64f1a2b3...", "userId": 1, "username": "anna", "action": "CREATE", "timestamp": "2024-05-01T10:30:00" }
{ "_id": "64f1a2c4...", "userId": 2, "username": "joan", "action": "DELETE", "timestamp": "2024-05-01T11:00:00" }
```

---

## Per què MongoDB per als logs?

Els logs d'auditoria són un bon cas d'ús per a MongoDB perquè:

- **Creixen molt ràpidament** — MongoDB escala bé per a grans volums de dades
- **Esquema flexible** — cada log pot tenir camps lleugerament diferents sense trencar res
- **No hi ha relacions** — els logs no necessiten JOINs complexos
- **Ràpid d'escriure** — optimal per a operacions d'escriptura freqüent

---

## Conceptes bàsics per al projecte

### 1. `@Document` — equivalent a `@Entity`

En comptes de `@Entity` (JPA), els documents MongoDB usen `@Document`.  
L'atribut `collection` és el nom de la col·lecció (equivalent al nom de la taula).

```java
@Document(collection = "user_logs")
public class UserLog {
    // ...
}
```

### 2. `@Id` — l'identificador del document

A MongoDB, el camp `@Id` ha de ser de tipus `String` (no `Long` com a JPA).  
MongoDB genera automàticament un ID únic en format hexadecimal.

```java
@Document(collection = "user_logs")
public class UserLog {

    @Id
    private String id;          // MongoDB genera: "64f1a2b3c4d5e6f7a8b9c0d1"

    private Long userId;
    private String username;
    private String action;      // "CREATE" o "DELETE"
    private LocalDateTime timestamp;

    // constructor, getters i setters...
}
```

> **Diferència important:** A JPA usem `@GeneratedValue` per generar l'ID. A MongoDB **no cal** — el genera sol quan es guarda el document.

### 3. `MongoRepository` — equivalent a `JpaRepository`

En comptes de `JpaRepository`, els repositoris MongoDB extenen `MongoRepository`.  
El segon paràmetre del tipus és el tipus de l'ID (`String`, no `Long`).

```java
// JPA (com ja coneixes):
public interface UserRepository extends JpaRepository<User, Long> { }

// MongoDB (nou):
public interface UserLogRepository extends MongoRepository<UserLog, String> { }
```

`MongoRepository` té els mateixos mètodes bàsics que ja coneixes:
- `save(document)` → guarda o actualitza el document
- `findAll()` → retorna tots els documents
- `findById(id)` → retorna un document per ID
- `deleteById(id)` → esborra un document

### 4. Guardar un document al Service

Per guardar un log, és exactament igual que guardar una entitat JPA:

```java
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserLogRepository userLogRepository;  // ← repositori MongoDB
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       UserLogRepository userLogRepository,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userLogRepository = userLogRepository;
        this.userMapper = userMapper;
    }

    public UserResponseDTO create(UserRequestDTO request) {
        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);

        // Guardar el log a MongoDB
        UserLog log = new UserLog();
        log.setUserId(saved.getId());
        log.setUsername(saved.getUsername());
        log.setAction("CREATE");
        log.setTimestamp(LocalDateTime.now());
        userLogRepository.save(log);   // ← guarda a MongoDB

        return userMapper.toDto(saved);
    }
}
```

---

## Configuració a `application.properties`

Per connectar Spring Boot a MongoDB, afegeix aquestes línies al fitxer de propietats:

```properties
spring.mongodb.uri=mongodb://root:example@localhost:27017/projecte3_db?authSource=admin
spring.mongodb.database=projecte3_db

# Opcional: veure les consultes MongoDB a la consola
logging.level.org.springframework.data.mongodb=DEBUG
```

- `uri` — adreça del servidor MongoDB (per defecte, `localhost` port `27017`)
- `database` — nom de la base de dades (la crea automàticament si no existeix)

> **No cal crear la col·lecció manualment.** La primera vegada que guardi un document, MongoDB crea la col·lecció automàticament.

---

## Dependència al `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

---

## Resum ràpid

| Element | JPA (MySQL) | MongoDB |
|---------|-------------|---------|
| Anotació classe | `@Entity` | `@Document(collection = "nom")` |
| Tipus de l'ID | `Long` + `@GeneratedValue` | `String` (MongoDB el genera sol) |
| Repositori base | `JpaRepository<T, Long>` | `MongoRepository<T, String>` |
| Guardar | `repository.save(entitat)` | `repository.save(document)` |
| Properties | `spring.datasource.url=...` | `spring.data.mongodb.uri=...` |

---

## Diferència clau de mentalitat

A **MySQL** (JPA): penses en **taules, columnes i relacions** (FK, JOIN...).  
A **MongoDB**: penses en **documents JSON** que es guarden tal com arriben, sense relacions forçades.

Per a aquest projecte, **no necessites saber res més de MongoDB** que el que s'explica aquí.  
Usa MySQL per a `User` i `AcademicProfile` (dades estructurades amb relació).  
Usa MongoDB per a `UserLog` (registre d'accions, sense necessitat de relacions).
