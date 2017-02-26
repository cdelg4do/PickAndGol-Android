# Android Client for PickAndGol

#### **- Antes de empezar:**
Se han omitido del repositorio la clave API para acceder a servicios de **Google Maps para Android** y el ID del Identity Pool para **Amazon AWS Mobile SDK**. Para poder compilar el proyecto correctamente, será necesario obtener estos valores desde sus respectivas consolas de desarrollador, e incluirlas en un nuevo fichero **/res/values/api_keys.xml** con este formato:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="google_maps_api_key">CLAVE DE LA API AQUÍ</string>
	<string name="aws_identity_pool_id">ID DEL POOL ID AQUÍ</string>
</resources>
```

- Consola de desarrollador de Google: https://console.developers.google.com/apis/credentials
- Identidades federadas de Amazon Cognito: https://console.aws.amazon.com/cognito/federated
