cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wall")


add_library( # Sets the name of the library.
             native-lib
             SHARED

             # Provides a relative path to your source file(s).
              src/main/cpp/graphics/GLUtils.cpp
              src/main/cpp/graphics/Matrix.cpp
             src/main/cpp/native-lib.cpp
             src/main/cpp/GLRenderer.cpp
              src/main/cpp/GLrenderS.cpp
              )
include_directories(src/main/cpp/)
target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       android
                         log
                         EGL
                         GLESv2 )