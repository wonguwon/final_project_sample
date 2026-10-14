"""
요청 · 응답 모양 (Pydantic).

Spring 과 주고받는 JSON 은 camelCase(baseDate, volumeAvg20)로 맞춘다.
파이썬 안에서는 snake_case(base_date)로 쓰고, alias_generator 가 바꿔 준다.
"""

from datetime import date

from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel


class CamelModel(BaseModel):
    # alias_generator: 필드 이름 base_date ↔ JSON 키 baseDate
    # populate_by_name: 요청에서 snake_case 로 보내도 받아 준다
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class PriceRow(CamelModel):
    date: date
    open: int = Field(gt=0)
    high: int = Field(gt=0)
    low: int = Field(gt=0)
    close: int = Field(gt=0)
    volume: int = Field(ge=0)


class SurgeRequest(CamelModel):
    code: str
    # 최근 시세, 날짜 오름차순. 피처(20일 변동성)를 계산하려면 21일 이상 필요하다
    prices: list[PriceRow] = Field(min_length=21, max_length=120)


class ModelInfo(CamelModel):
    train_period: list[str]
    test_auc: float
    base_rate: float
    created_at: str


class SurgeResponse(CamelModel):
    code: str
    base_date: date                 # 마지막 시세 날짜. 예측 대상은 "이 날의 다음 거래일"
    probability: float              # 다음 거래일 거래량이 20일 평균보다 많을 확률
    base_rate: float                # 평소 급증 비율 (학습 데이터)
    level: str                      # HIGH · NORMAL · LOW (평소 비율과 비교)
    last_volume: int                # 마지막 날 거래량
    volume_avg20: float             # 최근 20일 평균 거래량
    features: dict[str, float]      # 모델에 넣은 피처 값
    model_info: ModelInfo
