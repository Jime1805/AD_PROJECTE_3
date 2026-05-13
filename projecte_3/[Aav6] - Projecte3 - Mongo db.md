# PROJECTE 3 — Gestió d'Usuaris: API REST amb MongoDB
## Mòdul: Accés a Dades | Treball en parelles | Spring Boot + MongoDB

---

## Descripció general

Construiràs una **API REST** per gestionar els usuaris d'un centre educatiu.  
Els usuaris poden tenir tres rols: **TEACHER**, **STUDENT** i **FAMILIAR**.

Cada usuari pot tenir associat un **Perfil Acadèmic** amb informació com el curs i el grup.

El projecte usa **exclusivament MongoDB** com a base de dades.

---

## Diferència clau respecte a JPA

A JPA hauríem creat dues taules (`users` i `academic_profiles`) i les hauríem unit amb una clau forana (`@OneToOne`).

A MongoDB ho fem diferent: en comptes de dues col·leccions separades, el perfil acadèmic s'**embeu directament dins del document d'usuari**. No hi ha claus foranes ni JOINs — tot viu en un sol document autocontingut.

```
JPA (relacional)                  MongoDB (document)
─────────────────────────         ──────────────────────────────────────
TAULA users                       COL·LECCIÓ users
  id | username | role            {
  ─────────────────────             "_id": "abc123",
  1  | anna     | STUDENT           "username": "anna",
                                    "role": "STUDENT",
TAULA academic_profiles             "academicProfile": {
  id | grade | user_id               "grade": "2n DAM",
  ──────────────────                 "course": "2024-2025"
  1  | 2nDAM | 1 (FK)              }
                                  }
```

El perfil acadèmic **no té `@Document`** — és una classe Java normal que Mongo serialitza com a objecte aniuat.

---

## Estructura de paquets

```
com.projecte3
├── model/
│   ├── User.java              ← @Document
│   ├── AcademicProfile.java   ← classe simple (NO @Document)
│   └── Role.java              ← enum
├── repository/
│   └── UserRepository.java    ← MongoRepository
├── dto/
│   ├── UserResponseDTO.java
│   ├── UserRequestDTO.java
│   └── AcademicProfileDTO.java
├── mapper/
│   └── UserMapper.java
├── service/
│   └── UserService.java
└── controller/
    └── UserController.java
```

---

## Configuració

### `application.properties`

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/projecte3_db
spring.data.mongodb.database=projecte3_db

# Opcional: veure les consultes MongoDB a la consola
logging.level.org.springframework.data.mongodb=DEBUG
```

> MongoDB crea la base de dades i la col·lecció automàticament la primera vegada que guardis un document. No cal crear res manualment.

### Dependències `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

---

## Model de dades

### Document `User` — col·lecció `users`

