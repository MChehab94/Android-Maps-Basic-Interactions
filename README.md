This is a simple demo that shows how to handle the following basic interactions using Google Maps:
* Changing map type
* Animating to user's location
* Handling map click listener
* Handling marker click listener
* Dragging a marker

The code from the [LocationHandler](https://github.com/MChehab94/Android-Get-User-Location) repo is used to easily retrieve the location (including the entire flow and proper handling)  
There are two modules:  
* app: Java implementation
* kotlin: Kotlin implementation

A thorough explanation can be found in the post [here](http://mobiledevhub.com/2018/11/14/android-how-to-interact-with-google-maps/).  
NOTE: In order to use this code, you need to create an API key for Google Maps from https://console.developers.google.com. 
Once you generate the key, you need to add it in **google_maps_api_key.xml** located **res -> values**
