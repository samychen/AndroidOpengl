cmake_minimum_required(VERSION 3.4.1)
set(distribution_DIR ${CMAKE_SOURCE_DIR}/../../../../src/main/jniLibs)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY PROJECTSOURCEDIR/../jniLibs/{ANDROID_ABI})

 set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall")
 set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fno-rtti -fno-exceptions -Wall")
if(CMAKE_COMPILER_IS_GNUCXX)
    set(CMAKE_CXX_FLAGS "-std=c++11 ${CMAKE_CXX_FLAGS}")
    message(STATUS "optional:-std=c++11")
endif(CMAKE_COMPILER_IS_GNUCXX)

add_library(
                beauty-lib
                SHARED
                # graphics
                src/main/cpp/graphics/GLUtils.h
                src/main/cpp/graphics/GLUtils.cpp
                src/main/cpp/graphics/Matrix.h
                src/main/cpp/graphics/Matrix.cpp

                # filter
                # src/main/cpp/samplecode.cpp
                src/main/cpp/facetune/textureeffect.cpp
                 src/main/cpp/facetune/beauty_jni.cpp
             )
add_library( libbeautytune
             STATIC
             IMPORTED
            )
set_target_properties(
                       libbeautytune
                       PROPERTIES IMPORTED_LOCATION
                       ../../../../src/main/jniLibs/${ANDROID_ABI}/libbeautytune.a)
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libbeautytune.a
${CMAKE_CURRENT_SOURCE_DIR}/.externalNativeBuild/cmake/debug/${ANDROID_ABI}/libbeautytune.a COPYONLY)

include_directories(src/main/cpp/)

find_library(log-lib log)

target_link_libraries( # Specifies the target library.
                       beauty-lib
                       EGL
                       GLESv2
                       libbeautytune
                       android
                       ${log-lib} )