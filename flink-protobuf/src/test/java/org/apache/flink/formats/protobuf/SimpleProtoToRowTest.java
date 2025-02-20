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

import org.apache.flink.formats.protobuf.deserialize.PbRowDataDeserializationSchema;
import org.apache.flink.formats.protobuf.testproto.SimpleTest;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.runtime.typeutils.InternalTypeInfo;
import org.apache.flink.table.types.logical.RowType;

import com.google.protobuf.ByteString;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SimpleProtoToRowTest {
    @Test
    public void testSimple() throws Exception {
        RowType rowType = PbRowTypeInformation.generateRowType(SimpleTest.getDescriptor());
        PbRowDataDeserializationSchema deserializationSchema =
                new PbRowDataDeserializationSchema(
                        rowType,
                        InternalTypeInfo.of(rowType),
                        SimpleTest.class.getName(),
                        false,
                        false);

        SimpleTest simple =
                SimpleTest.newBuilder()
                        .setA(1)
                        .setB(2L)
                        .setC(false)
                        .setD(0.1f)
                        .setE(0.01)
                        .setF("haha")
                        .setG(ByteString.copyFrom(new byte[] {1}))
                        .setH(SimpleTest.Corpus.IMAGES)
                        .setFAbc7D(1) // test fieldNameToJsonName
                        .build();

        RowData row = deserializationSchema.deserialize(simple.toByteArray());
        row =
                ProtobufTestHelper.validateRow(
                        row, PbRowTypeInformation.generateRowType(SimpleTest.getDescriptor()));

        assertEquals(9, row.getArity());
        assertEquals(1, row.getInt(0));
        assertEquals(2L, row.getLong(1));
        assertFalse((boolean) row.getBoolean(2));
        assertEquals(Float.valueOf(0.1f), Float.valueOf(row.getFloat(3)));
        assertEquals(Double.valueOf(0.01d), Double.valueOf(row.getDouble(4)));
        assertEquals("haha", row.getString(5).toString());
        assertEquals(1, (row.getBinary(6))[0]);
        assertEquals("IMAGES", row.getString(7).toString());
        assertEquals(1, row.getInt(8));
    }

    @Test
    public void testNotExistsValueIgnoringDefault() throws Exception {
        RowType rowType = PbRowTypeInformation.generateRowType(SimpleTest.getDescriptor());
        PbRowDataDeserializationSchema deserializationSchema =
                new PbRowDataDeserializationSchema(
                        rowType,
                        InternalTypeInfo.of(rowType),
                        SimpleTest.class.getName(),
                        false,
                        false);

        SimpleTest simple =
                SimpleTest.newBuilder()
                        .setB(2L)
                        .setC(false)
                        .setD(0.1f)
                        .setE(0.01)
                        .setF("haha")
                        .build();

        RowData row = deserializationSchema.deserialize(simple.toByteArray());
        row = ProtobufTestHelper.validateRow(row, rowType);

        assertTrue(row.isNullAt(0));
        assertFalse(row.isNullAt(1));
    }

    @Test
    public void testDefaultValues() throws Exception {
        RowType rowType = PbRowTypeInformation.generateRowType(SimpleTest.getDescriptor());
        PbRowDataDeserializationSchema deserializationSchema =
                new PbRowDataDeserializationSchema(
                        rowType,
                        InternalTypeInfo.of(rowType),
                        SimpleTest.class.getName(),
                        false,
                        true);

        SimpleTest simple = SimpleTest.newBuilder().setC(false).setD(0.1f).setE(0.01).build();

        RowData row = deserializationSchema.deserialize(simple.toByteArray());
        row = ProtobufTestHelper.validateRow(row, rowType);

        assertFalse(row.isNullAt(0));
        assertFalse(row.isNullAt(1));
        assertFalse(row.isNullAt(5));
        assertEquals(10, row.getInt(0));
        assertEquals(100L, row.getLong(1));
        assertEquals("f", row.getString(5).toString());
    }
}
