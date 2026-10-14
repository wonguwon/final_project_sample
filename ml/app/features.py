"""
피처 계산 — 학습(train_surge.py)과 예측(app/main.py)이 같은 함수를 쓴다.

add_features() 는 강의 10_ml/_ml.py 의 함수와 같은 식이다. 한 글자도 바꾸지 않았다.
학습할 때와 예측할 때 피처를 다르게 계산하면, 에러 없이 "조용히 틀린" 예측이 나온다.
"""

import numpy as np
import pandas as pd

# 10_ml/_ml.py 의 피처 7개
FEATURES = [
    "ret_1d",        # 전일 수익률
    "ret_5d",        # 5일 수익률
    "ma5_ratio",     # 5일 이동평균 대비 비율
    "ma20_ratio",    # 20일 이동평균 대비 비율
    "vol20",         # 20일 변동성
    "volume_ratio",  # 거래량 20일 평균 대비 배수
    "range_pct",     # 당일 변동폭
]

# 11_dl/01_keras.ipynb 의 거래량 급증 모델 피처 9개 = 위 7개 + 어제 거래량 · 어제 종가
SURGE_FEATURES = FEATURES + ["volume_prev", "close_prev"]

# 예측에 필요한 최소 일수: vol20 = 수익률(전날이 필요) 20일의 표준편차 → 종가 21일
MIN_ROWS = 21


def add_features(df, shift_features=True):
    """
    피처를 만든다. (10_ml/_ml.py 와 같은 식)

      shift_features=True  : 모든 피처를 한 칸 민다. "어제까지의 정보로 오늘을 예측" (학습용)
      shift_features=False : 밀지 않는다. 그날까지의 정보 그대로 (예측용, 아래 next_day_features 참고)
    """
    df = df.sort_values(["code", "date"]).copy()
    g = df.groupby("code")

    df["ret_1d"] = g["close"].transform(lambda s: s.pct_change())
    df["ret_5d"] = g["close"].transform(lambda s: s.pct_change(5))
    df["ma5_ratio"] = df["close"] / g["close"].transform(lambda s: s.rolling(5).mean())
    df["ma20_ratio"] = df["close"] / g["close"].transform(lambda s: s.rolling(20).mean())
    df["vol20"] = g["close"].transform(lambda s: s.pct_change().rolling(20).std())
    df["volume_ratio"] = df["volume"] / g["volume"].transform(lambda s: s.rolling(20).mean())
    df["range_pct"] = (df["high"] - df["low"]) / df["low"]

    if shift_features:
        # 한 칸 밀어 '어제까지의 정보' 로 만든다. 반드시 종목별로.
        for col in FEATURES:
            df[col] = df.groupby("code")[col].shift(1)

    return df


def build_surge_dataset(prices):
    """
    학습용 데이터. (10_ml/_ml.py 의 build_dataset + 11_dl/01_keras.ipynb 의 load_dataset)

    정답 surge = 오늘 거래량 > 어제까지 20일 평균 거래량
    """
    df = add_features(prices, shift_features=True)
    df = df.dropna(subset=FEATURES).reset_index(drop=True)
    df = df.replace([np.inf, -np.inf], np.nan).dropna(subset=FEATURES).reset_index(drop=True)

    g = df.sort_values(["code", "date"]).groupby("code")
    df["volume_prev"] = g["volume"].shift(1)  # 어제 거래량
    df["close_prev"] = g["close"].shift(1)    # 어제 종가

    # 정답은 피처를 만들기 전 원본 시세에서 계산한다 (01_keras.ipynb 와 같은 방식)
    raw = prices.sort_values(["code", "date"]).copy()
    raw["vol_ma20_prev"] = raw.groupby("code")["volume"].transform(lambda s: s.rolling(20).mean().shift(1))
    raw["surge"] = (raw["volume"] > raw["vol_ma20_prev"]).astype("int8")

    df = df.merge(raw[["code", "date", "surge"]], on=["code", "date"], how="left")
    return df.dropna(subset=["volume_prev", "close_prev", "surge"]).reset_index(drop=True)


def time_split(df, test_ratio=0.2):
    """시계열은 랜덤 분할하면 안 된다. 날짜 기준으로 앞(학습) · 뒤(검증)를 자른다. (10_ml/_ml.py)"""
    cutoff = df["date"].quantile(1 - test_ratio)
    train = df[df["date"] <= cutoff].reset_index(drop=True)
    test = df[df["date"] > cutoff].reset_index(drop=True)
    return train, test, cutoff


def next_day_features(prices, features=SURGE_FEATURES):
    """
    최근 시세(한 종목, 날짜 오름차순)로 "마지막 날의 다음 거래일" 을 예측할 피처 1행을 만든다.

    학습 데이터에서 날짜 D 의 피처는 "D 전날까지의 정보" 였다 (shift(1)).
    그러니 다음 거래일을 예측하려면 마지막 날까지의 정보를 "밀지 않고" 쓰면 된다.
      - 피처 7개      : shift 하지 않은 마지막 행의 값
      - volume_prev  : 마지막 날 거래량 (다음 거래일 입장에서 "어제" 거래량)
      - close_prev   : 마지막 날 종가
    """
    if len(prices) < MIN_ROWS:
        raise ValueError(f"시세가 {MIN_ROWS}일 이상 필요합니다. (받은 일수: {len(prices)})")

    df = add_features(prices, shift_features=False)
    last = df.iloc[-1]

    row = {col: last[col] for col in FEATURES}
    row["volume_prev"] = last["volume"]
    row["close_prev"] = last["close"]

    X = pd.DataFrame([row])[features].astype("float64")  # 저장된 피처 순서대로 정렬 (06_model_save 의 교훈)
    if not np.isfinite(X.to_numpy()).all():
        raise ValueError("피처를 계산할 수 없습니다. (거래량 · 가격이 0 인 날이 있는지 확인)")
    return X
