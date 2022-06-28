# Smart Park - mobile android application

📚 "Mobile Development" project (Artificial Intelligence, UniGe)

The Smart Park application has the purpose to store the current gps location when parking the car. 
It can do it in 3 different ways:
- Manually:
  - Saves the current gps location clicking a button in the home.
- Automatic: (If the car restart repeat the procedure from the beginning)
  - With Bluetooth:
    - Saves in a temporary variable the current gps location every times that the car stops.
    - Checks the bluetooth’s name and if it’s still connected to the car.
    - If it’s unconnected it saves permanently the temporary position (If you get too far from the car the bluetooth will automatically unlink).
  - Without Bluetooth:
    - Saves in a temporary variable the current gps location every times that the car stops.
    - Using the accelerometer of the smartphone it checks if the speed of the car is under a certain value (for example walking) for a certain amount of time (for example 5 
  minutes).
    - The app also checks that the count of steps is higher than a certain limit (for example 15 steps, it means that the user gets away from the car).
    - Saves the temporary position permanently.

Other features:
- In the homepage is displayed the gps position through the coordinates and a map.
- The user can add informations to the gps position like a picture and some notes.
- The user can clear the saved gps position and the correlated informations.
