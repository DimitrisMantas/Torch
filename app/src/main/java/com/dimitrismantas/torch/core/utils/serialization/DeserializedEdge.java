/*
 * Torch is a model, open-source Android application for optimal routing
 * in offline mobile devices.
 * Copyright (C) 2021-2022  DIMITRIS(.)MANTAS(@outlook.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

// automatically generated by the FlatBuffers compiler, do not modify

package com.dimitrismantas.torch.core.utils.serialization;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class DeserializedEdge extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_2_0_0(); }
  public static DeserializedEdge getRootAsDeserializedEdge(ByteBuffer _bb) { return getRootAsDeserializedEdge(_bb, new DeserializedEdge()); }
  public static DeserializedEdge getRootAsDeserializedEdge(ByteBuffer _bb, DeserializedEdge obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public DeserializedEdge __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int endVertexLabel() { int o = __offset(4); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public short length() { int o = __offset(6); return o != 0 ? bb.getShort(o + bb_pos) : 0; }
  public short travelTime() { int o = __offset(8); return o != 0 ? bb.getShort(o + bb_pos) : 0; }

  public static int createDeserializedEdge(FlatBufferBuilder builder,
      int endVertexLabel,
      short length,
      short travelTime) {
    builder.startTable(3);
    DeserializedEdge.addEndVertexLabel(builder, endVertexLabel);
    DeserializedEdge.addTravelTime(builder, travelTime);
    DeserializedEdge.addLength(builder, length);
    return DeserializedEdge.endDeserializedEdge(builder);
  }

  public static void startDeserializedEdge(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addEndVertexLabel(FlatBufferBuilder builder, int endVertexLabel) { builder.addInt(0, endVertexLabel, 0); }
  public static void addLength(FlatBufferBuilder builder, short length) { builder.addShort(1, length, 0); }
  public static void addTravelTime(FlatBufferBuilder builder, short travelTime) { builder.addShort(2, travelTime, 0); }
  public static int endDeserializedEdge(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }
  public static void finishDeserializedEdgeBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
  public static void finishSizePrefixedDeserializedEdgeBuffer(FlatBufferBuilder builder, int offset) { builder.finishSizePrefixed(offset); }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public DeserializedEdge get(int j) { return get(new DeserializedEdge(), j); }
    public DeserializedEdge get(DeserializedEdge obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

