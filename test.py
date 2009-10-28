#!/usr/bin/env python

"""A script for running SST JUnit tests.
"""

import subprocess
import sys

def main():
    """
    The main method body.
    """

    subprocess.Popen(["make", "java"]).wait()

    javacmd = ["java", "-ea", "-XX:+AggressiveHeap", "-XX:+AllowUserSignalHandlers", "-Xcheck:jni", "-cp", "bin"];

    subprocess.Popen(javacmd + ["shared.test.All"]).wait()
    subprocess.Popen(javacmd + ["shared.test.Demo"]).wait()
    subprocess.Popen(javacmd + ["shared.test.AllNative"]).wait()
    subprocess.Popen(javacmd + ["sharedx.test.AllX"]).wait()

#

if __name__ == "__main__":
    sys.exit(main())
