#pragma once

#include "kaldi/base/kaldi-common.h"
#include "kaldi/util/common-utils.h"
#include "kaldi/feat/feature-mfcc.h"
#include "kaldi/feat/wave-reader.h"
#include "kaldi/feat/resample.h"
#include "kaldi/matrix/kaldi-matrix.h"
#include "kaldi/transform/cmvn.h"

using namespace kaldi;

extern int samp_freq;
Matrix<BaseFloat> compute_mfcc_feats(const char* audiofilepath);