| Camp | Tipus | Descripció |
|------|-------|------------|
| `id` | `String` | `@Id`, generat per MongoDB automàticament |
| `firstName` | `String` | Nom |
| `lastName` | `String` | Cognoms |
| `email` | `String` | Ha de ser únic (s'indica amb `@Indexed(unique = true)`) |
| `username` | `String` | Ha de ser únic (`@Indexed(unique = true)`) |
| `password` | `String` | Contrasenya (text pla per a aquesta activitat) |
| `role` | `Role` | Enum: `TEACHER`, `STUDENT`, `FAMILIAR` |
| `dataCreated` | `LocalDateTime` | Data de creació (s'assigna al crear) |
| `academicProfile` | `AcademicProfile` | Perfil **embegut** — pot ser `null` |

```java
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String username;

    private String firstName;
    private String lastName;
    private String password;
    private Role role;
    private LocalDateTime dataCreated;

    private AcademicProfile academicProfile;  // ← embegut, NO @DBRef

    // constructors, getters i setters...
}
```

### Classe `AcademicProfile` — NO és `@Document`

```java
public class AcademicProfile {
    private String grade;        // ex: "2n DAM"
    private String course;       // ex: "2024-2025"
    private String observations;
    private String status;       // "ACTIVE" / "INACTIVE"

    // constructors, getters i setters...
}
```

> `AcademicProfile` **no porta cap anotació de MongoDB**. Quan Mongo guarda un `User`,
> serialitza automàticament el camp `academicProfile` com a objecte aniuat dins el mateix document.

### Enum `Role`

```java
public enum Role {
    TEACHER,
    STUDENT,
    FAMILIAR
}
```

---

## DTOs — Noms i camps obligatoris

### `UserResponseDTO` ← Response DTO

```
id, firstName, lastName, email, username, role, dataCreated,
academicProfile (de tipus AcademicProfileDTO — pot ser null)
```

### `AcademicProfileDTO` ← Response DTO (aniuat dins UserResponseDTO)

```
grade, course, observations, status
```

### `UserRequestDTO` ← Request DTO

```
firstName, lastName, email, username, password, role
grade, course, observations   ← opcionals (per crear el perfil acadèmic)
```

> **No** s'inclou `id` ni `dataCreated` al Request DTO — els gestiona el servidor.

---

## Mapper — `UserMapper`

Ha de ser un `@Component` de Spring.

### `toDto(User user) → UserResponseDTO`
- Retorna `null` si l'entrada és `null`
- Omple tots els camps de `UserResponseDTO`
- Si `user.getAcademicProfile() != null` → convertir a `AcademicProfileDTO`
- Si `user.getAcademicProfile() == null` → el camp queda `null` (sense excepció)

### `toEntity(UserRequestDTO request) → User`
- Retorna `null` si l'entrada és `null`
- Construeix un `User` amb les dades del request
- Assigna `dataCreated = LocalDateTime.now()`
- Si `request.getGrade() != null` → crear un `AcademicProfile` i assignar-lo
- **No** assigna l'`id` (el genera MongoDB)

---

## Repositori — `UserRepository`

```java
public interface UserRepository extends MongoRepository<User, String> {

    // Query Derivation — MongoDB genera la consulta automàticament
    List<User> findByRole(Role role);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
```

> `MongoRepository<User, String>` — el segon paràmetre és `String` (no `Long`)
> perquè els IDs de MongoDB són text (`"64f1a2b3c4d5e6f7a8b9c0d1"`).

---

## Endpoints REST

| Mètode | URL | Descripció | Codi HTTP |
|--------|-----|------------|-----------|
| `GET` | `/api/users` | Llista tots els usuaris | 200 OK |
| `GET` | `/api/users/{id}` | Obté un usuari per ID | 200 OK / 404 |
| `GET` | `/api/users/role/{role}` | Filtra per rol (`TEACHER`, `STUDENT`, `FAMILIAR`) | 200 OK |
| `GET` | `/api/users/username/{username}` | Busca per nom d'usuari | 200 OK / 404 |
| `POST` | `/api/users` | Crea un usuari nou | 201 CREATED |
| `PUT` | `/api/users/{id}` | Actualitza un usuari existent | 200 OK / 404 |
| `DELETE` | `/api/users/{id}` | Esborra un usuari | 204 NO CONTENT / 404 |

---

## ISSUES — Distribució de feina en parelles

Cada membre del grup crea i tanca les seves issues al repositori GitHub.

---

### Integrant 1 — Configuració, Model i Capa de Lectura

---

#### Issue 1: Configuració del projecte

**Descripció:**  
Crea el projecte Spring Boot i configura la connexió a MongoDB.

**Tasques:**
- [ ] Crear el projecte a [start.spring.io](https://start.spring.io) amb les dependències: `Spring Web`, `Spring Data MongoDB`
- [ ] Configurar `application.properties` amb la URI de MongoDB
- [ ] Crear l'estructura de paquets (model, repository, dto, mapper, service, controller)
- [ ] Verificar que l'aplicació arrenca sense errors i es connecta a MongoDB

**Criteri d'acceptació:** L'aplicació arrenca i la consola mostra la connexió a MongoDB sense errors.

---

#### Issue 2: Crear el model — `Role`, `AcademicProfile`, `User`

**Descripció:**  
Crea l'enum `Role`, la classe `AcademicProfile` (embedded) i el document `User`.

**Tasques:**
- [ ] Crear `Role.java` amb els valors `TEACHER`, `STUDENT`, `FAMILIAR`
- [ ] Crear `AcademicProfile.java` (classe normal, **sense** `@Document`) amb els camps: `grade`, `course`, `observations`, `status`
- [ ] Crear `User.java` amb `@Document(collection = "users")`
- [ ] Afegir tots els camps de `User` (inclòs el camp `academicProfile` de tipus `AcademicProfile`)
- [ ] Usar `@Indexed(unique = true)` per a `email` i `username`

**Criteri d'acceptació:** La classe compila. En arrencar l'app i guardar un usuari de prova, MongoDB crea la col·lecció `users` amb el perfil aniuat dins el document.

---

#### Issue 3: Crear `UserRepository`

**Descripció:**  
Implementa el repositori MongoDB amb els mètodes de consulta necessaris.

**Tasques:**
- [ ] Crear `UserRepository` que extengui `MongoRepository<User, String>`
- [ ] Afegir `findByRole(Role role)` per filtrar per rol
- [ ] Afegir `findByUsername(String username)` per buscar per nom d'usuari
- [ ] Afegir `findByEmail(String email)` per buscar per email

**Criteri d'acceptació:** El repositori s'injecta correctament al Service sense errors.

---

#### Issue 4: Implementar `UserService` — operacions de lectura

**Descripció:**  
Implementa els mètodes de lectura del servei. Requereix el `UserMapper` (Issue 7).  
Coordina't amb el teu company.

**Tasques:**
- [ ] Crear `UserService` amb `@Service` i injecció per constructor (`private final`)
- [ ] Implementar `findAll()` → `List<UserResponseDTO>`
- [ ] Implementar `findById(String id)` → `UserResponseDTO` (null si no existeix)
- [ ] Implementar `findByRole(Role role)` → `List<UserResponseDTO>`
- [ ] Implementar `findByUsername(String username)` → `UserResponseDTO` (null si no existeix)

**Criteri d'acceptació:** Els mètodes retornen DTOs correctes amb dades de prova inserides directament a MongoDB.

---

#### Issue 5: Implementar `UserController` — endpoints GET

**Descripció:**  
Implementa els endpoints de lectura al controlador REST.

**Tasques:**
- [ ] Crear `UserController` amb `@RestController` i `@RequestMapping("/api/users")`
- [ ] `GET /api/users` → `findAll()`
- [ ] `GET /api/users/{id}` → `findById()` (404 si no existeix)
- [ ] `GET /api/users/role/{role}` → `findByRole()`
- [ ] `GET /api/users/username/{username}` → `findByUsername()` (404 si no existeix)
- [ ] Retornar `ResponseEntity<>` amb els codis HTTP correctes

**Criteri d'acceptació:** Els quatre endpoints retornen JSON correcte quan es prova amb Postman.

---

### Integrant 2 — DTOs, Mapper i Capa d'Escriptura

---

#### Issue 6: Crear els DTOs — `UserResponseDTO`, `AcademicProfileDTO`, `UserRequestDTO`

**Descripció:**  
Crea les tres classes DTO amb els camps indicats a l'enunciat.

**Tasques:**
- [ ] Crear `AcademicProfileDTO.java` amb: `grade`, `course`, `observations`, `status`
- [ ] Crear `UserResponseDTO.java` amb: `id`, `firstName`, `lastName`, `email`, `username`, `role`, `dataCreated`, `academicProfile` (de tipus `AcademicProfileDTO`)
- [ ] Crear `UserRequestDTO.java` amb: `firstName`, `lastName`, `email`, `username`, `password`, `role`, `grade`, `course`, `observations`
- [ ] Afegir constructors buits, getters i setters a tots

**Criteri d'acceptació:** Les tres classes compilen sense errors.

---

#### Issue 7: Implementar `UserMapper`

**Descripció:**  
Implementa el mapper per convertir entre el document `User` i els DTOs.  
Requereix que la Issue 2 i la Issue 6 estiguin acabades.

**Tasques:**
- [ ] Crear `UserMapper.java` com a `@Component`
- [ ] Implementar `toDto(User user) → UserResponseDTO`:
  - Null-safe: retorna `null` si l'entrada és `null`
  - Si `user.getAcademicProfile() != null` → convertir a `AcademicProfileDTO`
- [ ] Implementar `toEntity(UserRequestDTO request) → User`:
  - Null-safe: retorna `null` si l'entrada és `null`
  - Assignar `dataCreated = LocalDateTime.now()`
  - Si `request.getGrade() != null` → crear `AcademicProfile` i assignar-lo

**Criteri d'acceptació:** El mapper converteix correctament en els dos sentits sense NullPointerException.

---

#### Issue 8: Implementar `UserService` — operacions d'escriptura

**Descripció:**  
Implementa els mètodes d'escriptura del servei.  
Requereix la Issue 3 (repositori) i la Issue 7 (mapper).

**Tasques:**
- [ ] Implementar `create(UserRequestDTO request) → UserResponseDTO`:
  - Verificar que l'email no existeixi ja (retornar `null` si existeix)
  - Convertir a entitat amb el Mapper
  - Guardar amb `userRepository.save()`
  - Retornar el DTO de l'entitat guardada (amb l'`id` generat per Mongo)
- [ ] Implementar `update(String id, UserRequestDTO request) → UserResponseDTO`:
  - Verificar que l'usuari existeix (retornar `null` si no)
  - Actualitzar els camps (respectar el `dataCreated` original)
  - Guardar i retornar el DTO actualitzat
- [ ] Implementar `delete(String id) → boolean`:
  - Retornar `false` si no existeix
  - Esborrar i retornar `true`

**Criteri d'acceptació:** Es pot crear, actualitzar i esborrar usuaris. Els canvis es veuen a MongoDB.

---

#### Issue 9: Implementar `UserController` — endpoints POST, PUT i DELETE

**Descripció:**  
Implementa els endpoints d'escriptura al controlador REST.

**Tasques:**
- [ ] `POST /api/users` → `create()` → `201 CREATED` amb `UserResponseDTO` (o `409 CONFLICT` si l'email ja existeix)
- [ ] `PUT /api/users/{id}` → `update()` → `200 OK` (o `404 NOT FOUND`)
- [ ] `DELETE /api/users/{id}` → `delete()` → `204 NO CONTENT` (o `404 NOT FOUND`)

**Criteri d'acceptació:** Els tres endpoints funcionen correctament amb Postman. Verificar a MongoDB que els documents es creen, actualitzen i esborren correctament.

---

#### Issue 10: Endpoint de filtre per rol i validació final

**Descripció:**  
Verifica que el filtre per rol funciona correctament i integra tota la feina amb l'Integrant 1.

**Tasques:**
- [ ] Provar `GET /api/users/role/STUDENT` — verificar que retorna només estudiants
- [ ] Provar `GET /api/users/role/TEACHER` — verificar que retorna només professors
- [ ] Inserir a MongoDB (via Postman) com a mínim 5 usuaris de rols diferents i verificar tots els endpoints
- [ ] Provar el cas d'error: `GET /api/users/{id}` amb un ID que no existeix → ha de retornar 404
- [ ] Provar el cas d'error: `POST /api/users` amb un email que ja existeix → ha de retornar 409

**Criteri d'acceptació:** Tots els endpoints funcionen correctament. La col·lecció `users` a MongoDB mostra els documents amb el perfil acadèmic correctament embegut.

---

## Resum de la distribució

| Issue | Integrant | Depèn de |
|-------|-----------|----------|
| 1 — Configuració | 1 | — |
| 2 — Model (`User`, `AcademicProfile`, `Role`) | 1 | Issue 1 |
| 3 — `UserRepository` | 1 | Issue 2 |
| 4 — `UserService` lectura | 1 | Issues 3 + 7 |
| 5 — `UserController` GET | 1 | Issue 4 |
| 6 — DTOs | 2 | — |
| 7 — `UserMapper` | 2 | Issues 2 + 6 |
| 8 — `UserService` escriptura | 2 | Issues 3 + 7 |
| 9 — `UserController` POST/PUT/DELETE | 2 | Issue 8 |
| 10 — Filtre per rol + validació final | 2 | Totes |

> Les Issues 1, 2, 3 i les Issues 6, 7 es poden treballar en paral·lel des del primer dia.

---

## Recorda

- `AcademicProfile` **no és** `@Document` — és una classe normal que s'embeu dins `User`
- L'`@Id` de MongoDB és `String`, no `Long`
- No cal `@GeneratedValue` — MongoDB genera l'ID sol
- No hi ha `cascade`, `fetch` ni `@JoinColumn` — MongoDB no té relacions ni claus foranes
- El **Mapper** es crida des del **Service**, mai des del Controller
- El **Controller** treballa exclusivament amb DTOs — mai retorna entitats directament
- El **Repository** mai rep ni retorna DTOs

> Consulta el fitxer `iniciacio_mongodb.md` per repassar els conceptes bàsics de MongoDB.
