/*
 * This file is part of the Shared Scientific Toolbox in Java ("this library").
 * 
 * Copyright (C) 2008 Roy Liu
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

#include <windows.h>
#include <stdio.h>
#include <tchar.h>
#include <conio.h>

#ifndef _Included_BuildAndTest
#define _Included_BuildAndTest

/**
 * A class for build followed by test of the SST on Windows.
 */
class BuildAndTest {

public:

    /**
     * Executes the given command line and waits for completion.
     * 
     * @param cmd
     *      the command line.
     */
    static void execAndWaitFor(const TCHAR *cmd);
};

#endif
