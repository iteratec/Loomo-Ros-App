#include <iostream>
#include <depthimage_to_laserscan/DepthConfig.h>

using namespace std;


class DepthFilteredConfig: public DepthConfig{
	public:
	int filter;
	int kernel_size;


	
};

