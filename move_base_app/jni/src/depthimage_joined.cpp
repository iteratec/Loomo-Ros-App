
#include <depthimage_to_laserscan/DepthImageToLaserScanROS.h>
#include <nodelet/nodelet.h>

#include <jni.h>
#include <android/log.h>
#include <pluginlib/class_list_macros.h>

namespace depthimage_to_laserscan
{

class DepthImageToLaserScanNodelet : public nodelet::Nodelet
{
public:
  DepthImageToLaserScanNodelet()  {};

  ~DepthImageToLaserScanNodelet() {}

private:
  virtual void onInit()
  {
    dtl.reset(new DepthImageToLaserScanROS(getNodeHandle(), getPrivateNodeHandle()));
  };
  
  boost::shared_ptr<DepthImageToLaserScanROS> dtl;
};

}


PLUGINLIB_DECLARE_CLASS(depthimage_to_laserscan, DepthImageToLaserScanNodelet, depthimage_to_laserscan::DepthImageToLaserScanNodelet, nodelet::Nodelet);






using namespace depthimage_to_laserscan;
using std::string;

DepthImageToLaserScanROS::DepthImageToLaserScanROS(ros::NodeHandle& n, ros::NodeHandle& pnh):pnh_(pnh), it_(n), srv_(pnh) {
  boost::mutex::scoped_lock lock(connect_mutex_);
  
  // Dynamic Reconfigure
  dynamic_reconfigure::Server<depthimage_to_laserscan::DepthConfig>::CallbackType f;
  f = boost::bind(&DepthImageToLaserScanROS::reconfigureCb, this, _1, _2);
  srv_.setCallback(f);
  
  // Lazy subscription to depth image topic
  pub_ = n.advertise<sensor_msgs::LaserScan>("scan", 10, boost::bind(&DepthImageToLaserScanROS::connectCb, this, _1), boost::bind(&DepthImageToLaserScanROS::disconnectCb, this, _1));
}

DepthImageToLaserScanROS::~DepthImageToLaserScanROS(){
  sub_.shutdown();
}



void DepthImageToLaserScanROS::depthCb(const sensor_msgs::ImageConstPtr& depth_msg,
	      const sensor_msgs::CameraInfoConstPtr& info_msg){
  try
  {
    sensor_msgs::LaserScanPtr scan_msg = dtl_.convert_msg(depth_msg, info_msg);
    pub_.publish(scan_msg);
  }
  catch (std::runtime_error& e)
  {
    ROS_ERROR_THROTTLE(1.0, "Could not convert depth image to laserscan: %s", e.what());
  }
}

void DepthImageToLaserScanROS::connectCb(const ros::SingleSubscriberPublisher& pub) {
  boost::mutex::scoped_lock lock(connect_mutex_);
  if (!sub_ && pub_.getNumSubscribers() > 0) {
    ROS_DEBUG("Connecting to depth topic.");
    image_transport::TransportHints hints("raw", ros::TransportHints(), pnh_);
    sub_ = it_.subscribeCamera("image", 10, &DepthImageToLaserScanROS::depthCb, this, hints);
  }
}

void DepthImageToLaserScanROS::disconnectCb(const ros::SingleSubscriberPublisher& pub) {
  boost::mutex::scoped_lock lock(connect_mutex_);
  if (pub_.getNumSubscribers() == 0) {
    ROS_DEBUG("Unsubscribing from depth topic.");
    sub_.shutdown();
  }
}

void DepthImageToLaserScanROS::reconfigureCb(depthimage_to_laserscan::DepthConfig& config, uint32_t level){
    dtl_.set_scan_time(config.scan_time);
    dtl_.set_range_limits(config.range_min, config.range_max);
    dtl_.set_scan_height(config.scan_height);
    dtl_.set_output_frame(config.output_frame_id);
}

DepthImageToLaserScanROS::DepthImageToLaserScanROS(ros::NodeHandle& n, ros::NodeHandle& pnh):pnh_(pnh), it_(n), srv_(pnh) {
  boost::mutex::scoped_lock lock(connect_mutex_);
  
  // Dynamic Reconfigure
  dynamic_reconfigure::Server<depthimage_to_laserscan::DepthConfig>::CallbackType f;
  f = boost::bind(&DepthImageToLaserScanROS::reconfigureCb, this, _1, _2);
  srv_.setCallback(f);
  
  // Lazy subscription to depth image topic
  pub_ = n.advertise<sensor_msgs::LaserScan>("scan", 10, boost::bind(&DepthImageToLaserScanROS::connectCb, this, _1), boost::bind(&DepthImageToLaserScanROS::disconnectCb, this, _1));
}

