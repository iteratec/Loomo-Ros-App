## Rosjava Loomo App ##

## Disclaimer

This project is a prototype developed by iteratec's students and interns.
It is work in progress in many areas and does not reflect the usual code quality standards
applied by iteratec.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details

### Project Structure ###

The RosDeveloperAvtivity is the main entry point of the Project.
It initializes the EmojiView of Loomos API.
In the Background it starts the ROS System.
The Activity extends RosActivity and with the super Constructor the Master chooser can be started. When the constructor is given a master URI it will connect directly which is useful for faster app boot when developing.
The RosDeveloperActivity starts the several RosNodes with a NodeMainExecutor.
There are Native nodes which have empty native methods that get injected from .so files created with the ros_android_ndk.
And there is the LoomoRosBridgeNode that starts Loomos Publishers and Subscribers.
The ParameterNode is important to load the yaml files that will set the parameters and load the required plug-ins.

##Logging##
A custom Logger can be used for logging.
To disable the custom logger change the boolean "logging" in /res/values/strings.xml
The logger can ignore certain classes when their TAG is added as a String and a reference to it in the array "mutedTAGS"

### Requirements ###

This App will only work on a Loomo Segwayrobot as it uses its API to access its sensors.

## Getting Started

### Prerequisites
You will need :
a Loomo SegwayRobot
```
https://developer.segwayrobotics.com/developer/documents/segway-robots-sdk.html
```
ROS kinetic installed on your PC, you can follow this tutorial:
```
http://wiki.ros.org/ROS/Installation
```
a Raspberry Pi with some form of ROS compatible Ubuntu or Raspbian installed
and a Laserscanner, in this work a rplidar a1 by slamtec was used:
```
http://www.slamtec.com/en/lidar/a1
```

### Setting up the Raspberry Pi and the Laserscanner
Depending on what Ubuntu or Raspbian version you installed on the Raspberry Pi, the according steps for installing ROS Kinetic need to be taken. In this work Debian Stretch was used. If you chose to use it as well, you can find the installation guide for ROS here
```
http://wiki.ros.org/ROSberryPi/Installing%20ROS%20Kinetic%20on%20the%20Raspberry%20Pi
```
If you follow that guide, it can be useful to install the common_msgs package right away, as the rplidar rosnode uses it. Therefor it can be added to the installation in this line:
```
rosinstall_generator ros_comm common_msgs --rosdistro kinetic --deps --wet-only --tar > kinetic-custom_ros.rosinstall
```
Once you installed ROS, the RPLIDAR ROS package can be installed on the Raspberry Pi. The tutorial can be found here:
```
https://github.com/robopeak/rplidar_ros
```
It can be desirable to start the rplidar node from your PC via ssh, so you might want to enable it. Then you can control the Raspberry from your PC by typing:
```
ssh pi@<RaspberryPiIP>
```
the standard password is raspberry.
Lastly the ROS MASTER URI and the ROS IP need to be set in the ~/.bashrc file of the Raspberry Pi. Type
```
nano ~/.bashrc
```
then add at the end of the file:
```
export ROS_MASTER_URI=http://<MasterURI>:11311
export ROS_IP=<RaspberryPiIP>
```
 If you run the master on your PC, use your PC's IP address as MASTER URI. Additionally you can source the rplidar workspace below the ros workspace in your ~/.bashrc file so you don't need to do so via the terminal every time:
```
source /opt/ros/kinetic/setup.bash
source ~/rplidar/devel/setup.bash
```

### Setting up your PC
The SLAM will run your PC, so you need to install a few additional ROS packages:
```
sudo apt-get install ros-kinetic-move-base ros-kinetic-slam-gmapping ros-kinetic-map-server ros-kinetic-teleop-twist-keyboard
```
Then you can install the additional launch files for the SLAM,
start with creating a new catkin workspace
```
mkdir -p $yourWorkspace/src && cd $yourWorkspace
catkin_make
cd src
```
now add the loomo_laser folder into the src folder, it can be found here:
```
https://github.com/iteratec/Loomo-Ros-Slam-Launch
```
then run
```
cd ~/yourWorkspace
catkin_make
```
Then you can source your workspace in the ~/.bashrc file as well:
```
source /opt/ros/kinetic/setup.bash
source ~/<yourWorkspace>/devel/setup.bash
```
as well as add the ROS IP and HOSTNAME to the ~/.bashrc file:
```
export ROS_IP=<yourPCIP>
export ROS_HOSTNAME=<yourPCIP>
```
If your ROS MASTER does not run on your PC, you need to add the MASTER_URI as well
```
export ROS_MASTER_URI=http://<MasterURI>:11311
```
### Setting up Loomo
To let Loomo know about the ROS MASTER, add the MASTER_URI in the constructor of the RosDeveloperActivity in the Loomo App
```
super("RosDeveloperActivity", "RosDeveloperActivity", new URI("http://<MasterURI>:11311"));
```
To use the laser-scans for building a map or localization, the system needs to know the laser's position with respect to the robot's base frame. Therefore this transformation needs to be published by a static-transform-publisher. So you can measure the translation and rotation of your mounted laser-scanner with respect to the center of the robot on ground level. Then in the App in the TFPublisher in run() find the block that creates the transformation from the frame "BASE_POSE_FRAME" to the frame "laser". Add the orientation of your laser in radians in this line
```
org.apache.commons.math3.complex.Quaternion q_laser= toQuaternion(0,0,-PI/2);
```
The robot is facing the x direction. The lasers orientation can be found in its data sheet.
Then add the translation w.r.t base frame in the new Translation() part of
```
tfData.set(target_laser,source_laser,System.currentTimeMillis(),rotation_laser,new Translation(0.2f,0.2f,0),0);
```
In loomo-app/src/main/res/raw/laser_filter.yaml and loomo-app/src/main/res/raw/laser_filter_clearing.yaml an angle in radians is defined of what range of the laser to ignore so it doesn't scan the robot itself. If your scanner is positioned differently you will have to change that as well.


