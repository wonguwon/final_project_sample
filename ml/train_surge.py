"""
거래량 급증 모델 학습 → models/surge_bundle.pkl 저장

  실행:  (ml 폴더에서)  .venv\\Scripts\\python train_surge.py

예측 대상 : "다음 거래일 거래량이 최근 20일 평균보다 많을까?" (11_dl/01_keras.ipynb 와 같은 문제)
저장 방식 : 10_ml/06_model_save.ipynb 의 번들 — Pipeline + 피처 목록 + 메타데이터를 한 파일에
"""

import json
import time
from pathlib import Path

import joblib
import numpy as np
import pandas as pd
import sklearn
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, roc_auc_score
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

from app.features import SURGE_FEATURES, build_surge_dataset, next_day_features, time_split

SEED = 42
BASE_DIR = Path(__file__).resolve().parent
MODEL_DIR = BASE_DIR / "models"


def main():
    prices = pd.read_csv(BASE_DIR / "data" / "prices.csv", encoding="utf-8-sig", parse_dates=["date"])
    df = build_surge_dataset(prices)
    train, test, cutoff = time_split(df)
    print(f"데이터 {len(df):,}행 / 급증 비율 {df['surge'].mean():.3f} / 분할 기준일 {cutoff.date()}")
    print(f"학습 {len(train):,}행 / 검증 {len(test):,}행 / 피처 {len(SURGE_FEATURES)}개")

    # 06_model_save 와 같은 모양의 Pipeline.
    # (트리 모델은 스케일링이 필요 없지만, "전처리 + 모델을 한 덩어리로 저장" 하는 형태를 그대로 따른다)
    pipe = Pipeline([
        ("scaler", StandardScaler()),
        ("model", RandomForestClassifier(n_estimators=100, max_depth=8, random_state=SEED, n_jobs=-1)),
    ])
    t0 = time.perf_counter()
    pipe.fit(train[SURGE_FEATURES], train["surge"])
    print(f"학습 시간 {time.perf_counter() - t0:.1f}초")

    prob = pipe.predict_proba(test[SURGE_FEATURES])[:, 1]
    auc = roc_auc_score(test["surge"], prob)
    acc = accuracy_score(test["surge"], (prob >= 0.5).astype(int))
    print(f"검증 AUC {auc:.4f} / 정확도 {acc:.4f} / 전부 '급증 아님' 으로 찍었을 때 {1 - test['surge'].mean():.4f}")
    # 정확도가 "전부 아님" 과 같다 = 확률이 0.5 를 넘는 경우가 거의 없다. 그래도 확률의 순서는 의미가 있다
    print(f"확률 범위 {prob.min():.3f} ~ {prob.max():.3f} / 0.5 이상 {(prob >= 0.5).mean():.1%}")
    top, bottom = prob >= np.quantile(prob, 0.8), prob <= np.quantile(prob, 0.2)
    print(f"실제 급증 비율 — 확률 상위 20% {test['surge'][top].mean():.1%} / 하위 20% {test['surge'][bottom].mean():.1%}")

    check_parity(prices, df)

    # 번들: 모델과 함께 남겨야 할 것을 한 덩어리로 (06_model_save 6절)
    bundle = {
        "pipeline": pipe,
        "features": SURGE_FEATURES,
        # 평소 급증 비율. 이 모델의 확률은 0.5 를 거의 넘지 않으므로(최대 0.47 정도),
        # 0.5 로 자르지 않고 "평소 비율보다 높은가" 로 판단할 때 쓴다
        "base_rate": round(float(train["surge"].mean()), 4),
        "train_period": [str(train["date"].min().date()), str(train["date"].max().date())],
        "test_auc": round(float(auc), 4),
        "test_accuracy": round(float(acc), 4),
        "sklearn_version": sklearn.__version__,
        "created_at": time.strftime("%Y-%m-%d %H:%M:%S"),
    }
    MODEL_DIR.mkdir(exist_ok=True)
    joblib.dump(bundle, MODEL_DIR / "surge_bundle.pkl")

    meta = {k: v for k, v in bundle.items() if k != "pipeline"}
    (MODEL_DIR / "meta.json").write_text(json.dumps(meta, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"저장 : {MODEL_DIR / 'surge_bundle.pkl'}")
    print(json.dumps(meta, ensure_ascii=False, indent=2))


def check_parity(prices, df):
    """
    학습 피처와 예측 피처가 정말 같은지 확인한다.

    날짜 D 까지의 최근 30일 시세로 next_day_features() 를 계산하면,
    학습 데이터에서 "D 다음 거래일" 행의 피처와 같아야 한다. 다르면 서빙이 조용히 틀린 것이다.
    """
    for code in ["G0001", "G0050", "G0120"]:
        p = prices[prices["code"] == code].sort_values("date").reset_index(drop=True)
        for i in [100, 400, len(p) - 2]:
            window = p.iloc[i - 29:i + 1]            # D 까지 30일
            next_date = p.iloc[i + 1]["date"]        # D 다음 거래일
            served = next_day_features(window).iloc[0]
            trained = df[(df["code"] == code) & (df["date"] == next_date)][SURGE_FEATURES].iloc[0]
            diff = np.abs(served.to_numpy() - trained.to_numpy()).max()
            assert np.allclose(served, trained, rtol=1e-9, atol=1e-12), f"{code} {next_date.date()} 차이 {diff}"
    print("피처 일치 확인 : 학습 피처 = 예측 피처 (3종목 × 3날짜)")


if __name__ == "__main__":
    main()
