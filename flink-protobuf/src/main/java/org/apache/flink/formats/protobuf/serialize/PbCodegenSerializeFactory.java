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

package org.apache.flink.formats.protobuf.serialize;

import org.apache.flink.formats.protobuf.PbCodegenException;
import org.apache.flink.formats.protobuf.PbFormatUtils;
import org.apache.flink.table.types.logical.ArrayType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.MapType;
import org.apache.flink.table.types.logical.RowType;

import com.google.protobuf.Descriptors;

public class PbCodegenSerializeFactory {
    public static PbCodegenSerializer getPbCodegenSer(
            Descriptors.FieldDescriptor fd, LogicalType type) throws PbCodegenException {
        if (type instanceof RowType) {
            return new PbCodegenRowSerializer(fd.getMessageType(), (RowType) type);
        } else if (PbFormatUtils.isSimpleType(type)) {
            return new PbCodegenSimpleSerializer(fd, type);
        } else if (type instanceof ArrayType) {
            return new PbCodegenArraySerializer(fd, ((ArrayType) type).getElementType());
        } else if (type instanceof MapType) {
            return new PbCodegenMapSerializer(fd, (MapType) type);
        } else {
            throw new PbCodegenException("Cannot support flink data type: " + type);
        }
    }

    public static PbCodegenSerializer getPbCodegenTopRowSer(
            Descriptors.Descriptor descriptor, RowType rowType) {
        return new PbCodegenRowSerializer(descriptor, rowType);
    }
}
