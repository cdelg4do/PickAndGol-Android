# Android Client for PickAndGol

##### **- Antes de empezar:**
Se han omitido del repositorio la clave API para acceder a servicios de **Google Maps para Android** y el ID del Identity Pool para **Amazon AWS Mobile SDK**. Para poder compilar el proyecto correctamente, será necesario obtener estos valores desde sus respectivas consolas de desarrollador, e incluirlas en un nuevo fichero **app/src/main/res/values/api_keys.xml** con este formato:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="google_maps_api_key">CLAVE DE LA API AQUÍ</string>
	<string name="aws_identity_pool_id">ID DEL POOL ID AQUÍ</string>
</resources>
```

- Consola de desarrollador de Google: https://console.developers.google.com/apis/credentials
- Identidades federadas de Amazon Cognito: https://console.aws.amazon.com/cognito/federated

También se ha omitido del repositorio el fichero de configuración de servicios de Firebase (necesario para el funcionamiento de las notificaciones push). Dicho fichero puede descargarse desde la consola de Firebase y debe guardarse como **/app/google-services.json** en el proyecto.

- Consola de Firebase:
https://console.firebase.google.com/

'
##### **- Librerías utilizadas en el proyecto:**
- Gson (para parseo de documentos JSON)
- Volley (para la gestión de peticiones de red)
- Picasso (para la descarga y cacheado de imágenes)
- OkHttp3Downloader (gestor de red y caché de disco para Picasso)
- Butterknife (para el enlazado de vistas de la interfaz de usuario)
- Google Maps (para el visionado de mapas y elección de ubicaciones)
- CircleImageView (para las imágenes de perfil circulares)
- ArcNavigationView (para el Navigation Drawer curvo)
- Secure-preferences (para la encriptación del fichero local de preferencias)
- Amazon AWS Mobile SDK: Core + S3 (para el almacenamiento remoto de imágenes)
- Google Location and Activity Recognition (gestión de la ubicación del dispositivo)
- CircleIndicator (para el indicador circular de imágenes)
- Firebase: Core + Cloud Messaging (para el uso de notificaciones push)
