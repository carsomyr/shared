include(FindDoxygen)

# Set appropriate properties for verbose compiler output that is readable by Eclipse file indexers and error parsers.

set(CMAKE_VERBOSE_MAKEFILE "ON")

if(CMAKE_COMPILER_IS_GNUCC)
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fmessage-length=0")
endif(CMAKE_COMPILER_IS_GNUCC)

if(CMAKE_COMPILER_IS_GNUCXX)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fmessage-length=0")
endif(CMAKE_COMPILER_IS_GNUCXX)

# Restore the APPLE variable if cross compiling on Mac OS.

if(CMAKE_CROSSCOMPILING AND CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin")
    set(APPLE "1")
endif(CMAKE_CROSSCOMPILING AND CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin")

# Add the doxygen build target.

if(DOXYGEN_FOUND)

    file(GLOB_RECURSE FILES
        "src/*.cpp"
        "include/*.hpp")

    add_custom_command(OUTPUT "../../doxydoc"
        COMMAND ${DOXYGEN_EXECUTABLE}
        WORKING_DIRECTORY "../../build"
        DEPENDS ${FILES})
    add_custom_target(doxygen DEPENDS "../doxydoc")

endif(DOXYGEN_FOUND)

# If not provided by the user, the version is an empty string.

if(NOT VERSION)
    set(VERSION "")
endif(NOT VERSION)