## Drawing a new map
Ensure that your PC, Loomo and the Raspberry Pi are all in the same wireless network. You can test this by pinging Loomo and the Raspberry Pi. You should also check that the time in all systems is synchronized, otherwise you will run into trouble with the transformations later on.

In the Loomo App you only need to run the LoomoRosBridge node and the ScanFilterNodes, so you can uncomment the other nodes in startNodes() in the RosService.

Start the ROS master on your PC by typing
```
roscore
```
in a terminal, then start the App on Loomo.
Connect to your Raspberry Pi via ssh
```
ssh pi@<RaspberryPiIP>
```
the standard password is raspberry.  Then launch the laser-scanner
```
roslaunch rplidar_ros rplidar.launch
```

Start the SLAM on your PC by launching
```
roslaunch loomo_laser slam_laser.launch
```
Now you should see your map in Rviz. To move Loomo around use the teleop keyboard in another terminal on your PC
```
rosrun teleop_twist_keyboard teleop_twist_keyboard.py
```
and slowly drive through the area you want to map.
When you are satisfied with your map, save it using the map_server in another terminal
```
rosrun map_server map_saver -f filename
```
If you aren't satisfied with the map you can tweak the settings of SLAM.

## Adding the new map and a new use-case to the project

### Editing your map and adding it to the project
The map you just created will be used by both the amcl node as well as the move_base node. The amcl node is used to localize your robot, so you should provide it with the original map.
The move_base node is used for path planning. So you might want to edit the map by adding black lines for areas the robot is not supposed to reach, e.g. to block off a staircase. Then mirror both maps and add them to the raw folder in the App.
Then in RosService in initialiseNavigation() add your maps and yaml in the MUC_OG case.
```
mapServerNode.publishMapforAmcl(R.raw.yourMap_yaml, R.raw.yourMap, map_publish_latch);
mapServerNode.publishMapforMb(R.raw.raw.yourMap_yaml, R.raw.yourMap_edited, map_publish_latch);
```

### Adding your initial position and goal position
Now you can follow the instructions in the 'Navigating with an existing map' section. Choose the MUC_OG location in the dropdown and then choose init_navigation.
This should open Rviz, load your new map and initialize the robot at some position.
To find the correct starting position for your use-case, try to guess it by using the '2D Pose Estimate' tool in Rviz. Then echo the pose estimate by using
```
rostopic echo /initialpose
```
and keep adjusting your guess until the laser scan fits correctly into the map. Then add this new position in the App as a new MapPosition.
Use the x and y entry from the pose position you echoed as well as the orientation. Take care to add the scalar component of the quaternion, so the w entry
from the echo, as first entry in the quaternion in the App. So the mapping is a->w b->x c->y d->z.
You can repeat the same process for your goal position.

### Adding a use-case
You can add your own use-case in Location. Create your own location by providing a greeting, your own initial position and at least two goal positions.
Then add your own location case in initialiseNavigation() in the RosService. Make sure to add your own maps for the mapServerNodes.
Now when you follow the 'navigating with an existing map' section and choose your new location. Your own map should be loaded and the robot's position should be initialized correctly.

## Navigating with an existing map
Ensure that your PC, Loomo and the Raspberry Pi are all in the same wireless network. You can test this by pinging Loomo and the Raspberry Pi. You should also check that the time in all systems is synchronized, otherwise you will run into trouble with the transformations later on.

Here the laserscanner is used for localization and for path planning, so obstacle avoidance.
Ensure all nodes are started in startNodes() in the RosService.
Start the ROS Master on your PC
```
roscore
```
Connect to your Raspberry Pi via ssh
```
ssh pi@<RaspberryPiIP>
```
the standard password is raspberry.  Then launch the laserscanner
```
roslaunch rplidar_ros rplidar.launch
```

and then the App on Loomo. Long click on Loomo's display and choose your location from the drop-down menu. Then click again and choose init_navigation.
Launch the rviz in a new terminal
```
roslaunch loomo_laser rviz_laserfilter.launch
```
Now you should see the map in Rviz and Loomo's position. You can send Loomo to a new goal by clicking on the '2D Nav Goal' button in Rviz and then on a point on the map.
If you want to stop Loomo you can type
```
rostopic pub /move_base/cancel actionlib_msgs/GoalID -- {}
```
Alternatively you can let Loomo follow your own use-case, if you added it. Then you can choose start_navigation from the drop-down menu and Loomo should start to drive to the first goal.




