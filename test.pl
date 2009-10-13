#!/usr/bin/env perl

my $java =
"java -ea -XX:+AggressiveHeap -XX:+AllowUserSignalHandlers -Xcheck:jni -cp bin/";

system("$java shared.test.All");
print("\n");
system("$java shared.test.Demo");
print("\n");
system("$java shared.test.AllNative");
print("\n");
system("$java sharedx.test.AllX");
print("\n");
