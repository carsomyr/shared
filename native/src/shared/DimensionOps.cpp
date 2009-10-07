/*
 * This file is part of the Shared Scientific Toolbox in Java ("this library").
 * 
 * Copyright (C) 2007 Roy Liu
 * 
 * This library is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library. If not, see
 * http://www.gnu.org/licenses/.
 */

#include <DimensionOps.hpp>

void DimensionOps::assignBaseIndices( //
        jint *srcIndices, //
        const jint *srcDArr, jint *srcDArrModified, const jint *srcSArr, jint *srcSArrModified, //
        jint ndims, jint dim) {

    memcpy(srcDArrModified, srcDArr, sizeof(jint) * dim);
    memcpy(srcDArrModified + dim, srcDArr + dim + 1, sizeof(jint) * ((ndims - 1) - dim));

    memcpy(srcSArrModified, srcSArr, sizeof(jint) * dim);
    memcpy(srcSArrModified + dim, srcSArr + dim + 1, sizeof(jint) * ((ndims - 1) - dim));

    MappingOps::assignMappingIndices(srcIndices, srcDArrModified, srcSArrModified, ndims - 1);
}
