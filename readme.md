# Licencias

Licencias es una aplicación web construida con Spring Boot que gestiona licencias de conducir.

## 📁 Estructura del proyecto

```
main
└── java
    └── met.agiles.licencias
        ├── configuration
        ├── controllers
        ├── dto
        ├── enums
        ├── exceptions
        ├── persistance
        │   ├── models
        │   └── repository
        ├── services
        └── LicenciasApplication.java
```

## 🧾 Requisitos

- Java 24
- Maven 3.8+
- PostgreSQL 13+
- Git

## 📦 Clonar el repositorio

```bash
git clone https://github.com/Style12341/licencias
cd licencias
```

> Asegúrate de reemplazar la URL con la de tu repositorio real.

## ⚙️ Configuración de la base de datos

### 1. Verifica que el usuario `postgres` tenga una contraseña

Abre la consola de PostgreSQL:

```bash
sudo -u postgres psql
```

Y luego dentro de la consola:

```sql
\password postgres
```

Salir de PostgreSQL:

```sql
\q
```

### 2. Crear la base de datos `licencias`

Desde tu terminal:

```bash
createdb -U postgres licencias
```

Si necesitas autenticación:

```bash
PGPASSWORD=postgres createdb -U postgres licencias
```

También puedes hacerlo en la consola interactiva:

```bash
psql -U postgres
```

Luego:

```sql
CREATE DATABASE licencias;
\q
```

## ▶️ Ejecución

### Usando Maven

```bash
./mvnw spring-boot:run
```

o

```bash
mvn spring-boot:run
```

### Usando la clase principal

Desde tu IDE o desde terminal:

```bash
./mvnw clean package
java -jar target/licencias-0.0.1-SNAPSHOT.jar
```


## 📚 Dependencias principales

- Spring Boot Web
- Spring Boot Data JPA
- PostgreSQL
- Spring Security
- Thymeleaf
- Lombok
- Spring REST Docs

## 🧪 Tests

Ejecuta los tests con:

```bash
./mvnw test
```

## 📄 Licencia

Este proyecto no tiene licencia asignada actualmente.

---
**Desarrollado por:** [Tu Nombre o Equipo]
