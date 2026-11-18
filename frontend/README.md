# Frontend - Sistema de Parqueadero FET

Cliente React (Vite) para probar visualmente el backend. Tiene cinco pestañas:

- **Propietarios, Vehículos, Espacios, Movimientos** — consumen la **API REST**
  (`/api/**`), una llamada por recurso.
- **GraphQL** — consume el **endpoint GraphQL** (`/graphql`): arma todo el panel con
  una sola consulta anidada y escucha los movimientos en tiempo real por
  subscription sobre WebSocket.

## Requisitos

- Node.js 18+
- El backend corriendo y accesible por HTTP (ver `../backend`)

## Configuración

La URL base de la API se lee de la variable `VITE_API_BASE_URL` (ver `.env`). Por defecto:

```
VITE_API_BASE_URL=http://localhost:8080
```

El backend escucha en el puerto `8080` tanto si lo ejecutas con `./gradlew bootRun`
(`server.port` en `application.yml`) como si lo levantas con `04_Docker/docker-compose.yml`
(mapeo `8080:8080`), así que este valor sirve para los dos casos. Si cambias el puerto,
crea un archivo `.env.local` (ignorado por git) con el nuevo valor. Ojo: no puedes correr
el backend local y el contenedor a la vez, porque se pelean el `8080`.

## Uso

```bash
npm install
npm run dev
```

Abre `http://localhost:5173`.

## Pestaña GraphQL

| Archivo | Rol |
|---------|-----|
| `src/api/graphqlClient.js` | Transporte: `gql()` por HTTP y `gqlSubscribe()` por WebSocket |
| `src/api/graphqlParqueadero.js` | Documentos GraphQL (queries, mutations, subscription) |
| `src/hooks/useMovimientosEnVivo.js` | Mantiene la subscription y acumula los últimos eventos |
| `src/pages/GraphQLPage.jsx` | La vista |

**Sin dependencias nuevas.** El cliente son dos archivos sobre `fetch` y
`WebSocket` nativos; no hace falta Apollo ni urql. La subscription implementa a
mano el subprotocolo `graphql-transport-ws`, que es el que expone el
`GraphQlWebSocketHandler` de Spring: `connection_init` → `connection_ack` →
`subscribe`, y luego un mensaje `next` por cada movimiento.

**Qué demuestra la pestaña.** El panel (espacios libres, vehículos dentro con su
propietario, y propietarios con sus vehículos) se arma con **una sola petición**.
La misma vista contra REST necesita una llamada por lista más una por cada
relación, porque los DTOs REST solo traen `vehiculoId` y `espacioId`; en GraphQL
el servidor resuelve esas relaciones con `@BatchMapping`.

Registrar una entrada o una salida desde esa misma pestaña hace que el movimiento
aparezca en la tabla "en vivo" sin recargar: el backend lo emite después del
commit en PostgreSQL, el mismo evento que alimenta el tópico de Kafka. El
indicador de estado muestra si la conexión está activa, y si el backend se
reinicia la subscription se reabre sola.

> El sink del servidor es `directBestEffort`: lo ocurrido mientras la conexión
> estuvo caída no se reenvía. Para el pasado está la consulta de historial.

## Notas

- El backend no traía configuración CORS; se agregó `CorsConfig` (`backend/src/main/java/.../config/CorsConfig.java`)
  para permitir solicitudes desde `http://localhost:*` hacia `/api/**`. Para GraphQL
  el CORS se declara en `spring.graphql.cors` (`application.yml`), porque el
  handshake del WebSocket no pasa por el `WebMvcConfigurer`.
- Si al correr `./gradlew bootRun` aparece `Migration checksum mismatch for migration version 1`, es porque la
  base de datos local ya tiene aplicada una versión antigua del script de migración y el script en disco cambió.
  No se modificó la base de datos ni los scripts de migración como parte de este frontend; para destrabarlo hay
  que decidir (según corresponda al estado del trabajo en curso del backend) entre `flyway repair` o recrear el
  volumen de PostgreSQL local.
