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
                lesson-lib

                SHARED

                # graphics
                src/main/cpp/graphics/GLUtils.h
                src/main/cpp/graphics/GLUtils.cpp
                src/main/cpp/graphics/Matrix.h
                src/main/cpp/graphics/Matrix.cpp

                # Provides a relative path to your source file(s).
                # lesson1
                src/main/cpp/lesson1/Native1Lesson.cpp
                src/main/cpp/lesson1/Native1Lesson.h

                # lesson2
                src/main/cpp/lesson2/Native2Lesson.cpp
                src/main/cpp/lesson2/Native2Lesson.h

                # lesson3
                src/main/cpp/lesson3/Native3Lesson.cpp
                src/main/cpp/lesson3/Native3Lesson.h

                # lesson4
                src/main/cpp/lesson4/Native4Lesson.cpp
                src/main/cpp/lesson4/Native4Lesson.h

                # lesson5
                src/main/cpp/lesson5/Native5Lesson.cpp
                src/main/cpp/lesson5/Native5Lesson.h

                # lesson6
                src/main/cpp/lesson6/Native6Lesson.cpp
                src/main/cpp/lesson6/Native6Lesson.h

                # lesson7
                src/main/cpp/lesson7/Native7Lesson.cpp
                src/main/cpp/lesson7/Native7Lesson.h
                src/main/cpp/lesson7/Cubes.cpp
                src/main/cpp/lesson7/Cubes.h
                src/main/cpp/lesson7/CubesClientSide.cpp
                src/main/cpp/lesson7/CubesClientSide.h
                src/main/cpp/lesson7/CubesClientSideWithStride.cpp
                src/main/cpp/lesson7/CubesClientSideWithStride.h
                src/main/cpp/lesson7/CubesWithVbo.cpp
                src/main/cpp/lesson7/CubesWithVbo.h
                src/main/cpp/lesson7/CubesWithVboWithStride.cpp
                src/main/cpp/lesson7/CubesWithVboWithStride.h
                src/main/cpp/lesson7/GenData.cpp
                src/main/cpp/lesson7/GenData.h

                # lesson8
                src/main/cpp/lesson8/Native8Lesson.cpp
                src/main/cpp/lesson8/Native8Lesson.h
                src/main/cpp/lesson8/HeightMap.cpp
                src/main/cpp/lesson8/HeightMap.h

                # filter
                # src/main/cpp/samplecode.cpp
                src/main/cpp/facetune/facetune.cpp
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
                       lesson-lib
                       EGL
                       GLESv2
                       libbeautytune
                       android
                       ${log-lib} )