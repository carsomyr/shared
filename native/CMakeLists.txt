cmake_minimum_required(VERSION "2.8")

project("shared")

include("../build/conf/common.cmake")
include("FindJNI")

enable_language("CXX")
enable_language("Java")

if(MINGW)
    enable_language("RC")
endif(MINGW)

aux_source_directory("src/shared" SRC_SHARED)
aux_source_directory("src/shared_cl" SRC_SHARED_CL)
aux_source_directory("src/shared_common" SRC_SHARED_COMMON)
aux_source_directory("src/sharedx" SRC_SHAREDX)

if(MINGW)
    aux_source_directory("src/win32" SRC_WIN32)
endif(MINGW)

# Try to divine the location of JNI headers.

if(NOT JAVA_INCLUDE_PATH)

    file(GLOB TRY_JAVA_INCLUDE_PATH
        "/usr/lib/jvm/java-6-*/include"
        "/usr/java/jdk1.6.0_*/include")

    find_path(JAVA_INCLUDE_PATH NAMES "jni.h" PATHS ${TRY_JAVA_INCLUDE_PATH} NO_DEFAULT_PATH)
    find_path(JAVA_INCLUDE_PATH2 NAMES "jni_md.h" PATHS "${JAVA_INCLUDE_PATH}" "${JAVA_INCLUDE_PATH}/win32" "${JAVA_INCLUDE_PATH}/linux" NO_DEFAULT_PATH)

endif(NOT JAVA_INCLUDE_PATH)

if(NOT JAVA_INCLUDE_PATH)
    message(SEND_ERROR "JNI headers could not be found.")
endif(NOT JAVA_INCLUDE_PATH)

include_directories(
    "include/shared"
    "include/shared_cl"
    "include/shared_common"
    "include/sharedx"
    "include/win32"
    "include/jni"
    ${JAVA_INCLUDE_PATH}
    ${JAVA_INCLUDE_PATH2})

# Add the 'sst' build target.

set(BUILD_TARGETS "sst")
add_library("sst" SHARED ${SRC_SHARED} ${SRC_SHARED_COMMON})
set_target_properties("sst" PROPERTIES SOVERSION ${VERSION})
set_target_properties("sst" PROPERTIES VERSION ${VERSION})
set_target_properties("sst" PROPERTIES COMPILE_FLAGS "")
set_target_properties("sst" PROPERTIES LINK_FLAGS "")

# Add the 'sstx' build target if FFTW3 is found.

find_library(FFTW3_LIBRARY_FILENAME "fftw3")
get_filename_component(FFTW3_LIBRARY_PATH ${FFTW3_LIBRARY_FILENAME} PATH)
find_path(FFTW3_INCLUDE_PATH "fftw3.h")

if(FFTW3_LIBRARY_PATH AND FFTW3_INCLUDE_PATH)

    include_directories(${FFTW3_INCLUDE_PATH})

    list(APPEND BUILD_TARGETS "sstx")
    add_library("sstx" SHARED ${SRC_SHARED} ${SRC_SHARED_COMMON} ${SRC_SHAREDX})
    set_target_properties("sstx" PROPERTIES SOVERSION ${VERSION})
    set_target_properties("sstx" PROPERTIES VERSION ${VERSION})
    set_target_properties("sstx" PROPERTIES COMPILE_FLAGS "")

    # Be conservative with flags on Mac OS.

    if(CMAKE_SYSTEM_NAME MATCHES "Darwin")

        set_target_properties(sstx PROPERTIES LINK_FLAGS "-L${FFTW3_LIBRARY_PATH} -Wl,-lfftw3")

    else(CMAKE_SYSTEM_NAME MATCHES "Darwin")

        set_target_properties(sstx PROPERTIES LINK_FLAGS "-L${FFTW3_LIBRARY_PATH} -Wl,-Bstatic -Wl,-whole-archive -Wl,-lfftw3 -Wl,-no-whole-archive -Wl,-Bdynamic")

    endif(CMAKE_SYSTEM_NAME MATCHES "Darwin")

