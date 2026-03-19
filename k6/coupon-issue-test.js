import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 10,        // 동시 유저 10명
    iterations: 10, // 총 10번 요청
};

export default function () {
    const userId = __VU; // 가상 유저 ID (1~10)

    const payload = JSON.stringify({
        userId: userId,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(
        'http://localhost:8080/api/v1/coupons/3/issues',
        payload,
        params
    );

    check(res, {
        '성공 (201)': (r) => r.status === 201,
        '실패 (409 소진)': (r) => r.status === 409,
    });

    console.log(`userId: ${userId} → status: ${res.status} body: ${res.body}`);
}