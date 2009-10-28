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

#include <BuildAndTest.hpp>

void BuildAndTest::execAndWaitFor(const TCHAR *cmd) {

    STARTUPINFO si;
    PROCESS_INFORMATION pi;

    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);
    ZeroMemory(&pi, sizeof(pi));

    // Start the child process.
    if (!CreateProcess(NULL, //
            (TCHAR *) cmd, //
            NULL, //
            NULL, //
            FALSE, //
            0, //
            NULL, //
            NULL, //
            &si, //
            &pi) //
    ) {

        printf("Could not create child process '%s'.\n", cmd);

        return;
    }

    // Wait until child process exits.
    WaitForSingleObject(pi.hProcess, INFINITE);

    // Close process and thread handles.
    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);
}

int _tmain(int argc, TCHAR *argv[]) {

    BuildAndTest::execAndWaitFor("cmd /C java -cp build/ant-launcher.jar org.apache.tools.ant.launch.Launcher");
    BuildAndTest::execAndWaitFor("cmd /C java -cp bin shared.test.All");
    BuildAndTest::execAndWaitFor("cmd /C java -cp bin shared.test.AllNative");
    BuildAndTest::execAndWaitFor("cmd /C java -cp bin sharedx.test.AllX");

    printf("\nPress ENTER to continue.\n");

    for (;;) {

        switch (getch()) {
        case '\r':
        case -1:
            goto end;
        }
    }

    end: return 0;
}
