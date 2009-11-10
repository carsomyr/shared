#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""A script for running SST JUnit tests.
"""

import subprocess
import sys

def main():
    """
    The main method body.
    """

    subprocess.call(["make", "jar"])

    javacmd = ["java", "-ea", "-XX:+AggressiveHeap", "-XX:+AllowUserSignalHandlers", "-Xcheck:jni", "-cp", "sst.jar"]

    subprocess.call(javacmd + ["shared.test.All"])
    subprocess.call(javacmd + ["shared.test.Demo"])
    subprocess.call(javacmd + ["shared.test.AllNative"])
    subprocess.call(javacmd + ["sharedx.test.AllX"])

#

if __name__ == "__main__":
    sys.exit(main())
