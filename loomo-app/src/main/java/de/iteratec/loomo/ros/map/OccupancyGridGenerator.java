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

package de.iteratec.loomo.ros.map;

import nav_msgs.MapMetaData;

import org.jboss.netty.buffer.ChannelBuffer;
import std_msgs.Header;

public interface OccupancyGridGenerator {
    void fillHeader(Header header);

    void fillInformation(MapMetaData information, int infoResource, int mapResource);

    ChannelBuffer generateData();

    public static final String PGM = "pgm";
    public static final String BMP = "bmp";
}