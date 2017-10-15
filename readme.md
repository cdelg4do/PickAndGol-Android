<img alt="App logo" src="https://avatars0.githubusercontent.com/u/25353537?s=70"/>

# Android Client for PickAndGol

This is the Android client for the **PickAndGol** platform, a pet project I am working on along a small group of fellow developers. PickAndGol is also available on iOS devices and has a web version, at **www.pickandgol.com**.

#### **- Notes:**
- This repository is just a 'screenshot' of the original working repository, made to show visitors some of my progress on this project. The development is still in progress so this repo does not necessarily represent the last updated version.

- For security and privacy reasons, the code in this repo does not contain some private API keys, necessary to fully compile the project. In order to try the app on your device, you can still do it by generating your own keys (see the **"Missing project files"** section below), or by using <a href="http://bit.ly/pickandgol_r20171010"> **THIS** </a> installer package.

### **Description**
PickAndGol is a social app that enables people to find where their favorite sport events are broadcasted in their nearby. You can search events by name, category or location, and also add new ones to the system so that they can be checked by other users.

Every event in the system is tied to one or more pubs where it is being broadcasted, so you can also search by pubs and see what events are coming on each pub. New pubs can be added as well, and you can keep a favorite list in order to receive notifications when new events are added on some of your favorite pubs.

Future features include: integration with some major social network so that you can tell all your contacts you are attending to an event at some pub, and a review system where users can publish and evaluate the free bites of food served on the pub (typically Spanish!) during the event broadcasts.

### **Screenshots**

&nbsp;
<kbd> <img alt="screenshot 1" src="https://user-images.githubusercontent.com/18370149/31582196-bcb28b6a-b17d-11e7-8b9d-d0fbb46f9923.jpg" width="256"> </kbd> &nbsp; <kbd> <img alt="screenshot 2" src="https://user-images.githubusercontent.com/18370149/31582197-bccddc08-b17d-11e7-9f84-d2b27a4e6e5b.jpg" width="256"> </kbd> &nbsp; <kbd> <img alt="screenshot 3" src="https://user-images.githubusercontent.com/18370149/31582198-bce770d2-b17d-11e7-8831-7041adeed65d.jpg" width="256"> </kbd>

&nbsp;
<kbd> <img alt="screenshot 4" src="https://user-images.githubusercontent.com/18370149/31582199-bcfff850-b17d-11e7-8770-f6950d1d594a.jpg" width="256"> </kbd> &nbsp; <kbd> <img alt="screenshot 5" src="https://user-images.githubusercontent.com/18370149/31582200-bd1923ca-b17d-11e7-9dac-8c3a3e56600e.jpg" width="256"> </kbd> &nbsp; <kbd> <img alt="screenshot 6" src="https://user-images.githubusercontent.com/18370149/31582201-bd3282ca-b17d-11e7-8da5-1c2f84e69870.jpg" width="256"> </kbd>

&nbsp;
<kbd> <img alt="screenshot 7" src="https://user-images.githubusercontent.com/18370149/31582202-bd4b4ed6-b17d-11e7-901d-980203385e93.jpg" width="256"> </kbd> &nbsp; <kbd> <img alt="screenshot 8" src="https://user-images.githubusercontent.com/18370149/31582203-bd6412f4-b17d-11e7-9960-2aae473e33f2.jpg" width="256"> </kbd> &nbsp; <kbd> <img alt="screenshot 9" src="https://user-images.githubusercontent.com/18370149/31582204-bd7cb9a8-b17d-11e7-8e80-28cc363fa2cb.jpg" width="256"> </kbd>

&nbsp;
<kbd> <img alt="screenshot 10" src="https://user-images.githubusercontent.com/18370149/31582205-bd964c60-b17d-11e7-9954-9a413a8fe10a.jpg" width="256"> </kbd> &nbsp; <kbd> <img alt="screenshot 11" src="https://user-images.githubusercontent.com/18370149/31582206-bdb657e4-b17d-11e7-91f0-6213d4b09147.jpg" width="256"> </kbd> &nbsp; <kbd> <img alt="screenshot 12" src="https://user-images.githubusercontent.com/18370149/31582207-bdcecfe0-b17d-11e7-8029-0aa592e234d9.jpg" width="256"> </kbd>

### **Architectural considerations**
The app has been designed following the **SOLID** principles and implementing a **Clean Architecture**, organizing it in several layers where the code dependencies point inwards only, and the inner layers do not know anything about the outer layers. This way a separation of concerns (presentation, businnes rules, data persistence, etc) is achieved while elements in each level can be tested relying on the tests of the previous level.

Also, each layer uses its own data model so that they are independent of the specific implementation used in the other layers, as long as the interfaces between them remain unchanged. This specially afects the app database manager, for instance, whose implementation could be easily replaced from Realm to another engine without affecting the rest of the application.

You can find additional information about Clean Architectures <a href="https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html">**here**</a>.

### **External libraries used by the Android client**
The following third-party libraries/frameworks are used in this project:
- Realm (to be used as local database manager for cached data)
- Gson (to easily parse JSON responses from the backend)
- Volley (to manage network requests)
- Picasso (for download and caching of images)
- OkHttp3Downloader (used as network manager and disk cache for Picasso)
- Butterknife (to easily manage view dependencies)
- Google Maps (for showing maps on screen and choosing locations)
- CircleImageView (for the circular profile images)
- ArcNavigationView (to show the curved navigation drawer)
- Secure-preferences (to store a locally encrypted preferences file)
- Amazon AWS Mobile SDK: Core + S3 (to upload images to the Amazon cloud)
- Google Location and Activity Recognition (to manage the device loction)
- CircleIndicator (a lightweight viewpager indicator)
- Firebase: Core + Cloud Messaging (to support push notifications)

### **Missing project files**

The repository does not include the API key to use the **Google Maps for Android** services and the Identity Pool Id for the **Amazon AWS Mobile SDK**. In order to compile the project, it is mandatory to obtain (or generate) these values from their respective admin/developer consoles. Then, they must be added to a file **/res/values/api_keys.xml** with the following content:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="google_maps_api_key">YOUR_API_KEY_HERE</string>
	<string name="aws_identity_pool_id">YOUR_POOL_ID_HERE</string>
</resources>
```

- Google developer console: https://console.developers.google.com/apis/credentials
- Amazon Cognito federated identities: https://console.aws.amazon.com/cognito/federated

Also, the settings file for Firebase services is not included in the repository. This file is necessary in order to have the push notifications running, and must be downloaded from the Firebase console and stored as **/app/google-services.json** in the project.

- Firebase console:
https://console.firebase.google.com/
