/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.formats.protobuf;

import org.apache.flink.formats.protobuf.testproto.MapTest;
import org.apache.flink.table.data.GenericMapData;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RowToMapProtoBytesTest {
    @Test
    public void testSimple() throws Exception {
        Map map1 = new HashMap();
        map1.put(StringData.fromString("a"), StringData.fromString("b"));
        Map map2 = new HashMap();
        map2.put(StringData.fromString("c"), GenericRowData.of(1, 2L));
        RowData row = GenericRowData.of(1, new GenericMapData(map1), new GenericMapData(map2));

        byte[] bytes = ProtobufTestHelper.rowToPbBytes(row, MapTest.class);

        MapTest mapTest = MapTest.parseFrom(bytes);
        assertEquals(1, mapTest.getA());
        assertEquals("b", mapTest.getMap1Map().get("a"));
        MapTest.InnerMessageTest innerMessageTest = mapTest.getMap2Map().get("c");
        assertEquals(1, innerMessageTest.getA());
        assertEquals(2L, innerMessageTest.getB());
    }

    @Test
    public void testNull() throws Exception {
        RowData row = GenericRowData.of(1, null, null);

        byte[] bytes = ProtobufTestHelper.rowToPbBytes(row, MapTest.class);
        MapTest mapTest = MapTest.parseFrom(bytes);
        Map<String, String> map = mapTest.getMap1Map();
        assertEquals(0, map.size());
    }
}
