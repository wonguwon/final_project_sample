package com.kh.back.prediction.service;

import com.kh.back.prediction.dto.SurgePrediction;

public interface PredictionService {

    SurgePrediction predictSurge(String code);
}
