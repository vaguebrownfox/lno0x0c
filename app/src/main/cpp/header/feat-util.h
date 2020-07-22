#pragma once

#include "base/kaldi-common.h"
#include "util/common-utils.h"
#include "matrix/kaldi-matrix.h"
#include "ivectorbin/compute-vad.h"
#include <algorithm>
#include <fstream>

using namespace kaldi;
#define SQR(a) (a*a)
extern int no_of_rows;
extern int no_of_mfcc_feats;

void writeFeats(const char* featfilename,Matrix<BaseFloat> feats,int* labels);