/*
 * This file is part of the Shared Scientific Toolbox in Java ("this library").
 * 
 * Copyright (C) 2009 Roy Liu
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

#include <Common.hpp>
#include <MappingOps.hpp>

#include <JNIHeadersWrap.hpp>

#ifndef _Included_IndexOps
#define _Included_IndexOps

/**
 * A class for indexing operations.
 */
class IndexOps {

public:

    /**
     * Extracts the valid indices along a dimension anchored at the given logical index.
     * 
     * @param env
     *      the JNI environment.
     * @param thisObj
     *      this object.
     * @param srcV
     *      the source values.
     * @param srcD
     *      the source dimensions.
     * @param srcS
     *      the source strides.
     * @param logical
     *      the logical index.
     * @return the valid indices.
     */
    static jintArray find(JNIEnv *env, jobject thisObj, //
            jintArray srcV, jintArray srcD, jintArray srcS, jintArray logical);

private:

    inline static jint *findProxy(JNIEnv *, //
            jintArray, jintArray, jintArray, jintArray);
};

#endif
