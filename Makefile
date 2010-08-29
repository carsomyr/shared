include build/*.mk

#------------------------------------------------------------------------------#
# Declare variables.                                                           #
#------------------------------------------------------------------------------#

ANT					= java -cp 'build/ant-launcher.jar' \
					org.apache.tools.ant.launch.Launcher

# Java Sources

JSRCS				= $(wildcard src*/*/*.java) \
					$(wildcard src*/*/*/*.java) \
					$(wildcard src*/*/*/*/*.java)

# C++ Headers, Sources, and Objects

CSRCS				= $(wildcard native/src/*/*.cpp)
CHEADERS			= $(wildcard native/include/*/*.hpp)

# JNI Headers

JNI_HEADERS			= native/include/jni/jni_headers.h
JNI_HEADERSX		= native/include/jni/jni_headersx.h

# Tokens

BUILD_TOKEN			= .bin
CHECKSTYLE_TOKEN	= .checkstyle
DOXYDOC_TOKEN		= .doxydoc
JAVADOC_TOKEN		= .javadoc
PUBLISH_TOKEN		= .publish
PUBLISHX_TOKEN		= .publishx

# Macros

LIB_DIR				= native/$(OS)$(WORD_SIZE)

MAKE_SHARED			= \
	$(MAKE) -C native shared \
	&& cp $(LIB_DIR)/$(LIB_PREFIX)sst.$(LIB_SUFFIX) \
		build/lib/$(LIB_PREFIX)sst$(WORD_SIZE).$(LIB_SUFFIX) \
	&& $(ANT) build-resource
MAKE_SHAREDX		= \
	$(MAKE) -C native sharedx \
	&& cp $(LIB_DIR)/$(LIB_PREFIX)sstx.$(LIB_SUFFIX) \
		build/libx/$(LIB_PREFIX)sstx$(WORD_SIZE).$(LIB_SUFFIX) \
	&& $(ANT) build-resource
MAKE_SHARED_CL		= \
	$(MAKE) -C native shared_cl \
	&& cp $(LIB_DIR)/$(LIB_PREFIX)sst_cl.$(LIB_SUFFIX) \
		build/lib/$(LIB_PREFIX)sst_cl$(WORD_SIZE).$(LIB_SUFFIX) \
	&& $(ANT) build-resource
MAKE_BUILD_AND_TEST	= \
	$(MAKE) -C native buildandtest \
	&& cp $(LIB_DIR)/buildandtest.exe .

#------------------------------------------------------------------------------#
# Make the high level targets.                                                 #
#------------------------------------------------------------------------------#

.PHONY: all \
	shared sharedx shared_cl shared32 sharedx32 shared64 sharedx64 \
	headers headersx \
	win32 java jar javadoc doxydoc checkstyle publish publishx \
	clean clean32 clean64 clean_win32 distclean

all: shared sharedx

#------------------------------------------------------------------------------#
# Make the native libraries and executables.                                   #
#------------------------------------------------------------------------------#

# Default

shared: bin/lib/$(LIB_PREFIX)sst.$(LIB_SUFFIX)

build/lib/$(LIB_PREFIX)sst.$(LIB_SUFFIX): \
	bin/lib/$(LIB_PREFIX)sst.$(LIB_SUFFIX)

bin/lib/$(LIB_PREFIX)sst.$(LIB_SUFFIX): \
	$(CSRCS) $(CHEADERS) $(JNI_HEADERS)
	$(MAKE_SHARED)

sharedx: bin/libx/$(LIB_PREFIX)sstx.$(LIB_SUFFIX)

build/libx/$(LIB_PREFIX)sstx.$(LIB_SUFFIX): \
	bin/libx/$(LIB_PREFIX)sstx.$(LIB_SUFFIX)

bin/libx/$(LIB_PREFIX)sstx.$(LIB_SUFFIX): \
	$(CSRCS) $(CHEADERS) $(JNI_HEADERS) $(JNI_HEADERSX)
	$(MAKE_SHAREDX)

shared_cl: bin/lib/$(LIB_PREFIX)sst_cl.$(LIB_SUFFIX)

build/lib/$(LIB_PREFIX)sst_cl.$(LIB_SUFFIX): \
	bin/lib/$(LIB_PREFIX)sst_cl.$(LIB_SUFFIX)

bin/lib/$(LIB_PREFIX)sst_cl.$(LIB_SUFFIX): \
	$(CSRCS) $(CHEADERS) $(JNI_HEADERS)
	$(MAKE_SHARED_CL)

# 32-Bit

shared32: WORD_SIZE = 32
shared32: bin/lib/$(LIB_PREFIX)sst32.$(LIB_SUFFIX)

bin/lib/$(LIB_PREFIX)sst32.$(LIB_SUFFIX): \
	$(CSRCS) $(CHEADERS) $(JNI_HEADERS)
	$(MAKE_SHARED)

