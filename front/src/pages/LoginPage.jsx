import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { login } from '../api/authApi';
import { Button, Form, Input, Label, Message } from '../components/FormElements';
import { ButtonSpinner } from '../components/Spinner';

const Footer = styled.p`
  text-align: center;
  font-size: 14px;
  color: ${({ theme }) => theme.colors.muted};

  a {
    margin-left: 6px;
    font-weight: 700;
    color: ${({ theme }) => theme.colors.primary};
  }

  a:hover {
    text-decoration: underline;
  }
`;

function LoginPage() {
  const [form, setForm] = useState({ loginId: '', password: '' });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  // RequireAuth 가 넘겨준 "원래 가려던 주소". 없으면 홈으로
  const from = location.state?.from ?? '/';
  const signupDone = location.state?.signupDone;

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(form);
      navigate(from, { replace: true }); // replace: 뒤로 가기로 로그인 화면에 돌아오지 않게
    } catch (err) {
      // 서버가 보낸 실패 봉투의 message 를 그대로 보여 준다
      setError(err.response?.data?.error?.message ?? '로그인에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Form onSubmit={handleSubmit}>
      <h2>로그인</h2>
      {signupDone && <Message>회원가입이 완료되었습니다. 로그인해 주세요.</Message>}
      <Label>
        아이디
        <Input name="loginId" value={form.loginId} onChange={handleChange} autoComplete="username" />
      </Label>
      <Label>
        비밀번호
        <Input
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
          autoComplete="current-password"
        />
      </Label>
      {error && <Message $error>{error}</Message>}
      <Button type="submit" disabled={submitting}>
        {submitting ? (
          <>
            <ButtonSpinner /> 로그인 중
          </>
        ) : (
          '로그인'
        )}
      </Button>
      <Footer>
        <Link to="/signup">회원가입</Link>
      </Footer>
    </Form>
  );
}

export default LoginPage;
