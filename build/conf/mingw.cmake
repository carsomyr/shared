# Determine the MinGW installation prefix.

find_program(MINGW_PREFIX NAMES "i586-mingw32msvc-gcc" "i386-mingw32-gcc")
get_filename_component(MINGW_PREFIX ${MINGW_PREFIX} NAME)

string(REGEX REPLACE "^(.*)-gcc$" "\\1" MINGW_PREFIX ${MINGW_PREFIX})

find_file(MINGW_INSTALLATION ${MINGW_PREFIX} PATHS "/usr" "/opt/local" NO_DEFAULT_PATH)

if(NOT MINGW_INSTALLATION)
    message(SEND_ERROR "An installation of MinGW could not be found.")
endif(NOT MINGW_INSTALLATION)

# Set up the MinGW toolchain.

set(CMAKE_SYSTEM_NAME "Windows")

set(CMAKE_C_COMPILER "${MINGW_PREFIX}-gcc")
set(CMAKE_CXX_COMPILER "${MINGW_PREFIX}-g++")

set(CMAKE_FIND_ROOT_PATH ${MINGW_INSTALLATION})

set(CMAKE_RC_COMPILE_OBJECT "<CMAKE_RC_COMPILER> <FLAGS> -o <OBJECT> <SOURCE>")
set(CMAKE_RC_COMPILER "${MINGW_PREFIX}-windres")
set(CMAKE_RC_FLAGS_INIT "-Ocoff")
