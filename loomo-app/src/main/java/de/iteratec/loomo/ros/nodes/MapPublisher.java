/*
 * Copyright 2017 Ekumen, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.iteratec.loomo.ros.nodes;

import android.util.Log;
import de.iteratec.loomo.location.Location;
import de.iteratec.loomo.location.LocationService;
import de.iteratec.loomo.ros.map.OccupancyGridGenerator;
import java.util.concurrent.CountDownLatch;
import nav_msgs.OccupancyGrid;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

/**
 * Publishes an {@link OccupancyGrid} created by a {@link OccupancyGridGenerator}.
 */
public class MapPublisher extends AbstractNodeMain {
    public static final String NODE_NAME = "map_server";
    private Publisher<OccupancyGrid> mbPublisher;
    private Publisher<OccupancyGrid> amclPublisher;
    private OccupancyGridGenerator mGridGenerator;
    private String amclTopicName;
    private String moveBaseTopicName;
    private String nodeName;

    public MapPublisher(OccupancyGridGenerator generator, String[] remappingArguments) {
        mGridGenerator = generator;
        this.amclTopicName = remappingArguments[0].split(":=")[1];
        this.moveBaseTopicName = remappingArguments[1].split(":=")[1];
    }

    public MapPublisher(OccupancyGridGenerator generator) {
        mGridGenerator = generator;
        this.amclTopicName="amcl_map";
        this.moveBaseTopicName="mb_map";

    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of(NODE_NAME);
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        super.onStart(connectedNode);
        amclPublisher = connectedNode.newPublisher(amclTopicName, OccupancyGrid._TYPE);
        amclPublisher.setLatchMode(true);

        mbPublisher = connectedNode.newPublisher(moveBaseTopicName, OccupancyGrid._TYPE);
        mbPublisher.setLatchMode(true);


    }

    public void publishMapforAmcl(CountDownLatch map_publish_latch){
        Location location= LocationService.getInstance().getLocation();
        publishMapforAmcl(location.getMapinfo(),location.getMapforamcl(),map_publish_latch);
    }

    public void publishMapforMb(CountDownLatch map_publish_latch){
        Location location= LocationService.getInstance().getLocation();
        publishMapforMb(location.getMapinfo(),location.getMapformb(),map_publish_latch);
    }

    public void publishMapforAmcl(int infoResource, int mapResource, CountDownLatch map_publish_latch) {
        Log.d(NODE_NAME, "publishing map for amcl");

        OccupancyGrid message = amclPublisher.newMessage();
        mGridGenerator.fillHeader(message.getHeader());
        mGridGenerator.fillInformation(message.getInfo(),infoResource,mapResource);
        message.setData(mGridGenerator.generateData());

        amclPublisher.publish(message);
        map_publish_latch.countDown();
    }
    public void publishMapforMb(int infoResource, int mapResource, CountDownLatch map_publish_latch) {
                Log.d(NODE_NAME, "publishing map for mb");
                OccupancyGrid message = mbPublisher.newMessage();
                mGridGenerator.fillHeader(message.getHeader());
                mGridGenerator.fillInformation(message.getInfo(),infoResource,mapResource);
                message.setData(mGridGenerator.generateData());

                mbPublisher.publish(message);
                map_publish_latch.countDown();
    }
}