

cmake_minimum_required(VERSION 3.4.1)

add_subdirectory(mbedtls)
add_subdirectory(libssh2)
add_subdirectory(markdown)

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED


        native-lib.c
        utils_string.c
        utils_httpc.c
        utils_url.c)

add_library(cJSON ${CMAKE_CURRENT_SOURCE_DIR}/cJSON/cJSON.c)
add_library(tmd5 ${CMAKE_CURRENT_SOURCE_DIR}/tmd5/tmd5.c)
add_library(tinyexpr ${CMAKE_CURRENT_SOURCE_DIR}/tinyexpr/tinyexpr.c)

find_library(
        log-lib
        log)

target_include_directories(native-lib PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/mbedtls/include)
target_link_libraries(
        native-lib
        markdown
        mbedtls
        cJSON
        tinyexpr
        tmd5
        ${log-lib})