# src Package

This is the README file for the `src` package, which provides comprehensive Java code and layout XML files for an Android application built for Aquaterra.

## Installation

A .apk file is provided to install the application to a device using an Android system (require a system to be above Android 8.0)

## Package Structure

```
├── src/                                     # source package   
    ├── main/  
        ├── java/com/example/aq_bluering/    # JAVA code  
            ├── Connection/                 
                ├── ApiConnection.java       #Object to establish a connection with AWS API  
                ├── JsonParser.java          #Object to parse response message from API  
                ├── RequestConstructor.java  #Object to generate request json message to API    
                └── WeatherApi.java          #Object to establish a connection with weather API  
            ├── ui/  
                ├── Dashboard/  
                    ├── Chart/  
                        ├── Chart.java               #Object to create the dashboard-chart view
                        ├── ChartInfoWindow.java     #Object to display information window for chart when click on each node     
                        └── ChartViewModel.java      #Object to support the creation of chart view  
                    ├── Detail/  
                        ├── Detail.java              #Object to create the dashboard-detail view  
                        ├── DetailViewModel.java     #Object to support the creation of detail view  
                        ├── LocationData.java        #Object to obtain location  
                        └── WeatherFragment.java     #Object to create weather forecast screen  
                    ├── Map/  
                        ├── Map.java                 #Object to create dashboard-map view  
                        └── MapViewModel.java        #Object to support the creation of map view  
                    └── Dashboard.java               #Object to create basic layout of dashboard pages  
                ├── FarmField/  
                    ├── FarmField.java              #Object to create farm&field management screen  
                    ├── FarmFieldCreation.java      #Object to create farm&field creation screen  
                    ├── FarmFieldViewModel.java     #Object to support the creation of farm&field management screen  
                    ├── FieldListAdapter.java       #Object to manage field detail list (text display and buttons' function)  
                    └── SensorInstallation.java     #Object to create sensor installation screen  
                ├── Gateway/
                    ├── Gateway.java                #Object to create gateway management screen  
                    ├── GatewayCreation.java        #Object to create gateway creation screen  
                    ├── GatewayListAdapter.java     #Object to manage gateway detail list (text display and buttons' function)  
                    └── GatewayViewModel.java       #Object to support the creation of gateway management screen  
                ├── Irrigation/  
                    ├── Irrigation.java             #Object to create irrigation zone management screen  
                    ├── IrrigationListAdapter.java  #Object to manage irrigation zone detail list (text display and buttons' function)  
                    ├── IrrigationViewModel.java    #Object to support the creation of irrigation zone management screen  
                    ├── ZoneCreation.java           #Object to create irrigation zone creation screen  
                    └── ZoneEditing.java            #Object to create irrigation zone editing screen  
                ├── Profile/  
                    ├── Profile.java                #Object to create profile screen  
                ├── Sensor/  
                    ├── Sensor.java                 #Object to create sensor management screen  
                    ├── SensorCreation.java         #Object to create sensor creation screen  
                    ├── SensorEditing.java          #Object to create sensor editing screen   
                    ├── SensorListAdapter.java      #Object to manage sensor detail list (text display and buttons' function)  
                    └── SensorViewModel.java        #Object to support the creation of sensor management screen
                └── DeleteComfirmation.java         #Object to display a comfirmation window before delete
            ├── LoginActivity.java          #Acitivity to configure Amplify and performe login authentication    
            ├── MainActivity.java           #Main activity for application, set navigation to each screen
            ├── SplashActivity.java         #Application entry point, show animation when app launch      
            └── UsernameShare.java          #Object to pass username parameter from authentication result to each module  
        ├── res/                            # XML files  
        └── AndroidManifest                # Application environment/parameter setting
```
               
      
