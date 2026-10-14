"""
prices.csv (90,000행) → MySQL daily_price 적재

  준비:  back/sql/price.sql 로 테이블을 만들고, ml/.env 에 DB 접속 정보를 넣는다
  실행:  (ml 폴더에서)  .venv\\Scripts\\python scripts\\load_prices.py

강의 09_database/pipeline/load.py 와 같은 방식이다.
  - ON DUPLICATE KEY UPDATE (UPSERT) → 두 번 실행해도 행이 늘어나지 않는다 (멱등)
  - 5,000행씩 나눠 보내고, 나눌 때마다 커밋
  - 적재 후 행 수뿐 아니라 합계 · 날짜 범위까지 DB 와 대조
"""

import os
import time
from pathlib import Path

import pandas as pd
import pymysql
from dotenv import load_dotenv

BASE_DIR = Path(__file__).resolve().parent.parent
load_dotenv(BASE_DIR / ".env")

CHUNK_SIZE = 5_000
COLS = ["code", "date", "open", "high", "low", "close", "volume", "change", "changeRate"]
COL_SQL = ", ".join(f"`{c}`" for c in COLS)
UPSERT = f"""
INSERT INTO daily_price ({COL_SQL}) VALUES ({", ".join(["%s"] * len(COLS))})
ON DUPLICATE KEY UPDATE
    open=VALUES(open), high=VALUES(high), low=VALUES(low),
    close=VALUES(close), volume=VALUES(volume),
    `change`=VALUES(`change`), changeRate=VALUES(changeRate)
"""


def connect():
    return pymysql.connect(
        host=os.getenv("DB_HOST", "127.0.0.1"),
        port=int(os.getenv("DB_PORT", 3306)),
        user=os.getenv("DB_USER"),
        password=os.getenv("DB_PASSWORD"),
        database=os.getenv("DB_NAME"),
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )


def load(df):
    rows = [tuple(r) for r in df[COLS].astype(object).itertuples(index=False)]
    conn = connect()
    start = time.perf_counter()
    try:
        for i in range(0, len(rows), CHUNK_SIZE):
            with conn.cursor() as cur:
                cur.executemany(UPSERT, rows[i:i + CHUNK_SIZE])
            conn.commit()  # 나눌 때마다 커밋
            print(f"  {min(i + CHUNK_SIZE, len(rows)):>6,} / {len(rows):,}")
    except Exception:
        conn.rollback()  # 커밋하지 못한 마지막 묶음을 되돌리고 예외는 그대로 올린다
        raise
    finally:
        conn.close()
    return time.perf_counter() - start


def verify(df):
    """적재 후 검증. 행 수만 보면 놓치므로 집계값도 대조한다."""
    conn = connect()
    with conn.cursor() as cur:
        cur.execute("""
            SELECT COUNT(*) AS n, COUNT(DISTINCT code) AS codes, SUM(close) AS close_sum,
                   MIN(date) AS min_d, MAX(date) AS max_d
            FROM daily_price
        """)
        db = cur.fetchone()
    conn.close()

    checks = {
        "행 수": (len(df), db["n"]),
        "종목 수": (df["code"].nunique(), db["codes"]),
        "종가 합계": (int(df["close"].sum()), int(db["close_sum"])),
        "최소 날짜": (str(df["date"].min().date()), str(db["min_d"])),
        "최대 날짜": (str(df["date"].max().date()), str(db["max_d"])),
    }
    all_ok = True
    for name, (expected, actual) in checks.items():
        ok = str(expected) == str(actual)
        all_ok &= ok
        print(f"  {'OK  ' if ok else 'FAIL'} {name:<8} {expected} / {actual}")
    return all_ok


if __name__ == "__main__":
    df = pd.read_csv(BASE_DIR / "data" / "prices.csv", encoding="utf-8-sig", parse_dates=["date"])
    df["date"] = df["date"].dt.date
    print(f"적재 시작 : {len(df):,}행")
    elapsed = load(df)
    print(f"적재 완료 : {elapsed:.1f}초")

    df["date"] = pd.to_datetime(df["date"])
    print("검증")
    print("통과" if verify(df) else "실패 — 위의 FAIL 항목을 확인하세요")