endif(FFTW3_LIBRARY_PATH AND FFTW3_INCLUDE_PATH)

# Add the 'sst_cl' build target if OpenCL is found.

find_library(CL_LIBRARY_FILENAME "OpenCL")

if(CL_LIBRARY_FILENAME)

    list(APPEND BUILD_TARGETS "sst_cl")
    add_library("sst_cl" SHARED ${SRC_SHARED_CL})
    set_target_properties("sst_cl" PROPERTIES SOVERSION ${VERSION})
    set_target_properties("sst_cl" PROPERTIES VERSION ${VERSION})
    set_target_properties("sst_cl" PROPERTIES COMPILE_FLAGS "")

    if(APPLE)

        target_link_libraries("sst_cl" ${CL_LIBRARY_FILENAME})

        set_target_properties("sst_cl" PROPERTIES LINK_FLAGS "")

    else(APPLE)

        get_filename_component(CL_LIBRARY_PATH ${CL_LIBRARY_FILENAME} PATH)
        find_path(CL_INCLUDE_PATH "CL/cl.h")

        include_directories(${CL_INCLUDE_PATH})

        set_target_properties("sst_cl" PROPERTIES LINK_FLAGS "-L${CL_LIBRARY_PATH} -Wl,-lOpenCL")

    endif(APPLE)

endif(CL_LIBRARY_FILENAME)

# Special suffix handling for Mac OS.

if(MINGW)

    foreach(BUILD_TARGET ${BUILD_TARGETS})
        set_target_properties(${BUILD_TARGET} PROPERTIES PREFIX "")
    endforeach(BUILD_TARGET ${BUILD_TARGETS})

    set(BUILD_TARGETS ${BUILD_TARGETS} "buildandtest")
    add_executable("buildandtest" ${SRC_WIN32} "src/icon/icon.rc")
    set_target_properties("buildandtest" PROPERTIES VERSION ${VERSION})
    set_target_properties("buildandtest" PROPERTIES COMPILE_FLAGS "")
    set_target_properties("buildandtest" PROPERTIES LINK_FLAGS "")

    foreach(BUILD_TARGET ${BUILD_TARGETS})

        get_target_property(COMPILE_FLAGS ${BUILD_TARGET} COMPILE_FLAGS)
        set_target_properties(${BUILD_TARGET} PROPERTIES COMPILE_FLAGS "${COMPILE_FLAGS} -Wall -Werror -pedantic -Wno-long-long")

        get_target_property(LINK_FLAGS ${BUILD_TARGET} LINK_FLAGS)
        set_target_properties(${BUILD_TARGET} PROPERTIES LINK_FLAGS "${LINK_FLAGS} -Wl,--add-stdcall-alias")

    endforeach(BUILD_TARGET ${BUILD_TARGETS})

else(MINGW)

    foreach(BUILD_TARGET ${BUILD_TARGETS})

        get_target_property(COMPILE_FLAGS ${BUILD_TARGET} COMPILE_FLAGS)
        set_target_properties(${BUILD_TARGET} PROPERTIES COMPILE_FLAGS "${COMPILE_FLAGS} -Wall -Werror -pedantic -Wno-long-long -fPIC")

        get_target_property(LINK_FLAGS ${BUILD_TARGET} LINK_FLAGS)
        set_target_properties(${BUILD_TARGET} PROPERTIES LINK_FLAGS "${LINK_FLAGS}")

        if(M)

            get_target_property(COMPILE_FLAGS ${BUILD_TARGET} COMPILE_FLAGS)
            set_target_properties(${BUILD_TARGET} PROPERTIES COMPILE_FLAGS "${COMPILE_FLAGS} -m${M}")

            get_target_property(LINK_FLAGS ${BUILD_TARGET} LINK_FLAGS)
            set_target_properties(${BUILD_TARGET} PROPERTIES LINK_FLAGS "${LINK_FLAGS} -m${M}")

        endif(M)

    endforeach(BUILD_TARGET ${BUILD_TARGETS})

endif(MINGW)
