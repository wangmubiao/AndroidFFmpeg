cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99 -Wall -DANDROID_NDK -DDISABLE_IMPORTGL -DGL_GLEXT_PROTOTYPES")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wall -UNDEBUG -DGL_GLEXT_PROTOTYPES")

file(GLOB OPENGL_SOURCE memtransfer.cpp
                        util.cpp
                        display.cpp)
add_library(opengl STATIC ${OPENGL_SOURCE})
target_link_libraries(opengl log dl android GLESv2 EGL)