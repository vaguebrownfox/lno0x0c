# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

include_directories(
        header
        header/kaldi
        header/libsvm
        header/openfst
)

add_library( # Sets the name of the library.
             lno-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
        library/kaldi/base/io-funcs.cc
        library/kaldi/base/kaldi-error.cc
        library/kaldi/base/kaldi-math.cc
        library/kaldi/base/kaldi-utils.cc
        library/kaldi/base/timer.cc

        library/kaldi/feat/feature-functions.cc
        library/kaldi/feat/feature-mfcc.cc
        library/kaldi/feat/feature-window.cc
        library/kaldi/feat/mel-computations.cc
        library/kaldi/feat/resample.cc
        library/kaldi/feat/wave-reader.cc

        library/kaldi/ivector/voice-activity-detection.cc

        library/kaldi/ivectorbin/compute-vad.cc

        library/kaldi/matrix/compressed-matrix.cc
        library/kaldi/matrix/kaldi-matrix.cc
        library/kaldi/matrix/kaldi-vector.cc
        library/kaldi/matrix/matrix-functions.cc
        library/kaldi/matrix/optimization.cc
        library/kaldi/matrix/packed-matrix.cc
        library/kaldi/matrix/qr.cc
        library/kaldi/matrix/sp-matrix.cc
        library/kaldi/matrix/sparse-matrix.cc
        library/kaldi/matrix/srfft.cc
        library/kaldi/matrix/tp-matrix.cc

        library/kaldi/transform/cmvn.cc

        library/kaldi/util/kaldi-holder.cc
        library/kaldi/util/kaldi-io.cc
        library/kaldi/util/kaldi-semaphore.cc
        library/kaldi/util/kaldi-table.cc
        library/kaldi/util/parse-options.cc
        library/kaldi/util/simple-io-funcs.cc
        library/kaldi/util/text-utils.cc

        library/libsvm/svm-predict.c
        library/libsvm/svm.cpp

        lno-lib.cpp
        asthma-predict.cpp
        compute-mfcc-feats.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

add_library(lapack
        STATIC
        IMPORTED)

add_library(blas
        STATIC
        IMPORTED)

add_library(clapack
        STATIC
        IMPORTED)

add_library(f2c
        STATIC
        IMPORTED)

add_library(openblas
        STATIC
        IMPORTED)


set_target_properties(blas
        PROPERTIES
        IMPORTED_LOCATION  ${CMAKE_SOURCE_DIR}/static/${ANDROID_ABI}/libblas.a)

set_target_properties(clapack
        PROPERTIES
        IMPORTED_LOCATION  ${CMAKE_SOURCE_DIR}/static/${ANDROID_ABI}/libclapack.a)

set_target_properties(lapack
        PROPERTIES
        IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/static/${ANDROID_ABI}/liblapack.a)

set_target_properties(f2c
        PROPERTIES
        IMPORTED_LOCATION  ${CMAKE_SOURCE_DIR}/static/${ANDROID_ABI}/libf2c.a)

set_target_properties(openblas
        PROPERTIES
        IMPORTED_LOCATION  ${CMAKE_SOURCE_DIR}/static/${ANDROID_ABI}/libopenblas.a)


target_link_libraries( # Specifies the target library.
                       lno-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
        lapack
        clapack
        blas
        f2c
        openblas
        )