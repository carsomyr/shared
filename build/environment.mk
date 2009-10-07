OS			:= $(shell uname)

ifeq ($(OS), Darwin)

LIB_PREFIX	= lib
LIB_SUFFIX	= jnilib

else

LIB_PREFIX	= lib
LIB_SUFFIX	= so

endif

export OS
export LIB_PREFIX
export LIB_SUFFIX
export WORD_SIZE
