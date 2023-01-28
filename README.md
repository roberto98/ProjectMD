# Smart Park - mobile android application

📚 "Mobile Development" project (Artificial Intelligence, UniGe)

The Smart Park application has the purpose to store the current gps location your parking spots. 
It can do it in 3 different ways:
- Manually:
  - Saves the current gps location clicking a button in the home.
- Automatic:
  - With Bluetooth:
    - Checks the bluetooth’s name and if it’s still connected to the car.
    - If it’s unconnected it saves the GPS position (If you get too far from the car the bluetooth will automatically unlink).
  - Without Bluetooth: (not implemented yet)
    - Saves in a temporary variable the current gps location every times that the car stops.
    - Using the accelerometer of the smartphone it checks if the speed of the car is under a certain value (for example walking) for a certain amount of time (for example 5  minutes).
    - The app also checks that the count of steps is higher than a certain limit (for example 15 steps, it means that the user gets away from the car).
    - Saves the temporary position permanently.

Other features:
- In the homepage is displayed the gps position through the coordinates and a GoogleMap.
- The user can add informations to the gps position like a picture and some notes.
- The user can clear the saved gps position and the correlated informations.

## Report of the project
Here you can download the [SmartPark_report.pdf](https://github.com/roberto98/SmartPark-Android-application/files/10527462/SmartPark_report.pdf) where there is explained how I implented the app and the possible future updates. Here also some screens of the current app.

![image](https://user-images.githubusercontent.com/32781888/215271893-9345aa63-83af-4c93-af16-e44b9ab8c605.png)
