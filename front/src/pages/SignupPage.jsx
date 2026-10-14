import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { signup } from '../api/authApi';
import { Button, Form, Input, Label, Message } from '../components/FormElements';
import { ButtonSpinner } from '../components/Spinner';

function SignupPage() {
  const [form, setForm] = useState({ loginId: '', password: '', passwordConfirm: '', nickname: '' });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // 프론트 검사는 "편의"용이다. 같은 규칙을 서버(@Valid)가 다시 검사한다
    if (form.password !== form.passwordConfirm) {
      setError('비밀번호가 서로 다릅니다.');
      return;
    }

    setSubmitting(true);
    try {
      const { passwordConfirm: _, ...body } = form; // 비밀번호 확인 값은 서버로 보내지 않는다
      await signup(body);
      navigate('/login', { state: { signupDone: true } });
    } catch (err) {
      setError(err.response?.data?.error?.message ?? '회원가입에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Form onSubmit={handleSubmit}>
      <h2>회원가입</h2>
      <Label>
        아이디 (4~30자)
        <Input name="loginId" value={form.loginId} onChange={handleChange} autoComplete="username" />
      </Label>
      <Label>
        비밀번호 (8자 이상)
        <Input
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
          autoComplete="new-password"
        />
      </Label>
      <Label>
        비밀번호 확인
        <Input
          name="passwordConfirm"
          type="password"
          value={form.passwordConfirm}
          onChange={handleChange}
          autoComplete="new-password"
        />
      </Label>
      <Label>
        닉네임
        <Input name="nickname" value={form.nickname} onChange={handleChange} />
      </Label>
      {error && <Message $error>{error}</Message>}
      <Button type="submit" disabled={submitting}>
        {submitting ? (
          <>
            <ButtonSpinner /> 가입 중
          </>
        ) : (
          '가입하기'
        )}
      </Button>
    </Form>
  );
}

export default SignupPage;
