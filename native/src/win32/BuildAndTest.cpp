/*
 * Copyright (c) 2008-2010 Roy Liu
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *     following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the author nor the names of any contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <BuildAndTest.hpp>

void BuildAndTest::execAndWaitFor(const TCHAR *cmd) {

    STARTUPINFO si;
    PROCESS_INFORMATION pi;

    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);
    ZeroMemory(&pi, sizeof(pi));

    HMODULE kernel32Module = GetModuleHandle(TEXT("kernel32"));
    BOOL (WINAPI *fn_Wow64DisableWow64FsRedirection)(PVOID *) = (BOOL (WINAPI *)(PVOID *)) GetProcAddress( //
            kernel32Module, "Wow64DisableWow64FsRedirection");
    BOOL (WINAPI *fn_Wow64RevertWow64FsRedirection)(PVOID) = (BOOL (WINAPI *)(PVOID)) GetProcAddress( //
            kernel32Module, "Wow64RevertWow64FsRedirection");

    PVOID save = NULL;

    if (fn_Wow64DisableWow64FsRedirection && fn_Wow64RevertWow64FsRedirection) {

        if (!fn_Wow64DisableWow64FsRedirection(&save)) {

            printf("Could not disable file system redirection.\n");

            return;
        }
    }

    // Start the child process.
    BOOL res = CreateProcess(NULL, //
            (TCHAR *) cmd, //
            NULL, //
            NULL, //
            FALSE, //
            0, //
            NULL, //
            NULL, //
            &si, //
            &pi);

    if (fn_Wow64DisableWow64FsRedirection && fn_Wow64RevertWow64FsRedirection) {

        if (!fn_Wow64RevertWow64FsRedirection(save)) {

            printf("Could not revert to previous file system redirection state.\n");

            return;
        }
    }

    if (!res) {

        printf("Could not create child process \"%s\".\n", cmd);

        return;
    }

    // Wait until child process exits.
    WaitForSingleObject(pi.hProcess, INFINITE);

    // Close process and thread handles.
    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);
}

int _tmain(int argc, TCHAR *argv[]) {

    BuildAndTest::execAndWaitFor("java -cp build/ant-launcher.jar org.apache.tools.ant.launch.Launcher");
    BuildAndTest::execAndWaitFor("java -cp bin org.shared.test.All");
    BuildAndTest::execAndWaitFor("java -cp bin org.shared.test.AllNative");
    BuildAndTest::execAndWaitFor("java -cp bin org.sharedx.test.AllX");

    //

    printf("\nPress ENTER to continue.\n");

    for (;;) {

        switch (getchar()) {
        case '\n':
        case -1:
            goto end;
        }
    }

    end: return 0;
}
