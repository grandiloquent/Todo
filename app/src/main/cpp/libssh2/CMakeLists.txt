cmake_minimum_required(VERSION 3.4.1)

set(MBEDTLS_INCLUDE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../mbedtls/include/mbedtls)

list(APPEND PRIVATE_COMPILE_DEFINITIONS LIBSSH2_MBEDTLS)
list(APPEND PRIVATE_INCLUDE_DIRECTORIES ${MBEDTLS_INCLUDE_DIR})
list(APPEND LIBRARIES ${MBEDTLS_LIBRARIES})
list(APPEND PC_LIBS -lmbedcrypto)
link_directories(${MBEDTLS_LIBRARY_DIR})

set(CRYPTO_SOURCES
        ${CMAKE_CURRENT_SOURCE_DIR}/src/mbedtls.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/mbedtls.h)

set(SOURCES
        ${CRYPTO_SOURCES}
        ${CMAKE_CURRENT_SOURCE_DIR}/src/agent.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/blf.h
        ${CMAKE_CURRENT_SOURCE_DIR}/src/bcrypt_pbkdf.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/blowfish.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/channel.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/channel.h
        ${CMAKE_CURRENT_SOURCE_DIR}/src/comp.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/comp.h
        ${CMAKE_CURRENT_SOURCE_DIR}/src/crypt.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/crypto.h
        ${CMAKE_CURRENT_SOURCE_DIR}/src/global.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/hostkey.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/keepalive.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/kex.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/knownhost.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/libssh2_priv.h
        ${CMAKE_CURRENT_SOURCE_DIR}/src/mac.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/mac.h
        ${CMAKE_CURRENT_SOURCE_DIR}/src/misc.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/misc.h
        ${CMAKE_CURRENT_SOURCE_DIR}/src/packet.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/packet.h
        ${CMAKE_CURRENT_SOURCE_DIR}/src/pem.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/publickey.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/scp.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/session.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/session.h
        ${CMAKE_CURRENT_SOURCE_DIR}/src/sftp.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/sftp.h
        ${CMAKE_CURRENT_SOURCE_DIR}/src/transport.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/transport.h
        ${CMAKE_CURRENT_SOURCE_DIR}/src/userauth.c
        ${CMAKE_CURRENT_SOURCE_DIR}/src/userauth.h
        ${CMAKE_CURRENT_SOURCE_DIR}/src/version.c)

add_library(libssh2 ${SOURCES})
target_compile_definitions(libssh2 PRIVATE ${PRIVATE_COMPILE_DEFINITIONS})
