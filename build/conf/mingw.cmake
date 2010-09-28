# Determine the MinGW installation prefix.

find_program(MINGW_GCC_FILENAME NAMES "i586-mingw32msvc-gcc")
get_filename_component(MINGW_PREFIX ${MINGW_GCC_FILENAME} NAME)

string(REGEX REPLACE "^(.*)-gcc\$" "\\1" MINGW_PREFIX ${MINGW_PREFIX})

get_filename_component(MINGW_PREFIX_PATH ${MINGW_GCC_FILENAME} PATH)
get_filename_component(MINGW_PREFIX_PATH ${MINGW_PREFIX_PATH} PATH)

find_file(MINGW_INSTALLATION ${MINGW_PREFIX} PATHS ${MINGW_PREFIX_PATH} NO_DEFAULT_PATH)

if(NOT MINGW_INSTALLATION)
    message(SEND_ERROR "An installation of MinGW could not be found.")
endif(NOT MINGW_INSTALLATION)

# Set the default behavior for finding programs, libraries, and headers.

set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE BOTH)

# Set up the MinGW toolchain.

set(CMAKE_SYSTEM_NAME "Windows")

set(CMAKE_C_COMPILER "${MINGW_PREFIX}-gcc")
set(CMAKE_CXX_COMPILER "${MINGW_PREFIX}-g++")

set(CMAKE_FIND_ROOT_PATH ${MINGW_INSTALLATION})

set(CMAKE_RC_COMPILE_OBJECT "<CMAKE_RC_COMPILER> <FLAGS> -o <OBJECT> <SOURCE>")
set(CMAKE_RC_COMPILER "${MINGW_PREFIX}-windres")
set(CMAKE_RC_FLAGS_INIT "-Ocoff")
