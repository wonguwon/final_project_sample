"""
거래량 급증 예측 API (FastAPI)

  실행:  (ml 폴더에서)  .venv\\Scripts\\uvicorn app.main:app --port 8000
  문서:  http://localhost:8000/docs   (Swagger — 브라우저에서 바로 요청해 볼 수 있다)

이 서버는 "계산"만 한다. DB 를 읽지 않는다.
최근 시세는 Spring 이 DB 에서 읽어 요청 본문으로 보내 준다.
"""

from contextlib import asynccontextmanager
from pathlib import Path

import joblib
import pandas as pd
from fastapi import FastAPI, HTTPException

from app.features import next_day_features
from app.schemas import ModelInfo, SurgeRequest, SurgeResponse

MODEL_PATH = Path(__file__).resolve().parent.parent / "models" / "surge_bundle.pkl"

# 서버가 뜰 때 한 번 불러 둔 모델을 담아 두는 곳
state = {}


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 모델은 요청마다가 아니라 서버가 뜰 때 "한 번" 불러온다 (06_model_save: 로드가 예측보다 느리다)
    state["bundle"] = joblib.load(MODEL_PATH)
    yield
    state.clear()


app = FastAPI(title="KH 주식 예측 API", version="1.0.0", lifespan=lifespan)


def model_info(bundle) -> ModelInfo:
    return ModelInfo(
        train_period=bundle["train_period"],
        test_auc=bundle["test_auc"],
        base_rate=bundle["base_rate"],
        created_at=bundle["created_at"],
    )


@app.get("/health")
def health():
    """서버와 모델이 준비됐는지 확인한다."""
    bundle = state["bundle"]
    return {"status": "ok", "features": bundle["features"], "modelInfo": model_info(bundle).model_dump(by_alias=True)}


@app.post("/predict/surge", response_model=SurgeResponse)
def predict_surge(request: SurgeRequest):
    """
    최근 시세로 "마지막 날의 다음 거래일 거래량이 최근 20일 평균보다 많을지" 확률을 계산한다.

    async 가 아닌 일반 def 로 둔다. 예측은 CPU 계산이라 FastAPI 가 별도 스레드에서 실행해 준다.
    """
    bundle = state["bundle"]

    prices = pd.DataFrame([p.model_dump() for p in request.prices])
    prices["code"] = request.code
    prices["date"] = pd.to_datetime(prices["date"])
    prices = prices.sort_values("date").reset_index(drop=True)

    try:
        X = next_day_features(prices, bundle["features"])
    except ValueError as e:
        raise HTTPException(status_code=422, detail=str(e))

    probability = float(bundle["pipeline"].predict_proba(X)[0, 1])

    # 이 모델의 확률은 0.5 를 거의 넘지 않는다. 0.5 로 자르지 않고 평소 급증 비율과 비교한다
    base_rate = bundle["base_rate"]
    if probability >= base_rate * 1.15:
        level = "HIGH"
    elif probability <= base_rate * 0.85:
        level = "LOW"
    else:
        level = "NORMAL"

    return SurgeResponse(
        code=request.code,
        base_date=prices["date"].iloc[-1].date(),
        probability=round(probability, 4),
        base_rate=base_rate,
        level=level,
        last_volume=int(prices["volume"].iloc[-1]),
        volume_avg20=round(float(prices["volume"].tail(20).mean()), 1),
        features={k: round(float(v), 6) for k, v in X.iloc[0].items()},
        model_info=model_info(bundle),
    )