sharedx32: WORD_SIZE = 32
sharedx32: bin/libx/$(LIB_PREFIX)sstx32.$(LIB_SUFFIX)

bin/libx/$(LIB_PREFIX)sstx32.$(LIB_SUFFIX): \
	$(CSRCS) $(CHEADERS) $(JNI_HEADERS) $(JNI_HEADERSX)
	$(MAKE_SHAREDX)

# 64-Bit

shared64: WORD_SIZE = 64
shared64: bin/lib/$(LIB_PREFIX)sst64.$(LIB_SUFFIX)

bin/lib/$(LIB_PREFIX)sst64.$(LIB_SUFFIX): \
	$(CSRCS) $(CHEADERS) $(JNI_HEADERS)
	$(MAKE_SHARED)

sharedx64: WORD_SIZE = 64
sharedx64: bin/libx/$(LIB_PREFIX)sstx64.$(LIB_SUFFIX)

bin/libx/$(LIB_PREFIX)sstx64.$(LIB_SUFFIX): \
	$(CSRCS) $(CHEADERS) $(JNI_HEADERS) $(JNI_HEADERSX)
	$(MAKE_SHAREDX)

# Windows 32-Bit Cross-Compile

win32: OS = Windows
win32: LIB_PREFIX =
win32: LIB_SUFFIX = dll
win32: bin/lib/sst.dll bin/libx/sstx.dll buildandtest.exe

bin/lib/sst.dll: $(CSRCS) $(CHEADERS) $(JNI_HEADERS)
	$(MAKE_SHARED)

bin/libx/sstx.dll: $(CSRCS) $(CHEADERS) $(JNI_HEADERS) $(JNI_HEADERSX)
	$(MAKE_SHAREDX)

buildandtest.exe: $(CSRCS) $(CHEADERS)
	$(MAKE_BUILD_AND_TEST)

#------------------------------------------------------------------------------#
# Make the JNI headers.                                                        #
#------------------------------------------------------------------------------#

headers: $(JNI_HEADERS)

$(JNI_HEADERS): $(JSRCS)
	$(ANT) headers

headersx: $(JNI_HEADERSX)

$(JNI_HEADERSX): $(JSRCS)
	$(ANT) headersx

#------------------------------------------------------------------------------#
# Make the Java classes.                                                       #
#------------------------------------------------------------------------------#

java: $(BUILD_TOKEN)

$(BUILD_TOKEN): $(JSRCS)
	$(ANT) build

#------------------------------------------------------------------------------#
# Make Jars.                                                                   #
#------------------------------------------------------------------------------#

jar: sst.jar

sst.jar: $(BUILD_TOKEN)
	$(ANT) jar
	touch $@

#------------------------------------------------------------------------------#
# Make the Javadoc.                                                            #
#------------------------------------------------------------------------------#

javadoc: $(JAVADOC_TOKEN)

$(JAVADOC_TOKEN): $(JSRCS)
	$(ANT) javadoc

#------------------------------------------------------------------------------#
# Make the Doxygen native documentation.                                       #
#------------------------------------------------------------------------------#

doxydoc: $(DOXYDOC_TOKEN)

$(DOXYDOC_TOKEN): $(CSRCS) $(CHEADERS)
	$(MAKE) -C native doxygen
	touch $@

#------------------------------------------------------------------------------#
# Run Checkstyle.                                                              #
#------------------------------------------------------------------------------#

checkstyle: $(CHECKSTYLE_TOKEN)

$(CHECKSTYLE_TOKEN): $(JSRCS) $(CSRCS) $(CHEADERS)
	$(ANT) checkstyle

#------------------------------------------------------------------------------#
# Publish Jars.                                                                #
#------------------------------------------------------------------------------#

publish: $(PUBLISH_TOKEN)

$(PUBLISH_TOKEN): $(JSRCS) build/lib/$(LIB_PREFIX)sst.$(LIB_SUFFIX)
	$(ANT) ivy-publish

publishx: $(PUBLISHX_TOKEN)

$(PUBLISHX_TOKEN): $(JSRCS) build/libx/$(LIB_PREFIX)sstx.$(LIB_SUFFIX)
	$(ANT) ivy-publishx

#------------------------------------------------------------------------------#
# Clean the distribution.                                                      #
#------------------------------------------------------------------------------#

clean: clean32 clean64 clean_win32
	rm -rf doxydoc demo
	rm -f $(DOXYDOC_TOKEN) *.exe
	$(ANT) clean
	$(MAKE) -C native clean

clean32: WORD_SIZE = 32
clean32:
	$(MAKE) -C native clean

clean64: WORD_SIZE = 64
clean64:
	$(MAKE) -C native clean

clean_win32: OS = Windows
clean_win32:
	$(MAKE) -C native clean

distclean: clean
	$(ANT) distclean
