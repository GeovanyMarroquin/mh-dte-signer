# El Salvador DTE Signer

Servicio de prueba para generar firmas **JWS Compact RS512** sobre documentos
DTE de El Salvador. Implementado con **Java 25**, **Spring Boot 4.1.1**, **Gradle** y **JJWT 0.13.0**.

> [!WARNING]
> Este proyecto existe con fines de desarrollo, integración y pruebas. No es
> una solución lista para producción ni sustituye los mecanismos oficiales del
> Ministerio de Hacienda. Los certificados generados por este repositorio son
> simulados y las claves privadas de prueba no deben utilizarse con documentos
> reales.

## Para qué sirve

El servicio permite probar integraciones que necesitan llamar al contrato de
firma de DTE sin depender de un certificado productivo. Para ello:

- Carga certificados `.crt` simulados con formato compatible con MH.
- Valida `passwordPri` mediante SHA-512.
- Firma el campo `dteJson` con una clave RSA y JWS `RS512`.
- Mantiene los nombres de campos y las respuestas esperadas por el sistema
  que se está simulando.

## Requisitos

Para ejecutar el proyecto localmente:

- Docker y Docker Compose, recomendado para una puesta en marcha rápida.
- O Java 25 y el wrapper de Gradle incluido (`./gradlew`).
- Python 3 y `cryptography` solo si se ejecuta el generador de certificados
  fuera de Docker.

## Ejecutar con Docker

1. Clona el repositorio y entra en su directorio:

   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd mh-dte-signer
   ```

2. Crea el archivo local de configuración:

   ```bash
   cp .env.example .env
   ```

3. Construye e inicia el servicio:

   ```bash
   docker compose up --build -d
   ```

4. Genera un certificado de prueba dentro del contenedor:

   ```bash
   docker compose exec dte-signer python3 scripts/generate-test-certificate.py --nit 061400000001 --password test123
   ```

5. Envía un documento de prueba para firmar:

   ```bash
   curl -X POST http://localhost:8113/firmardocumento \
     -H 'Content-Type: application/json' \
     -d '{
       "passwordPri": "test123",
       "nit": "061400000001",
       "dteJson": {
         "identificacion": {
           "version": 1,
           "ambiente": "00",
           "tipoDte": "01",
           "numeroControl": "DTE-01-00000000-000000000000001",
           "codigoGeneracion": "00000000-0000-0000-0000-000000000001"
         },
         "cuerpoDocumento": []
       }
     }'
   ```

   Una operación correcta devuelve `200` y un JWS en `body`:

   ```json
   {
     "status": "OK",
     "body": "<jws-compact>"
   }
   ```

6. Detén el servicio cuando termines:

   ```bash
   docker compose down
   ```

Los certificados generados con este flujo se guardan dentro del contenedor y
se eliminan al eliminarlo. Esto evita dejar claves de prueba en el equipo host.
Para conservar certificados únicamente durante una sesión, usa
`docker compose stop` en lugar de `docker compose down`.

## Ejecutar sin Docker

Configura el entorno y genera un certificado local:

```bash
cp .env.example .env
python3 -m pip install cryptography
python3 scripts/generate-test-certificate.py \
  --nit 061400000001 \
  --password test123
./gradlew bootRun
```

Por defecto, el servicio escucha en `http://localhost:8113`.

## Pruebas automatizadas

Ejecuta la suite con:

```bash
./gradlew test
```

También puedes construir la imagen Docker, que compila el proyecto y genera el
JAR de la aplicación durante el build:

```bash
docker compose build
```

## API

### `POST /firmardocumento`

También acepta `/firmardocumento/`. El cuerpo esperado es:

```json
{
  "passwordPri": "test123",
  "nit": "061400000001",
  "dteJson": {}
}
```

El archivo de certificado debe llamarse `{nit}.crt` y encontrarse en el
directorio configurado. La implementación normaliza NIT con guiones, pero el
archivo se busca usando el NIT sin guiones.

Respuestas principales:

- `200`: firma generada correctamente.
- `400`: JSON malformado o datos requeridos ausentes.
- `500`: certificado, contraseña o clave privada inválidos, entre otros errores
  del proceso de firma.

## Configuración

La configuración puede definirse en `.env` o mediante variables de entorno:

| Variable                |  Valor por defecto | Descripción                        |
|-------------------------|-------------------:|------------------------------------|
| `SERVER_PORT`           |             `8113` | Puerto HTTP del servicio.          |
| `CERTIFICATE_DIRECTORY` | `./uploads/certs/` | Directorio de certificados `.crt`. |

En Docker Compose, el directorio de certificados es
`/app/uploads/certs/`. No subas `.env`, certificados ni claves privadas al
repositorio.

## Compatibilidad de firma

- La contraseña se comprueba como `hex(SHA-512(passwordPri)) == privateKey.clave`.
- `privateKey.embodied` contiene una clave RSA PKCS#8 DER codificada en Base64.
- El payload firmado es `dteJson` serializado con claves ordenadas.
- El encabezado JWS es `{"alg":"RS512"}`.
- El formato XML del certificado y los nombres de los campos conservan
  compatibilidad con el sistema simulado.

## Estructura del proyecto

- `src/main/.../controller/`: endpoints HTTP.
- `src/main/.../service/`: carga de certificados, validación y firma.
- `src/test/`: pruebas automatizadas.
- `scripts/generate-test-certificate.py`: generador de certificados simulados.
- `Dockerfile` y `docker-compose.yml`: ejecución reproducible con Docker.

## Contribuciones

Las mejoras orientadas a pruebas, documentación, compatibilidad y seguridad son
bienvenidas. Antes de abrir un pull request, ejecuta `./gradlew test` y evita
incluir certificados, contraseñas o claves privadas reales.
