include ../src/shared/project.properties

#------------------------------------------------------------------------------#
# Declare variables.                                                           #
#------------------------------------------------------------------------------#

# Java Sources

JSRCS			= $(wildcard ../src*/*/*.java) \
				$(wildcard ../src*/*/*/*.java) \
				$(wildcard ../src*/*/*/*/*.java) \
				$(wildcard ../src*/*/*/*/*/*.java)

# JNI Headers

JNI_HEADERS		= include/jni/jni_headers.h
JNI_HEADERSX	= include/jni/jni_headersx.h

# Windows Icons

ICON_PNGS		= $(patsubst src/icon/icon_%.svg, icon_%.png, \
				$(wildcard src/icon/icon_*.svg))
ICON_ICO		= include/win32/icon.ico

# CMake

CMAKE			= cmake
CMAKE_TOOLCHAIN	= ../../build/conf/mingw.cmake

CMAKE_FLAGS		= -DCMAKE_BUILD_TYPE=Release

ifdef build.version

CMAKE_FLAGS		+= -DVERSION:STRING=$(build.version)

endif

ifdef WORD_SIZE

CMAKE_FLAGS		+= -DM:STRING=$(WORD_SIZE)

endif

ifeq ($(OS), Windows)

CMAKE_FLAGS		+= -DCMAKE_TOOLCHAIN_FILE=$(CMAKE_TOOLCHAIN)

endif

#------------------------------------------------------------------------------#
# Make the high level targets.                                                 #
#------------------------------------------------------------------------------#

.INTERMEDIATE: $(ICON_PNGS)
.PHONY: all cmake shared sharedx shared_cl buildandtest doxygen clean

all: shared sharedx

#------------------------------------------------------------------------------#
# Make the native libraries and executables.                                   #
#------------------------------------------------------------------------------#

cmake: $(OS)$(WORD_SIZE)/Makefile

$(OS)$(WORD_SIZE)/Makefile: CMakeLists.txt
	mkdir -p -- $(OS)$(WORD_SIZE)
	cd -- $(OS)$(WORD_SIZE) && $(CMAKE) .. $(CMAKE_FLAGS)

shared: cmake $(JNI_HEADERS)
	$(MAKE) -C $(OS)$(WORD_SIZE) -- sst

sharedx: cmake $(JNI_HEADERS) $(JNI_HEADERSX)
	$(MAKE) -C $(OS)$(WORD_SIZE) -- sstx

shared_cl: cmake $(JNI_HEADERS)
	$(MAKE) -C $(OS)$(WORD_SIZE) -- sst_cl

buildandtest: cmake $(ICON_ICO)
	$(MAKE) -C $(OS)$(WORD_SIZE) -- buildandtest

doxygen: cmake
	$(MAKE) -C $(OS)$(WORD_SIZE) -- doxygen

#------------------------------------------------------------------------------#
# Make the JNI headers.                                                        #
#------------------------------------------------------------------------------#

$(JNI_HEADERS):
	$(MAKE) -C .. -- headers

$(JNI_HEADERSX):
	$(MAKE) -C .. -- headersx

#------------------------------------------------------------------------------#
# Make the icon stack.                                                         #
#------------------------------------------------------------------------------#

$(ICON_PNGS): icon_%.png: src/icon/icon_%.svg
	inkscape --without-gui \
		--export-png $@ \
		--export-width $* \
		-- $<

$(ICON_ICO): $(ICON_PNGS)
	gimp -i -b - < ../build/conf/icon.scm > /dev/null
	mv -- icon.ico $(ICON_ICO)

#------------------------------------------------------------------------------#
# Clean the distribution.                                                      #
#------------------------------------------------------------------------------#

clean:
	rm -rf -- $(OS)$(WORD_SIZE)
	rm -f -- $(ICON_ICO)
