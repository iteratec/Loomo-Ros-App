#include <stdarg.h>
#include <stdio.h>
#include <sstream>
#include <map>
#include <string.h>
#include <errno.h>
#include <vector>
#include <set>
#include <fstream>
#include <android/log.h>
// %Tag(FULL_TEXT)%

// %Tag(ROS_HEADER)%
#include "ros/ros.h"
#include <std_msgs/String.h>
// %EndTag(ROS_HEADER)%

// %Tag(ANDROID_NATIVE_HEADER)%
#include <android_native_app_glue.h>
// %EndTag(ANDROID_NATIVE_HEADER)%
#include <move_base/move_base.h>
#include <jni.h>

using std::string;
int loop_count_ = 0;

inline string stdStringFromjString(JNIEnv *env, jstring java_string) {
    const char *tmp = env->GetStringUTFChars(java_string, NULL);
    string out(tmp);
    env->ReleaseStringUTFChars(java_string, tmp);
    return out;
}

void log(const char *msg, ...) {
    va_list args;
    va_start(args, msg);
    __android_log_vprint(ANDROID_LOG_INFO, "ROSCPP_NDK_EXAMPLE", msg, args);
    va_end(args);
}


// from android samples
/* return current time in seconds */
static double now(void) {

  struct timespec res;
  clock_gettime(CLOCK_REALTIME, &res);
  return res.tv_sec + (double) res.tv_nsec / 1e9;

}

#define LASTERR strerror(errno)

// %EndTag(CHATTER_CALLBACK)%

// %Tag(MAIN)%
extern "C"
JNIEXPORT jint JNICALL
Java_de_iteratec_loomo_ros_nodes_MoveBaseNode_execute(
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

    ros::init(argc, &argv[0], node_name.c_str());
        for (int i = 0; i < len; i++) {
        env->ReleaseStringUTFChars((jstring) env->GetObjectArrayElement(remappingArguments, i),
                                   refs[i]);
    }
    delete refs;
    delete argv;

    std::string master_uri = ros::master::getURI();

    if(ros::master::check()){
        log("ROS MASTER IS UP!");
    } else {
        log("NO ROS MASTER.");
    }
    log(master_uri.c_str());

    ros::NodeHandle n;

    tf::TransformListener tf(ros::Duration(10));
    move_base::MoveBase move_base(tf);

    ros::WallRate loop_rate(100);
    while(ros::ok()){
        ros::spinOnce();
        loop_rate.sleep();
    }
}

extern "C"
JNIEXPORT jint JNICALL 
Java_de_iteratec_loomo_ros_nodes_MoveBaseNode_shutdown
        (JNIEnv *, jobject) {
    log("Shutting down native node.");
    ros::shutdown();

    return 0;
}


void android_main(android_app *papp) {
    // Make sure glue isn't stripped
    app_dummy();


    // %EndTag(ROS_SPIN)%
}

// %EndTag(MAIN)%
// %EndTag(FULL_TEXT)%
