//
// Created by darwin on 16/7/20.
//

#include "compute-mfcc-feats.h"

extern int samp_freq;

Matrix<BaseFloat> compute_mfcc_feats(const char* filepath) {

    MfccOptions mfcc_opts;
//    mfcc_opts.frame_opts.frame_length_ms = 20;
//    mfcc_opts.frame_opts.frame_shift_ms = 10;
//    mfcc_opts.frame_opts.window_type = "hamming";
//    mfcc_opts.mel_opts.num_bins = 23;
//    mfcc_opts.mel_opts.low_freq = 300;
//    mfcc_opts.mel_opts.high_freq = 7800;
//    mfcc_opts.num_ceps = 13;
//    mfcc_opts.raw_energy = false;
//    mfcc_opts.cepstral_lifter = 0.0;

    BaseFloat vtln_warp = 1.0;
    std::string vtln_map_rspecifier;
    BaseFloat min_duration = 0.0;

    mfcc_opts.cepstral_lifter = 22.0;
    mfcc_opts.raw_energy = false;
    mfcc_opts.num_ceps = 13;
    mfcc_opts.energy_floor = 0.001;
    mfcc_opts.htk_compat = false;
    mfcc_opts.use_energy = true;
    mfcc_opts.mel_opts.low_freq = 40;
    mfcc_opts.mel_opts.high_freq = 7800;
    mfcc_opts.mel_opts.num_bins = 23;
    mfcc_opts.mel_opts.debug_mel = false;
    mfcc_opts.mel_opts.htk_mode = false;
    mfcc_opts.mel_opts.vtln_high = 7300;
    mfcc_opts.mel_opts.vtln_low = 60;
    mfcc_opts.frame_opts.frame_shift_ms = 10;
    mfcc_opts.frame_opts.frame_length_ms = 20;
    mfcc_opts.frame_opts.allow_downsample = false;
    mfcc_opts.frame_opts.window_type = "hamming";
    mfcc_opts.frame_opts.dither = 0.0;
    mfcc_opts.frame_opts.preemph_coeff = 0.0;
    mfcc_opts.frame_opts.remove_dc_offset = true;
    mfcc_opts.frame_opts.samp_freq = 16000;
    mfcc_opts.frame_opts.round_to_power_of_two = true;
    mfcc_opts.frame_opts.snip_edges = true;


    Mfcc mfcc(mfcc_opts);

    try {
        Input waveFile;
        waveFile.Open(filepath);
        WaveHolder waveHolder;
        waveHolder.Read(waveFile.Stream());
        const WaveData waveData = waveHolder.Value();
        waveFile.Close();


        if (waveData.Duration() < min_duration) {
            KALDI_WARN << "File is too short ("
                       << waveData.Duration() << " sec): producing no output.";
            exit(-1);
        }

        int32 num_chan = waveData.Data().NumRows();
        int32 channel0;

        {  // This block works out the channel (0=left, 1=right...)
            KALDI_ASSERT(num_chan > 0);  // should have been caught in
            // reading code if no channels.
            channel0 = 0;
            if (num_chan != 1)
                KALDI_WARN << "Channel not specified but you have data with "
                           << num_chan << " channels; defaulting to zero";
        }

        SubVector<BaseFloat> waveform0(waveData.Data(), channel0);
        Matrix<BaseFloat> features0;
        int sample_freq = waveData.SampFreq();

        mfcc.ComputeFeatures(waveform0, sample_freq, vtln_warp, &features0);

        return features0;

    } catch  (const std::exception &e) {
        std::cerr << e.what();
        exit(-1);
    }
}