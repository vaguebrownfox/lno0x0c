#include <jni.h>
#include <string>

#include "feat-util.h"
#include<iostream>
#include<fstream>
#include<vector>
#include "compute-mfcc-feats.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_fiaxco_nativek0x09_MainActivity_lnoPredict(
        JNIEnv* env,
        jobject /* this */,
        jstring filepath) {

    const char* file = env->GetStringUTFChars(filepath, nullptr);

    Matrix<BaseFloat> rawMfcc = compute_mfcc_feats(file);

    std::string info1 = "num coeff rows: " + std::to_string(rawMfcc.NumRows());

    std::string feats = "\n";

    feats += "samp freq: 1600Hz\n";
    feats += "frame length, shift: 20ms, 10ms\n";
    feats += "mel low - high freq: 300Hz - 7800Hz\n";
    feats += "num mel bins: 23\n";
    feats += "num cepstral coeffs: 13\n";
    feats += info1 + "\n";
    feats += "window: hamming\n\n";
    feats += "coefficients: \n";

    for (int j = 0; j < rawMfcc.NumRows(); j++) {
        float* feat = rawMfcc.RowData(j);

        for (int i = 0; i < 14; i++ ) {
            feats += std::to_string(feat[i]) + ", ";
        }
        feats += "\n";
    }

    return env->NewStringUTF(feats.c_str());
}
