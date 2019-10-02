

cmake_minimum_required(VERSION 3.4.1)

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED


        native-lib.c
        utils_string.c)

add_library(markdown
        STATIC
        ${CMAKE_CURRENT_SOURCE_DIR}/markdown/autolink.c
        ${CMAKE_CURRENT_SOURCE_DIR}/markdown/buffer.c
        ${CMAKE_CURRENT_SOURCE_DIR}/markdown/houdini_href_e.c
        ${CMAKE_CURRENT_SOURCE_DIR}/markdown/houdini_html_e.c
        ${CMAKE_CURRENT_SOURCE_DIR}/markdown/html.c
        ${CMAKE_CURRENT_SOURCE_DIR}/markdown/html_smartypants.c
        ${CMAKE_CURRENT_SOURCE_DIR}/markdown/markdown.c
        ${CMAKE_CURRENT_SOURCE_DIR}/markdown/stack.c
        )

find_library(
        log-lib

        log)


target_link_libraries(
        native-lib
        markdown

        ${log-lib})