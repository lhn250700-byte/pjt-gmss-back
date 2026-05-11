import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 100,
  duration: '10s',

  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

// 미리 발급받은 JWT AccessToken
const ACCESS_TOKEN =
  'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzb2NpYWwiOmZhbHNlLCJuaWNrbmFtZSI6IjEiLCJyb2xlTmFtZXMiOlsiVVNFUiJdLCJlbWFpbCI6ImFhMTIzQHRlc3QuY29tIiwiaWF0IjoxNzc4MjY0Mzg4LCJleHAiOjE3NzgyNjQ5ODh9.KewxCXe75qpugdo0UJnOYhBZYlCz2CeEatpfzksUqo4';

export default function () {
  const url =
    'http://localhost:8080/api/bbs_popularPostWeeklyList?period=weekly';

  const params = {
    headers: {
      Authorization: `Bearer ${ACCESS_TOKEN}`,
      'Content-Type': 'application/json',
    },
  };

  const res = http.get(url, params);

  check(res, {
    'status is 200': (r) => r.status === 200,

    'response time < 1000ms': (r) =>
      r.timings.duration < 1000,

    'response is array': (r) => {
      try {
        return Array.isArray(JSON.parse(r.body));
      } catch (e) {
        return false;
      }
    },
  });

  sleep(1);
}