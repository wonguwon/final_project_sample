import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import styled from 'styled-components';
import { getCompanies } from '../api/companyApi';
import { getPrediction } from '../api/predictionApi';
import { Select } from '../components/FormElements';
import { PageSpinner } from '../components/Spinner';
import { errorMessage } from '../utils/format';

// 판정(level)별 문구와 색
const LEVELS = {
  HIGH: { label: '평소보다 높음', color: '#dc2626' },
  NORMAL: { label: '평소 수준', color: '#6b7280' },
  LOW: { label: '평소보다 낮음', color: '#2563eb' },
};

const Card = styled.section`
  margin-top: 16px;
  padding: 24px;
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.radiusLg};
  background: ${({ theme }) => theme.colors.white};
  box-shadow: ${({ theme }) => theme.shadow};
`;

const Muted = styled.p`
  font-size: 14px;
  color: ${({ theme }) => theme.colors.muted};
`;

const Headline = styled.div`
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin: 12px 0;

  strong {
    font-size: 40px;
  }
`;

const Badge = styled.span`
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
  color: ${({ $color }) => $color};
  border: 1px solid ${({ $color }) => $color};
`;

// 확률 막대 + 평소 비율 표시선
const Bar = styled.div`
  position: relative;
  height: 12px;
  border-radius: 999px;
  background: ${({ theme }) => theme.colors.border};
`;

const Fill = styled.div`
  width: ${({ $percent }) => $percent}%;
  height: 100%;
  border-radius: 999px;
  background: ${({ $color }) => $color};
`;

const Marker = styled.div`
  position: absolute;
  top: -4px;
  left: ${({ $percent }) => $percent}%;
  width: 2px;
  height: 20px;
  background: ${({ theme }) => theme.colors.text};
`;

const Table = styled.table`
  width: 100%;
  margin-top: 20px;
  border-collapse: collapse;

  td {
    padding: 8px 4px;
    border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  }

  td:last-child {
    text-align: right;
  }
`;

const Notice = styled.p`
  margin-top: 16px;
  font-size: 13px;
  color: ${({ theme }) => theme.colors.muted};
`;

const percent = (v) => `${(v * 100).toFixed(1)}%`;
const number = (v) => Math.round(v).toLocaleString();

function PredictPage() {
  // 선택한 종목은 주소(?code=G0001)에 둔다 → 새로고침해도 그대로
  const [searchParams, setSearchParams] = useSearchParams();
  const code = searchParams.get('code') ?? '';

  const [companies, setCompanies] = useState([]);
  const [result, setResult] = useState({ code: null, data: null, error: null }); // 마지막으로 받은 응답
  const loading = code !== '' && result.code !== code;

  useEffect(() => {
    getCompanies(1, 100).then((res) => setCompanies(res.data.data));
  }, []);

  useEffect(() => {
    if (!code) return;
    getPrediction(code)
      .then((res) => setResult({ code, data: res.data.data, error: null }))
      .catch((err) => setResult({ code, data: null, error: errorMessage(err) }));
  }, [code]);

  const p = result.code === code ? result.data : null;
  const level = p ? LEVELS[p.level] : null;

  return (
    <section>
      <h2>거래량 급증 예측</h2>
      <Muted>다음 거래일 거래량이 최근 20일 평균보다 많을지, 머신러닝 모델이 계산한 확률입니다.</Muted>

      <Select value={code} onChange={(e) => setSearchParams(e.target.value ? { code: e.target.value } : {})}>
        <option value="">종목 선택</option>
        {companies.map((c) => (
          <option key={c.code} value={c.code}>
            {c.name}
          </option>
        ))}
      </Select>

      {loading && <PageSpinner />}
      {!loading && result.code === code && result.error && <Card>{result.error}</Card>}

      {!loading && p && (
        <Card>
          <h3>{p.companyName}</h3>
          <Muted>{p.baseDate} 까지의 시세로 계산한 "다음 거래일" 예측</Muted>

          <Headline>
            <strong>{percent(p.probability)}</strong>
            <Badge $color={level.color}>{level.label}</Badge>
          </Headline>

          <Bar>
            <Fill $percent={p.probability * 100} $color={level.color} />
            <Marker $percent={p.baseRate * 100} />
          </Bar>
          <Muted>세로선 = 평소 급증 비율 {percent(p.baseRate)}</Muted>

          <Table>
            <tbody>
              <tr>
                <td>마지막 날 거래량</td>
                <td>{number(p.lastVolume)}</td>
              </tr>
              <tr>
                <td>최근 20일 평균 거래량</td>
                <td>{number(p.volumeAvg20)}</td>
              </tr>
              <tr>
                <td>거래량 배수 (20일 평균 대비)</td>
                <td>{p.features.volume_ratio.toFixed(2)}배</td>
              </tr>
              <tr>
                <td>5일 수익률</td>
                <td>{percent(p.features.ret_5d)}</td>
              </tr>
              <tr>
                <td>20일 변동성</td>
                <td>{percent(p.features.vol20)}</td>
              </tr>
            </tbody>
          </Table>

          <Notice>
            모델: RandomForest · 학습 기간 {p.modelInfo.trainPeriod[0]} ~ {p.modelInfo.trainPeriod[1]} · 검증 AUC{' '}
            {p.modelInfo.testAuc}
            <br />
            AUC 0.5 는 무작위로 찍는 수준, 1.0 은 완벽한 예측입니다. 이 예측은 학습용이며 투자 판단의 근거가 아닙니다.
          </Notice>
        </Card>
      )}
    </section>
  );
}

export default PredictPage;