DepthImageToLaserScanROS::~DepthImageToLaserScanROS(){
  sub_.shutdown();
}



void DepthImageToLaserScanROS::depthCb(const sensor_msgs::ImageConstPtr& depth_msg,
        const sensor_msgs::CameraInfoConstPtr& info_msg){
  try
  {
    sensor_msgs::LaserScanPtr scan_msg = dtl_.convert_msg(depth_msg, info_msg);
    pub_.publish(scan_msg);
  }
  catch (std::runtime_error& e)
  {
    ROS_ERROR_THROTTLE(1.0, "Could not convert depth image to laserscan: %s", e.what());
  }
}

void DepthImageToLaserScanROS::connectCb(const ros::SingleSubscriberPublisher& pub) {
  boost::mutex::scoped_lock lock(connect_mutex_);
  if (!sub_ && pub_.getNumSubscribers() > 0) {
    ROS_DEBUG("Connecting to depth topic.");
    image_transport::TransportHints hints("raw", ros::TransportHints(), pnh_);
    sub_ = it_.subscribeCamera("image", 10, &DepthImageToLaserScanROS::depthCb, this, hints);
  }
}

void DepthImageToLaserScanROS::disconnectCb(const ros::SingleSubscriberPublisher& pub) {
  boost::mutex::scoped_lock lock(connect_mutex_);
  if (pub_.getNumSubscribers() == 0) {
    ROS_DEBUG("Unsubscribing from depth topic.");
    sub_.shutdown();
  }
}

void DepthImageToLaserScanROS::reconfigureCb(depthimage_to_laserscan::DepthConfig& config, uint32_t level){
    dtl_.set_scan_time(config.scan_time);
    dtl_.set_range_limits(config.range_min, config.range_max);
    dtl_.set_scan_height(config.scan_height);
    dtl_.set_output_frame(config.output_frame_id);
}


int main(int argc, char **argv){
  ros::init(argc, argv, "depthimage_to_laserscan");
  ros::NodeHandle n;
  ros::NodeHandle pnh("~");
  
  DepthImageToLaserScanROS dtl(n, pnh);
  
  ros::spin();

  return 0;
}

inline string stdStringFromjString(JNIEnv *env, jstring java_string) {
    const char *tmp = env->GetStringUTFChars(java_string, NULL);
    string out(tmp);
    env->ReleaseStringUTFChars(java_string, tmp);
    return out;
}

void log(const char *msg, ...) {
    va_list args;
    va_start(args, msg);
    __android_log_vprint(ANDROID_LOG_INFO, "MOVE_BASE_NDK_EXAMPLE", msg, args);
    va_end(args);
}


extern "C"
JNIEXPORT jint JNICALL
Java_de_iteratec_loomo_ros_nodes_DepthimageToLaserScanNode_execute(
        JNIEnv *env,
        jobject /* this */, jstring rosMasterUri, jstring rosHostname, jstring rosNodeName,
        jobjectArray remappingArguments) {

    string master("__master:=" + stdStringFromjString(env, rosMasterUri));
    string hostname("__ip:=" + stdStringFromjString(env, rosHostname));
    string node_name(stdStringFromjString(env, rosNodeName));
    jsize len = env->GetArrayLength(remappingArguments);
    int argc = 0;
    const int static_params = 4;
    std::string ni = "move_base_jni";
    char **argv = new char *[static_params + len];
    argv[argc++] = const_cast<char *>(ni.c_str());
    argv[argc++] = const_cast<char *>(master.c_str());
    argv[argc++] = const_cast<char *>(hostname.c_str());
        char **refs = new char *[len];
    for (int i = 0; i < len; i++) {
        refs[i] = (char *) env->GetStringUTFChars(
                (jstring) env->GetObjectArrayElement(remappingArguments, i), NULL);
        argv[argc] = refs[i];
        argc++;
    }
 
  return 0;
}

extern "C"
JNIEXPORT jint JNICALL 
Java_de_iteratec_loomo_ros_nodes_DepthimageToLaserScanNode_shutdown
        (JNIEnv *, jobject) {
    log("Shutting down native node.");
    ros::shutdown();

    return 0;
}
