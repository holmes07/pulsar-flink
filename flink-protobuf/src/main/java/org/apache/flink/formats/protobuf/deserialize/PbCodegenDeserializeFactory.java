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

package org.apache.flink.formats.protobuf.deserialize;

import org.apache.flink.formats.protobuf.PbCodegenException;
import org.apache.flink.formats.protobuf.PbFormatUtils;
import org.apache.flink.table.types.logical.ArrayType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.MapType;
import org.apache.flink.table.types.logical.RowType;

import com.google.protobuf.Descriptors;

public class PbCodegenDeserializeFactory {
    public static PbCodegenDeserializer getPbCodegenDes(
            Descriptors.FieldDescriptor fd, LogicalType type, boolean readDefaultValues)
            throws PbCodegenException {
        // We do not use FieldDescriptor to check because when FieldDescriptor is an element type in
        // array,
        // FieldDescriptor.isRepeated() is still true
        if (type instanceof RowType) {
            return new PbCodegenRowDeserializer(
                    fd.getMessageType(), (RowType) type, readDefaultValues);
        } else if (PbFormatUtils.isSimpleType(type)) {
            return new PbCodegenSimpleDeserializer(fd);
        } else if (type instanceof ArrayType) {
            return new PbCodegenArrayDeserializer(
                    fd, ((ArrayType) type).getElementType(), readDefaultValues);
        } else if (type instanceof MapType) {
            return new PbCodegenMapDeserializer(fd, (MapType) type, readDefaultValues);
        } else {
            throw new PbCodegenException("cannot support flink type: " + type);
        }
    }

    public static PbCodegenDeserializer getPbCodegenTopRowDes(
            Descriptors.Descriptor descriptor, RowType rowType, boolean readDefaultValues) {
        return new PbCodegenRowDeserializer(descriptor, rowType, readDefaultValues);
    }
}
